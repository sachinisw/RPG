package trace;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import actors.Agent;
import actors.Attacker;
import actors.User;
import graph.ActionEdge;
import graph.StateGraph;
import graph.StateVertex;
import run.InitialState;
import run.Observation;
import run.State;
import run.StateGenerator;

public class TraceGenerator {

	public final static int filterLimit = 80;
	public final static double selectProbability = 0.80;

	//this can be any observation sequence. For this one I only want the initial state graphs for attacker and user.
	public static Observation setObservations(String obsPath){
		Observation obs = new Observation();
		obs.readObservationFile(obsPath);
		return obs;
	}

	public static ArrayList<State> copyStates(ArrayList<State> state, int count){
		ArrayList<State> cp = new ArrayList<State>();
		for(int i=0; i<=count; i++){
			cp.add(state.get(i));
		}
		return cp;
	}

	//For this one I only want the initial state graphs for attacker and user.
	public static ArrayList<StateGraph> process(ArrayList<State> states, StateGenerator gen){
		ArrayList<StateGraph> graphs = new ArrayList<>();
		ArrayList<State> statesSeen = copyStates(states, 0);
		StateGraph graphAgent = gen.enumerateStates(states.get(0), statesSeen);
		StateGraph treeAgent = graphAgent.convertToTree(gen.getInitVertex(graphAgent, states.get(0)));
		gen.applyUniformProbabilitiesToStates(treeAgent, states.get(0));
		graphs.add(treeAgent);
		return graphs; //No DOT files generated for traces
	}

	public static ArrayList<StateGraph> generateStateGraphsForObservations(Agent agent, Observation ob, InitialState init){
		StateGenerator gen = new StateGenerator(agent);
		ArrayList<State> state = gen.getStatesAfterObservations(ob, init, true);
		ArrayList<StateGraph> graphs = process(state, gen); //graph for attacker
		return graphs;
	}
	//generates the trace with flagged observations for the classifier
	public static ArrayList<ArrayList<String>> generateTrace(StateGraph attacker, StateGraph user){ 
		ArrayList<ArrayList<String>> trace = new ArrayList<ArrayList<String>>();
		//		ArrayList<ArrayList<StateVertex>> us = user.getAllPathsFromRoot();
		ArrayList<ArrayList<StateVertex>> at = attacker.getAllPathsFromRoot();
		ArrayList<ArrayList<StateVertex>> undesirable = attacker.getUndesirablePaths(at);
		//		for(int i=0; i<us.size(); i++){ //adds paths from user domain to trace. these paths are included in attacker domain also. decided it was not needed.
		//			ArrayList<StateVertex> list = us.get(i);
		//			ArrayList<String> trc = new ArrayList<String>();
		//			for(int j=0; j<list.size()-1; j++){
		//				ArrayList<ActionEdge> actions = user.findEdgeForStateTransition(list.get(j), list.get(j+1));
		//				for (ActionEdge actionEdge : actions) {
		//					//trc.add(actionEdge.getAction());
		//					if(edgeInUndesirablePath(actionEdge, undesirable)){ //find trouble action. causing critical state. must be flagged
		//						//Y for steps until critical state N for steps after critical state
		//						if(edgeTriggersCriticalState(actionEdge)){ 
		//							trc.add("N:"+actionEdge.getAction());
		//						}else{
		//							trc.add("Y:"+actionEdge.getAction());
		//						}
		//					}else{
		//						trc.add("N:"+actionEdge.getAction());
		//					}
		//				}
		//			}
		//			trace.add(trc);
		//		}
		for(int i=0; i<at.size(); i++){
			ArrayList<StateVertex> list = at.get(i);
			ArrayList<String> trc = new ArrayList<String>();
			for(int j=0; j<list.size()-1; j++){
				ArrayList<ActionEdge> actions = attacker.findEdgeForStateTransition(list.get(j), list.get(j+1));
				for (ActionEdge actionEdge : actions) {
					if(edgeInUndesirablePath(actionEdge, undesirable)){ //find trouble action. causing critical state. must be flagged
						//Y for steps until critical state N for steps after critical state
						if(edgeTriggersCriticalState(actionEdge)){ 
							trc.add("N:"+actionEdge.getAction());
						}else{
							trc.add("Y:"+actionEdge.getAction());
						}
					}else{
						trc.add("N:"+actionEdge.getAction());
					}
				}
			}
			System.out.println(trc);
			trace.add(trc);
		}
		System.out.println("*******************************************************");
		System.out.println(trace);
		System.out.println("*****************Filtering*****************************");
//		System.out.println(selectTracesRandomly(trace));
		return selectTracesRandomly(trace);
//		return trace;
	}

	public static ArrayList<ArrayList<String>> selectTracesRandomly(ArrayList<ArrayList<String>> unfiltered){
		ArrayList<ArrayList<String>> trace = new ArrayList<ArrayList<String>>();
		if(unfiltered.size() > filterLimit){
			for (int i = 0; i < unfiltered.size(); i++) { //select all Y
				ArrayList<String> tr = unfiltered.get(i);
				for (String s : tr) {
					if(s.contains("Y:")){
						trace.add(tr);
						break;
					}
				}
			}
			for (int i = 0; i < unfiltered.size() && trace.size()<=100; i++) {
				ArrayList<String> tr = unfiltered.get(i);
				if(listContainsAllNo(tr) && selectCurrentTrace(tr)){ //filter and select all N traces
					trace.add(tr);
				}
			}
		}else{
			trace.addAll(unfiltered);
		}
		return trace;
	}
	
	private static boolean listContainsAllNo(ArrayList<String> list){
		int count = 0;
		for (String string : list) {
			if(string.contains("N:")){
				count++;
			}
		}
		return count==list.size();
	}

	private static double randDouble(int min, int max) {
		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}

	private static boolean selectCurrentTrace(ArrayList<String> tr){
		if(randDouble(0, 1)> selectProbability){
			return true;
		}
		return false;
	}

	private static boolean edgeInUndesirablePath(ActionEdge e, ArrayList<ArrayList<StateVertex>> undesirable){
		for (ArrayList<StateVertex> undesirablePath : undesirable) {
			for (StateVertex undesirableState : undesirablePath) {
				if(e.getTo().isEqual(undesirableState)){
					return true;
				}
			}
		}
		return false;
	}

	private static boolean edgeTriggersCriticalState(ActionEdge e) {
		if(e.getTo().isContainsCriticalState()){
			return true;
		}
		return false;
	}

	public static void writeTracesToFile(ArrayList<ArrayList<String>> traceset, String tracepath){
		PrintWriter writer = null;
		for (int i = 0; i < traceset.size(); i++) {
			ArrayList<String> tr = traceset.get(i);
			try{
				writer = new PrintWriter(tracepath+"/"+i, "UTF-8");
				for (String string : tr) {
					writer.write(string);
					writer.println();
				}
			}catch (FileNotFoundException | UnsupportedEncodingException  e) {
				e.printStackTrace();
			}finally{
				writer.close();
			}
		}
	}

	public static void generateTraceForScenario(){
		for(int instance=0; instance<=22; instance++){
			if(instance==21){
				String domainFile = ConfigParameters.prefix+instance+ConfigParameters.domainFile;
				String desirableStateFile = ConfigParameters.prefix+instance+ConfigParameters.desirableStateFile;
				String a_problemFile = ConfigParameters.prefix+instance+ConfigParameters.a_problemFile;
				String a_outputPath = ConfigParameters.prefix+instance+ConfigParameters.a_outputPath;
				String criticalStateFile = ConfigParameters.prefix+instance+ConfigParameters.criticalStateFile;
				String a_initFile = ConfigParameters.prefix+instance+ConfigParameters.a_initFile;
				String a_dotFilePrefix = ConfigParameters.prefix+instance;
				String u_problemFile = ConfigParameters.prefix+instance+ConfigParameters.u_problemFile;
				String u_outputPath = ConfigParameters.prefix+instance+ConfigParameters.u_outputPath;
				String u_initFile = ConfigParameters.prefix+instance+ConfigParameters.u_initFile;
				String u_dotFilePrefix = ConfigParameters.prefix+instance;
				String tracepath = ConfigParameters.traces+instance;
				String obspath = ConfigParameters.prefix+instance+ConfigParameters.observationFile;
				Attacker attacker = new Attacker(domainFile, desirableStateFile, a_problemFile, a_outputPath, criticalStateFile, a_initFile, a_dotFilePrefix, ConfigParameters.a_dotFile);
				User user = new User(domainFile, desirableStateFile, u_problemFile, u_outputPath, criticalStateFile, u_initFile, u_dotFilePrefix, ConfigParameters.u_dotFile);
				Observation obs = setObservations(obspath); //TODO: how to handle noise in trace. what counts as noise?
				System.out.println("Generating Attacker's State Tree");
				ArrayList<StateGraph> attackerState = generateStateGraphsForObservations(attacker, obs, attacker.getInitialState());//generate graph for attacker and user
				System.out.println("Generating User's State Tree");
				ArrayList<StateGraph> userState = generateStateGraphsForObservations(user, obs, user.getInitialState());
				System.out.println("Writing traces to files");
				writeTracesToFile(generateTrace(attackerState.get(0), userState.get(0)), tracepath); //i can give the same dot file path beacause I am generating the graph for initial state only
			}
		}
	}

	public static void main(String[] args) { 
		generateTraceForScenario();
	}
}
