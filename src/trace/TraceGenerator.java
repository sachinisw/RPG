package trace;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

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
	private static String observationFile = "/home/sachini/BLOCKS/scenarios/1/obs_blocks.txt";
	private static String domainFile = "/home/sachini/BLOCKS/scenarios/1/domain.pddl";
	private static String desirableStateFile = "/home/sachini/BLOCKS/scenarios/1/desirable.txt"; //attacker and user do not need separate files. the state graphs are generated from the observer's point of view
	private static String criticalStateFile = "/home/sachini/BLOCKS/scenarios/1/critical.txt";
	private static String a_initFile = "/home/sachini/BLOCKS/scenarios/1/inits4.txt";
	private static String a_problemFile = "/home/sachini/BLOCKS/scenarios/1/problem_4.pddl";
	private static String a_dotFilePrefix = "/home/sachini/BLOCKS/graph_ad_noreverse_";
	private static String u_problemFile = "/home/sachini/BLOCKS/scenarios/1/problem_3.pddl";
	private static String u_outputPath = "/home/sachini/BLOCKS/outs/user/"; 
	private static String a_outputPath = "/home/sachini/BLOCKS/outs/attacker/"; //clean this directory before running. if not graphs will be wrong
	private static String u_dotFilePrefix = "/home/sachini/BLOCKS/graph_ag_noreverse_";
	private static String u_initFile = "/home/sachini/BLOCKS/scenarios/1/inits3.txt";
	private static String traces = "/home/sachini/BLOCKS/traces/";

	//this can be any observation sequence. For this one I only want the initial state graphs for attacker and user.
	public static Observation setObservations(){
		Observation obs = new Observation();
		obs.readObservationFile(observationFile);
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
		return graphs;
	}

	public static ArrayList<StateGraph> generateStateGraphsForObservations(Agent agent, Observation ob, InitialState init){
		StateGenerator gen = new StateGenerator(agent);
		ArrayList<State> state = gen.getStatesAfterObservations(ob, init);
		ArrayList<StateGraph> graphs = process(state, gen); //graph for attacker
		return graphs;
	}

	public static void generateTrace(StateGraph attacker, StateGraph user){
		ArrayList<ArrayList<String>> trace = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<StateVertex>> us = user.getAllPathsFromRoot();
		ArrayList<ArrayList<StateVertex>> at = attacker.getAllPathsFromRoot();
		for(int i=0; i<us.size(); i++){
			ArrayList<StateVertex> list = us.get(i);
			ArrayList<String> trc = new ArrayList<String>();
			for(int j=0; j<list.size()-1; j++){
				//				System.out.println(list.get(j));
				ArrayList<ActionEdge> actions = user.findEdgeForStateTransition(list.get(j), list.get(j+1));
				for (ActionEdge actionEdge : actions) {
					trc.add(actionEdge.getAction());
				}
			}
			trace.add(trc);
		}
		for(int i=0; i<at.size(); i++){
			ArrayList<StateVertex> list = at.get(i);
			ArrayList<String> trc = new ArrayList<String>();
			for(int j=0; j<list.size()-1; j++){
				ArrayList<ActionEdge> actions = attacker.findEdgeForStateTransition(list.get(j), list.get(j+1));
				for (ActionEdge actionEdge : actions) {
					trc.add(actionEdge.getAction());
				}
			}
			trace.add(trc);
		}
		writeTracesToFile(trace);
		System.out.println("*******************************************************");
		System.out.println(trace);
		System.out.println("*******************************************************");

	}

	public static void writeTracesToFile(ArrayList<ArrayList<String>> traceset){
		PrintWriter writer = null;
		for (int i = 0; i < traceset.size(); i++) {
			ArrayList<String> tr = traceset.get(i);
			try{
				writer = new PrintWriter(traces+i, "UTF-8");
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

	public static void main(String[] args) { 
		Attacker attacker = new Attacker(domainFile, desirableStateFile, a_problemFile, a_outputPath, criticalStateFile, a_initFile, a_dotFilePrefix);
		User user = new User(domainFile, desirableStateFile, u_problemFile, u_outputPath, criticalStateFile, u_initFile, u_dotFilePrefix);
		Observation obs = setObservations(); //TODO: how to handle noise in trace. what counts as noise?
		ArrayList<StateGraph> attackerState = generateStateGraphsForObservations(attacker, obs, attacker.setInitialState());//generate graph for attacker and user
		ArrayList<StateGraph> userState = generateStateGraphsForObservations(user, obs, user.setInitialState());
		generateTrace(attackerState.get(0), userState.get(0));
	}
}
