package trace;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import actors.Agent;
import actors.Decider;
import graph.ActionEdge;
import graph.StateGraph;
import graph.StateVertex;
import run.InitialState;
import run.Observation;
import run.State;
import run.StateGenerator;

/**
 * Generate observation traces for training the decision tree
 * @author sachini
 *
 */
public class TraceGenerator {
	private static final Logger LOGGER = Logger.getLogger(TraceGenerator.class.getName());
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
		graphs.add(treeAgent);//gen.graphToDOT(graphAgent, 0, 0, true); gen.graphToDOT(treeAgent, 1, 1, true); //TODO: remove after debug
		return graphs; //No DOT files generated for traces
	}

	public static ArrayList<StateGraph> generateStateGraphsForObservations(Agent agent, String domain, Observation ob, InitialState init){
		StateGenerator gen = new StateGenerator(agent, domain);
		ArrayList<State> state = gen.getStatesAfterObservations(ob, init, true);
		ArrayList<StateGraph> graphs = process(state, gen); //graph for attacker
		return graphs;
	}
	//generates the trace with flagged observations for the classifier
	public static ArrayList<ArrayList<String>> generateTrace(StateGraph attacker){ 
		ArrayList<ArrayList<String>> trace = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<StateVertex>> at = attacker.getAllPathsFromRoot();
		ArrayList<ArrayList<StateVertex>> undesirable = attacker.getUndesirablePaths(at, ConfigParameters.domain);
		for(int i=0; i<at.size(); i++){
			ArrayList<StateVertex> list = at.get(i);
			ArrayList<String> trc = new ArrayList<String>();
			for(int j=0; j<list.size()-1; j++){
				ArrayList<ActionEdge> actions = attacker.findEdgeForStateTransition(list.get(j), list.get(j+1));
				for (ActionEdge actionEdge : actions) {
					if(edgeInUndesirablePath(actionEdge, undesirable)){ //find trouble action. causing critical state. must be flagged
						//Y for steps until critical state N for steps after critical state
						if(edgeTriggersCriticalState(actionEdge, attacker.getCritical().getCriticalState())){ 
							trc.add("N:"+actionEdge.getAction());
						}else{
							trc.add("Y:"+actionEdge.getAction());
						}
					}else{
						trc.add("N:"+actionEdge.getAction());
					}
				}
			}
			trace.add(trc);
		}
		return trace;
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

	private static boolean edgeTriggersCriticalState(ActionEdge e, ArrayList<String> criticalstate) {
		if(e.getTo().containsState(criticalstate)){//one type of critical state
			return true;
		}
		return false;
	}

	public static void writeTracesToFile(ArrayList<ArrayList<String>> traceset, String tracepath){
		PrintWriter writer = null;
		LOGGER.log(Level.INFO, "number of traces"+ traceset.size());
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

	public static void generateObservationTraceForScenario(){
		for(int trainInstance=0; trainInstance<=22; trainInstance++){
			if(trainInstance==1){
				String domainFile = ConfigParameters.prefix+trainInstance+ConfigParameters.domainFile;
				String desirableStateFile = ConfigParameters.prefix+trainInstance+ConfigParameters.desirableStateFile;
				String a_problemFile = ConfigParameters.prefix+trainInstance+ConfigParameters.a_problemFile;
				String a_outputPath = ConfigParameters.prefix+trainInstance+ConfigParameters.a_outputPath;
				String criticalStateFile = ConfigParameters.prefix+trainInstance+ConfigParameters.criticalStateFile;
				String a_initFile = ConfigParameters.prefix+trainInstance+ConfigParameters.a_initFile;
				String a_dotFilePrefix = ConfigParameters.prefix+trainInstance;
				String tracepath = ConfigParameters.traces+trainInstance;
				String obspath = ConfigParameters.prefix+trainInstance+ConfigParameters.observationFile;
				String domain = ConfigParameters.domain;
				Decider decider = new Decider(domainFile, desirableStateFile, a_problemFile, a_outputPath, criticalStateFile, a_initFile, a_dotFilePrefix, ConfigParameters.a_dotFile);
				Observation obs = setObservations(obspath); //TODO: how to handle noise in trace. what counts as noise?
				LOGGER.log(Level.INFO, "Generating State Tree for ["+ ConfigParameters.domain +"] instance-> "+trainInstance);
				ArrayList<StateGraph> attackerState = generateStateGraphsForObservations(decider, domain, obs, decider.getInitialState());//generate graph for attacker and user
				LOGGER.log(Level.INFO, "Writing traces to files");
				writeTracesToFile(generateTrace(attackerState.get(0)), tracepath); //i can give the same dot file path beacause I am generating the graph for initial state only
				LOGGER.log(Level.INFO, "----Traces done---");
			}
		}
	}

	public static void main(String[] args) { 
		generateObservationTraceForScenario();
	}
}
