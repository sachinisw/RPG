package run;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import actors.Agent;
import actors.Attacker;
import actors.User;
import graph.StateGraph;
import out.CSVGenerator;
import out.DataLine;
import out.ObjectiveWeight;
import out.WeightGroup;

public class Run {
	private static String observationFiles = "/home/sachini/BLOCKS/scenarios/1/obs/";
	private static String domainFile = "/home/sachini/BLOCKS/scenarios/1/domain.pddl";
	private static String desirableStateFile = "/home/sachini/BLOCKS/scenarios/1/desirable.txt"; //attacker and user do not need separate files. the state graphs are generated from the observer's point of view
	private static String criticalStateFile = "/home/sachini/BLOCKS/scenarios/1/critical.txt";
	private static String a_initFile = "/home/sachini/BLOCKS/scenarios/1/inits4.txt";
	private static String a_problemFile = "/home/sachini/BLOCKS/scenarios/1/problem_4.pddl";
	private static String a_dotFilePrefix = "/home/sachini/BLOCKS/scenarios/1/dot/graph_ad_noreverse_";
	private static String u_problemFile = "/home/sachini/BLOCKS/scenarios/1/problem_3.pddl";
	private static String u_outputPath = "/home/sachini/BLOCKS/outs/user/"; 
	private static String a_outputPath = "/home/sachini/BLOCKS/outs/attacker/"; //clean this directory before running. if not graphs will be wrong
	private static String u_dotFilePrefix = "/home/sachini/BLOCKS/scenarios/1/dot/graph_ag_noreverse_";
	private static String u_initFile = "/home/sachini/BLOCKS/scenarios/1/inits3.txt";
	private static String resultCSV = "/home/sachini/BLOCKS/scenarios/1/data/";
	private static String owFile = "/home/sachini/BLOCKS/configs/ow_short.config";

	public static ArrayList<String> getObservationFiles(){
		ArrayList<String> obFiles = new ArrayList<String>();
		try {
			File dir = new File(observationFiles);
			List<File> files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
			for (File fileItem : files) {
				obFiles.add(fileItem.getCanonicalPath());
			}
		}catch (IOException e) {
			e.printStackTrace();
		} 
		return obFiles;	
	}

	public static Observation setObservations(String obFile){
		Observation obs = new Observation();
		obs.readObservationFile(obFile);
		return obs;
	}

	public static ArrayList<State> copyStates(ArrayList<State> state, int count){
		ArrayList<State> cp = new ArrayList<State>();
		for(int i=0; i<=count; i++){
			cp.add(state.get(i));
		}
		return cp;
	}

	public static ArrayList<StateGraph> process(ArrayList<State> states, StateGenerator gen, int config){
		ArrayList<StateGraph> graphs = new ArrayList<>();
		for (int i=0; i<states.size(); i++) {
			ArrayList<State> statesSeen = copyStates(states, i);
			StateGraph graphAgent = gen.enumerateStates(states.get(i), statesSeen);
			if(config==0){
				gen.graphToDOT(graphAgent,i);
			}else{
				System.out.println("=======================================================================round"+i);
				StateGraph treeAgent = graphAgent.convertToTree(gen.getInitVertex(graphAgent, states.get(i)));
				//				tree.printMetrics(); gen.graphToDOTNoUndo(graph);
				gen.applyUniformProbabilitiesToStates(treeAgent, states.get(i));
				gen.graphToDOT(treeAgent, i);
				graphs.add(treeAgent);
				System.out.println(treeAgent.toString());
			}
		}
		return graphs;
	}

	public static ArrayList<StateGraph> generateStateGraphsForObservations(Agent agent, Observation ob, InitialState init, int reverseConfig){
		StateGenerator gen = new StateGenerator(agent);
		ArrayList<State> state = gen.getStatesAfterObservations(ob, init);
		ArrayList<StateGraph> graphs = process(state, gen, reverseConfig); //graph for attacker
		return graphs;
	}

	public static void computeMetrics(Observation ob, Attacker attacker, User user, ArrayList<StateGraph> attackers, ArrayList<StateGraph> users, String filename, String owFile){
		ArrayList<String> items = new ArrayList<String>();
		ObjectiveWeight ow = new ObjectiveWeight(owFile);
		ow.assignWeights();
		for (int i=1; i<attackers.size(); i++) {
			attacker.setState(attackers.get(i)); //add stategraphs to user, attacker objects
			user.setState(users.get(i));
			Metrics metrics = new Metrics(attacker, user); //compute metrics for user, attacker
			metrics.computeMetrics();
			for (WeightGroup grp : ow.getWeights()) {
				DataLine data = new DataLine(ob.getObservations().get(i-1), metrics, grp);
				data.computeWeightedMetrics();
				data.computeObjectiveFunctionValue();
				items.add(data.toString());
			}
		}
		CSVGenerator results = new CSVGenerator(filename, items);
		results.writeOutput();
	}

	public static void main(String[] args) { 
		int reverseConfig = 1;
		Attacker attacker = new Attacker(domainFile, desirableStateFile, a_problemFile, a_outputPath, criticalStateFile, a_initFile, a_dotFilePrefix);
		User user = new User(domainFile, desirableStateFile, u_problemFile, u_outputPath, criticalStateFile, u_initFile, u_dotFilePrefix);
		ArrayList<String> obFiles = getObservationFiles();
		for (String file : obFiles) {
			if(file.contains("6")){
				Observation obs = setObservations(file); //TODO: how to handle noise in trace. what counts as noise?
				String name[] = file.split("/");
				ArrayList<StateGraph> attackerState = generateStateGraphsForObservations(attacker, obs, attacker.setInitialState(), reverseConfig);//generate graph for attacker and user
				ArrayList<StateGraph> userState = generateStateGraphsForObservations(user, obs, user.setInitialState(), reverseConfig);
				computeMetrics(obs, attacker, user, attackerState, userState, resultCSV+name[name.length-1]+".csv", owFile);
			}
		}
	}
}
