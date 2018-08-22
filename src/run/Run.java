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
import con.ConnectivityGraph;
import graph.StateGraph;
import landmark.RelaxedPlanningGraph;
import out.CSVGenerator;
import out.DataLine;
import out.OWDataLine;
import out.ObjectiveWeight;
import out.WeightGroup;

public class Run {
	private static final int scenario = 21;
	private static final boolean writeDOT = false;
	private static String observationFiles = "/home/sachini/BLOCKS/scenarios/"+scenario+"/obs/";
	private static String domainFile = "/home/sachini/BLOCKS/scenarios/"+scenario+"/domain.pddl";
	private static String desirableStateFile = "/home/sachini/BLOCKS/scenarios/"+scenario+"/desirable.txt"; //attacker and user do not need separate files. the state graphs are generated from the observer's point of view
	private static String criticalStateFile = "/home/sachini/BLOCKS/scenarios/"+scenario+"/critical.txt";
	private static String a_initFile = "/home/sachini/BLOCKS/scenarios/"+scenario+"/inits_a.txt";
	private static String a_problemFile = "/home/sachini/BLOCKS/scenarios/"+scenario+"/problem_a.pddl";
	private static String a_dotFilePrefix = "/home/sachini/BLOCKS/scenarios/"+scenario+"/dot/";
	private static String a_dotFileSuffix = "graph_ad_noreverse_";
	private static String u_problemFile = "/home/sachini/BLOCKS/scenarios/"+scenario+"/problem_u.pddl";
	private static String u_outputPath = "/home/sachini/BLOCKS/scenarios/"+scenario+"/outs/user/"; 
	private static String a_outputPath = "/home/sachini/BLOCKS/scenarios/"+scenario+"/outs/attacker/"; //clean this directory before running. if not graphs will be wrong
	private static String u_dotFilePrefix = "/home/sachini/BLOCKS/scenarios/"+scenario+"/dot/";
	private static String u_dotFileSuffix = "graph_ag_noreverse_";
	private static String u_initFile = "/home/sachini/BLOCKS/scenarios/"+scenario+"/inits_u.txt";
	private static String resultCSV = "/home/sachini/BLOCKS/scenarios/"+scenario+"/data/weighted/";
	private static String decisionCSV = "/home/sachini/BLOCKS/scenarios/"+scenario+"/data/decision/";
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

	public static ArrayList<StateGraph> process(ArrayList<State> states, StateGenerator gen, int config, int obFileId){
		ArrayList<StateGraph> graphs = new ArrayList<>();
		for (int i=0; i<states.size(); i++) {
			ArrayList<State> statesSeen = copyStates(states, i);
			StateGraph graphAgent = gen.enumerateStates(states.get(i), statesSeen);
			if(config==0){
				gen.graphToDOT(graphAgent,i, obFileId, writeDOT);
			}else{
				System.out.println("=======================================================================round"+i);
				StateGraph treeAgent = graphAgent.convertToTree(gen.getInitVertex(graphAgent, states.get(i)));
				gen.applyUniformProbabilitiesToStates(treeAgent, states.get(i));
				gen.graphToDOT(treeAgent, i, obFileId, writeDOT);
				graphs.add(treeAgent);
//				System.out.println(treeAgent.toString());
			}
//			break; //TODO:: REMOVE AFTER DEBUG
		}
		return graphs;
	}

	public static ArrayList<StateGraph> generateStateGraphsForObservations(Agent agent, Observation ob, InitialState init, int reverseConfig, int obFileId){
		StateGenerator gen = new StateGenerator(agent);
		ArrayList<State> state = gen.getStatesAfterObservations(ob, init, false);
		ArrayList<StateGraph> graphs = process(state, gen, reverseConfig, obFileId); //graph for attacker
		return graphs;
	}
	
	
	public static ArrayList<RelaxedPlanningGraph> getRelaxedPlanningGraph(Agent agent){
		StateGenerator gen = new StateGenerator(agent);
		ArrayList<RelaxedPlanningGraph> rpgs = gen.readRelaxedPlanningGraph();
		return rpgs;
	}

	public static ArrayList<ConnectivityGraph> getConnectivityGraph(Agent agent){
		StateGenerator gen = new StateGenerator(agent);
		ArrayList<ConnectivityGraph> cons = gen.readConnectivityGraphs();
		return cons;
	}
	
	public static void computeMetricsWeighted(Observation ob, Attacker attacker, User user, ArrayList<StateGraph> attackers, ArrayList<StateGraph> users, String filename, String owFile){
		ArrayList<String> items = new ArrayList<String>();
		ObjectiveWeight ow = new ObjectiveWeight(owFile);
		ow.assignWeights();
		for (int i=1; i<attackers.size(); i++) {
			attacker.setState(attackers.get(i)); //add stategraphs to user, attacker objects
			user.setState(users.get(i));
			Metrics metrics = new Metrics(attacker, user); //compute metrics for user, attacker
			metrics.computeMetrics();
			for (WeightGroup grp : ow.getWeights()) {
				OWDataLine data = new OWDataLine(ob.getObservations().get(i-1), metrics, grp);
				data.computeWeightedMetrics();
				data.computeObjectiveFunctionValue();
				items.add(data.toString());
			}
		}
		CSVGenerator results = new CSVGenerator(filename, items, 1);
		results.writeOutput();
	}

	public static void computeMetricsForDecisionTree(Observation ob, Attacker attacker, User user, ArrayList<StateGraph> attackers, ArrayList<StateGraph> users, RelaxedPlanningGraph arpg, ConnectivityGraph con, String filename){
		ArrayList<String> items = new ArrayList<String>();
		for (int i=1; i<attackers.size(); i++) {
			attacker.setState(attackers.get(i)); //add stategraphs to user, attacker objects
			user.setState(users.get(i));
//			System.out.println("current obs===="+ ob.getObservations().get(i-1));
			Metrics metrics = new Metrics(attacker, user); //compute metrics for user, attacker
			metrics.computeMetrics();
			metrics.computeDistanceToCrtical();
			metrics.computeDistanceToDesirable();
			metrics.computeAttackLandmarks(arpg, con);
			DataLine data = new DataLine(ob.getObservations().get(i-1), metrics);
			data.computeObjectiveFunctionValue();
			items.add(data.toString());
		}
		CSVGenerator results = new CSVGenerator(filename, items, 0);
		results.writeOutput();
	}
	
	public static void main(String[] args) { 
		int reverseConfig = 1;
		Attacker attacker = new Attacker(domainFile, desirableStateFile, a_problemFile, a_outputPath, criticalStateFile, a_initFile, a_dotFilePrefix, a_dotFileSuffix);
		User user = new User(domainFile, desirableStateFile, u_problemFile, u_outputPath, criticalStateFile, u_initFile, u_dotFilePrefix, u_dotFileSuffix);
		ArrayList<String> obFiles = getObservationFiles();
		ArrayList<RelaxedPlanningGraph> a_rpg = getRelaxedPlanningGraph(attacker);
		ArrayList<ConnectivityGraph> a_con = getConnectivityGraph(attacker);
//		ArrayList<RelaxedPlanningGraph> u_rpg = getRPGForObservations(user);
		for (String file : obFiles) {
			String name[] = file.split("/");
//			if(Integer.parseInt(name[name.length-1])==6){ //for scenario4 //DEBUG;;;; remove after fixing
//			if(file.contains("19")){ //19 for scenario 1, 20 for scenario2
				Observation obs = setObservations(file); //TODO: how to handle noise in trace. what counts as noise?
				ArrayList<StateGraph> attackerState = generateStateGraphsForObservations(attacker, obs, attacker.getInitialState(), reverseConfig, Integer.parseInt(name[name.length-1]));//generate graph for attacker and user
				ArrayList<StateGraph> userState = generateStateGraphsForObservations(user, obs, user.getInitialState(), reverseConfig, Integer.parseInt(name[name.length-1]));
				computeMetricsWeighted(obs, attacker, user, attackerState, userState, resultCSV+name[name.length-1]+".csv", owFile);
				computeMetricsForDecisionTree(obs, attacker, user, attackerState, userState, a_rpg.get(0), a_con.get(0), decisionCSV+name[name.length-1]+".csv");
//			}
		}
	}
}
