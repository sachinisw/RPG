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

	public static ArrayList<String> getObservationFiles(String obsfiles){
		ArrayList<String> obFiles = new ArrayList<String>();
		try {
			File dir = new File(obsfiles);
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

	public static ArrayList<StateGraph> process(ArrayList<State> states, StateGenerator gen, int config, int obFileId, boolean writedot){
		ArrayList<StateGraph> graphs = new ArrayList<>();
		for (int i=0; i<states.size(); i++) {
			ArrayList<State> statesSeen = copyStates(states, i);
			StateGraph graphAgent = gen.enumerateStates(states.get(i), statesSeen);
			if(config==0){
				gen.graphToDOT(graphAgent,i, obFileId, writedot);
			}else{
				System.out.println(states.get(i)+"=======================================================================round"+i);
				StateGraph treeAgent = graphAgent.convertToTree(gen.getInitVertex(graphAgent, states.get(i)));
				gen.applyUniformProbabilitiesToStates(treeAgent, states.get(i));
				gen.graphToDOT(treeAgent, i, obFileId, writedot);
				graphs.add(treeAgent);
				//				System.out.println(treeAgent.toString());
			}
			//			break; //TODO:: REMOVE AFTER DEBUG
		}
		return graphs;
	}

	public static ArrayList<StateGraph> generateStateGraphsForObservations(Agent agent, Observation ob, InitialState init, int reverseConfig, int obFileId, boolean writedot){
		StateGenerator gen = new StateGenerator(agent);
		ArrayList<State> state = gen.getStatesAfterObservations(ob, init, false);
		ArrayList<StateGraph> graphs = process(state, gen, reverseConfig, obFileId, writedot); //graph for attacker
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

	public static void computeMetricsWeighted(String domain, Observation ob, Attacker attacker, User user, ArrayList<StateGraph> attackers, ArrayList<StateGraph> users, String filename, String owFile){
		ArrayList<String> items = new ArrayList<String>();
		ObjectiveWeight ow = new ObjectiveWeight(owFile);
		ow.assignWeights();
		for (int i=1; i<attackers.size(); i++) {
			attacker.setState(attackers.get(i)); //add stategraphs to user, attacker objects
			user.setState(users.get(i));
			Metrics metrics = new Metrics(attacker, user, domain); //compute metrics for user, attacker
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

	public static void computeMetricsForDecisionTree(String domain, Observation ob, Attacker attacker, User user, ArrayList<StateGraph> attackers, 
			ArrayList<StateGraph> users, RelaxedPlanningGraph arpg, ConnectivityGraph con, String filename, String lmoutput){
		ArrayList<String> items = new ArrayList<String>();
		for (int i=1; i<attackers.size(); i++) {
			attacker.setState(attackers.get(i)); //add stategraphs to user, attacker objects
			user.setState(users.get(i));
			//			System.out.println("current obs===="+ ob.getObservations().get(i-1));
			Metrics metrics = new Metrics(attacker, user, domain); //compute metrics for user, attacker
			metrics.computeMetrics();
			metrics.computeDistanceToCrtical();
			metrics.computeDistanceToDesirable();
			metrics.computeAttackLandmarks(arpg, con, lmoutput);
			DataLine data = new DataLine(ob.getObservations().get(i-1), metrics);
			data.computeObjectiveFunctionValue();
			items.add(data.toString());
		}
		CSVGenerator results = new CSVGenerator(filename, items, 0);
		results.writeOutput();
	}

	public static void run(String domain, String domainfile, String desirablefile, String a_prob, String a_dotpre, 
			String a_out, String criticalfile, String a_init, String a_dotsuf, String u_prob, String u_out, String u_init, String u_dotpre, 
			String u_dotsuf, String obs, String wt_csv, String ds_csv, String ow, String lm_out, boolean writedot) {
		int reverseConfig = 1;
		Attacker attacker = new Attacker(domainfile, desirablefile, a_prob, a_out, criticalfile , a_init, a_dotpre, a_dotsuf);
		User user = new User(domainfile, desirablefile, u_prob, u_out, criticalfile, u_init, u_dotpre, u_dotsuf);
		ArrayList<String> obFiles = getObservationFiles(obs);
		ArrayList<RelaxedPlanningGraph> a_rpg = getRelaxedPlanningGraph(attacker);
		ArrayList<ConnectivityGraph> a_con = getConnectivityGraph(attacker);
		//		ArrayList<RelaxedPlanningGraph> u_rpg = getRPGForObservations(user);
		for (String file : obFiles) {
			String name[] = file.split("/");
			//			if(Integer.parseInt(name[name.length-1])==4){ //for scenario4 //DEBUG;;;; remove after fixing
			//			if(file.contains("19")){ //19 for scenario 1, 20 for scenario2
			//			if(file.contains("7")){ //for grid
			Observation curobs = setObservations(file); //TODO: how to handle noise in trace. what counts as noise?
			ArrayList<StateGraph> attackerState = generateStateGraphsForObservations(attacker, curobs, attacker.getInitialState(), reverseConfig, Integer.parseInt(name[name.length-1]), writedot);//generate graph for attacker and user
			ArrayList<StateGraph> userState = generateStateGraphsForObservations(user, curobs, user.getInitialState(), reverseConfig, Integer.parseInt(name[name.length-1]), writedot);
			computeMetricsWeighted(domain, curobs, attacker, user, attackerState, userState, wt_csv+name[name.length-1]+".csv", ow);
			computeMetricsForDecisionTree(domain, curobs, attacker, user, attackerState, userState, a_rpg.get(0), a_con.get(0), ds_csv+name[name.length-1]+".csv",lm_out);				//rewrites landmarks for each observation. landmarks are generated from the intial state-> goal. i dont change it when the graph is generated for the updated state. TODO: check with dw to see if a change is needed  
			//			}
		}
	}
	public static void main(String[] args) { 
		int mode = 1; //0-train, 1-test
		if(mode==0) {
			String domain = TrainConfigs.domain;
			String domainfile = TrainConfigs.domainFile;
			String desirablefile = TrainConfigs.desirableStateFile;
			String criticalfile = TrainConfigs.criticalStateFile;
			String a_prob = TrainConfigs.a_problemFile;
			String a_out = TrainConfigs.a_outputPath;
			String a_init = TrainConfigs.a_initFile;
			String a_dotpre = TrainConfigs.a_dotFilePrefix;
			String a_dotsuf = TrainConfigs.a_dotFileSuffix;
			String u_prob = TrainConfigs.u_problemFile;
			String u_out = TrainConfigs.u_outputPath;
			String u_init = TrainConfigs.u_initFile;
			String u_dotpre = TrainConfigs.u_dotFilePrefix;
			String u_dotsuf = TrainConfigs.u_dotFileSuffix;
			String wt_csv = TrainConfigs.weightedCSV;
			String ds_csv = TrainConfigs.decisionCSV;
			String ow = TrainConfigs.owFile;
			String lm_out = TrainConfigs.lmoutputFile;
			String obs = TrainConfigs.observationFiles;
			boolean writedot = TrainConfigs.writeDOT;
			run(domain, domainfile, desirablefile, a_prob, a_dotpre, 
					a_out, criticalfile, a_init, a_dotsuf, u_prob, u_out, u_init, u_dotpre, 
					u_dotsuf, obs, wt_csv, ds_csv, ow, lm_out, writedot);
		}else {
			String domain = TestConfigs.domain;
			boolean writedot = TestConfigs.writeDOT;
			for (int instance=1; instance<=3; instance++) { //3 instances
				String domainfile = TestConfigs.prefix+instance+TestConfigs.domainFile;
				String desirablefile = TestConfigs.prefix+instance+TestConfigs.desirableStateFile;
				String criticalfile = TestConfigs.prefix+instance+TestConfigs.criticalStateFile;
				String a_prob = TestConfigs.prefix+instance+TestConfigs.a_problemFile;
				String a_out = TestConfigs.prefix+instance+TestConfigs.a_outputPath;
				String a_init = TestConfigs.prefix+instance+TestConfigs.a_initFile;
				String a_dotpre = TestConfigs.prefix+instance+TestConfigs.a_dotFilePrefix;
				String a_dotsuf = TestConfigs.prefix+instance+TestConfigs.a_dotFileSuffix;
				String u_prob = TestConfigs.prefix+instance+TestConfigs.u_problemFile;
				String u_out = TestConfigs.prefix+instance+TestConfigs.u_outputPath;
				String u_init = TestConfigs.prefix+instance+TestConfigs.u_initFile;
				String u_dotpre = TestConfigs.prefix+instance+TestConfigs.u_dotFilePrefix;
				String u_dotsuf = TestConfigs.prefix+instance+TestConfigs.u_dotFileSuffix;
				String wt_csv = TestConfigs.prefix+instance+TestConfigs.weightedCSV;
				String ds_csv = TestConfigs.prefix+instance+TestConfigs.decisionCSV;
				String ow = TestConfigs.prefix+instance+TestConfigs.owFile;
				String lm_out = TestConfigs.prefix+instance+TestConfigs.lmoutputFile;
				String obs = TestConfigs.prefix+instance+TestConfigs.observationFiles;
				for (int x=0; x<TestConfigs.instanceCases; x++) { //each instance has 20 problems
					run(domain, domainfile, desirablefile, a_prob, a_dotpre, 
							a_out, criticalfile, a_init, a_dotsuf, u_prob, u_out, u_init, u_dotpre, 
							u_dotsuf, obs, wt_csv, ds_csv, ow, lm_out,writedot);
				}
			}
		}
	}
}
