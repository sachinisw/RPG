package run;

import java.util.ArrayList;

import graph.StateGraph;

public class Run {
	private static String observationFile = "/home/sachini/BLOCKS/obs_blocks.txt";
	private static String initFile = "/home/sachini/BLOCKS/inits4.txt";

	public static Observation setObservations(){
		Observation obs = new Observation();
		obs.readObservationFile(observationFile);
		return obs;
	}
	
	public static InitialState setInitialState(){
		InitialState init = new InitialState();
		init.readInitsFromFile(initFile);
		return init;
	}
	
	public static ArrayList<State> copyStates(ArrayList<State> state, int count){
		ArrayList<State> cp = new ArrayList<State>();
		for(int i=0; i<=count; i++){
			cp.add(state.get(i));
		}
		return cp;
	}
	
	public static void generateStateGraphsForObservations(Observation ob, InitialState init, int reverseConfig){
		StateGenerator gen = new StateGenerator();
		ArrayList<State> states = gen.getStatesAfterObservations(ob, init);
		for (int i=0; i<states.size(); i++) {
			ArrayList<State> statesSeen = copyStates(states, i);
			StateGraph graph = gen.enumerateStates(states.get(i), statesSeen);
			if(reverseConfig==0){
				gen.graphToDOT(graph,i);
			}else{
				//gen.graphToDOTNoUndo(graph);
				System.out.println("=======================================================================round"+i);
				StateGraph tree = graph.convertToTree(gen.getInitVertex(graph, states.get(i)));
//				tree.printMetrics();
				gen.applyUniformProbabilitiesToStates(tree, states.get(i));
				gen.graphToDOT(tree, i);
			}
//			if(i==1) break; //DEBUG:: TODO remove after fixing double arrows
		}
	}
	
	public static void main(String[] args) {
		int reverseConfig = 1;
		InitialState init = setInitialState();
		Observation obs = setObservations();
		generateStateGraphsForObservations(obs, init, reverseConfig);		
	}
}
