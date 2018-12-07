package run;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import actors.Agent;
import actors.Decider;
import con.ConnectivityGraph;
import graph.StateGraph;
import landmark.RelaxedPlanningGraph;
import out.CSVGenerator;
import out.DecisionDataLine;
import out.WeightedDataLine;
import out.ObjectiveWeight;
import out.WeightGroup;

public class Run {

	private static final Logger LOGGER = Logger.getLogger(Run.class.getName());
	private static final boolean asTraceGenerator = false;
	
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

	public static ArrayList<StateGraph> process(String domain, ArrayList<State> states, StateGenerator gen, int config, int obFileId, boolean writedot){
		ArrayList<StateGraph> graphs = new ArrayList<>();
		for (int i=0; i<states.size(); i++) {
			ArrayList<State> statesSeen = copyStates(states, i);
			StateGraph graphAgent = gen.enumerateStates(states.get(i), statesSeen);
			if(config==0){
				gen.graphToDOT(graphAgent,i, obFileId, writedot);
			}else{
				System.out.println(states.get(i)+"=======================================================================round"+i);
				StateGraph treeAgent = graphAgent.convertToTree(gen.getInitVertex(graphAgent, states.get(i)), domain);
				gen.applyUniformProbabilitiesToStates(treeAgent, states.get(i));
				gen.graphToDOT(treeAgent, i, obFileId, writedot);
				graphs.add(treeAgent);
			}
		}
		return graphs;
	}

	public static ArrayList<StateGraph> generateStateGraphsForObservations(Agent agent, String dom, Observation ob, InitialState init, int reverseConfig, int obFileId, boolean writedot, boolean fulltrace){
		StateGenerator gen = new StateGenerator(agent, dom);
		ArrayList<State> state = null;
		if(fulltrace) {
			state = gen.getStatesAfterObservations(ob, init, asTraceGenerator);
			ArrayList<StateGraph> graphs = process(dom, state, gen, reverseConfig, obFileId, writedot); //graph for attacker
			return graphs;
		}else {
			ArrayList<String> cleaned = new ArrayList<String>();
			int starpos = 0;
			for (String o : ob.getObservations()) { //remove the * at the start
				if(o.contains("*")) {
					starpos++;
					cleaned.add(o.substring(1));
				}else {
					cleaned.add(o);
				}
			}
			Observation cleanOb = new Observation();
			cleanOb.setObservations(cleaned);
			state = gen.getStatesAfterObservations(cleanOb, init, asTraceGenerator);
			List<State> l = state.subList(starpos, state.size());
			ArrayList<State> substates = new ArrayList<State>();
			substates.addAll(l);
			ArrayList<StateGraph> graphs = process(dom, substates, gen, reverseConfig, obFileId, writedot);
			return graphs;
		}
	}


	public static ArrayList<RelaxedPlanningGraph> getRelaxedPlanningGraph(Agent agent, String dom){
		StateGenerator gen = new StateGenerator(agent, dom);
		ArrayList<RelaxedPlanningGraph> rpgs = gen.readRelaxedPlanningGraph();
		return rpgs;
	}

	public static ArrayList<ConnectivityGraph> getConnectivityGraph(Agent agent, String dom){
		StateGenerator gen = new StateGenerator(agent, dom);
		ArrayList<ConnectivityGraph> cons = gen.readConnectivityGraphs();
		return cons;
	}

	public static void computeMetricsWeighted(String domain, Observation ob, Decider attacker, /*User user,*/ ArrayList<StateGraph> attackers, 
			/*ArrayList<StateGraph> users,*/ String filename, String owFile){
		ArrayList<String> items = new ArrayList<String>();
		ObjectiveWeight ow = new ObjectiveWeight(owFile);
		ow.assignWeights();
		for (int i=1; i<attackers.size(); i++) {
			attacker.setState(attackers.get(i)); //add stategraphs to user, attacker objects
			Metrics metrics = new Metrics(attacker, domain); //compute metrics for user, attacker
			metrics.computeCRD();
			for (WeightGroup grp : ow.getWeights()) {
				WeightedDataLine data = new WeightedDataLine(ob.getObservations().get(i-1), metrics, grp);
				data.computeWeightedMetrics();
				data.computeObjectiveFunctionValue();
				items.add(data.toString());
			}
		}
		CSVGenerator results = new CSVGenerator(filename, items, 1);
		results.writeOutput();
	}

	public static void computeMetricsForDecisionTree(String domain, Observation ob, Decider attacker, ArrayList<StateGraph> attackers, 
			RelaxedPlanningGraph arpg, ConnectivityGraph con, String filename, String lmoutput){
		ArrayList<String> items = new ArrayList<String>();
		for (int i=1; i<attackers.size(); i++) {
			attacker.setState(attackers.get(i)); //add stategraphs to user, attacker objects
			Metrics metrics = new Metrics(attacker, domain); //compute features for decision tree
			metrics.computeCRD();
			metrics.computeDistanceMetrics();
			metrics.generateAttackerLandmarks(arpg, con, lmoutput);
			metrics.computeAttackLandmarksRemaining();
			metrics.percentOfLandmarksInState();
			DecisionDataLine data = new DecisionDataLine(ob.getObservations().get(i-1), metrics);
			data.computeObjectiveFunctionValue();
			items.add(data.toString());
		}
		CSVGenerator results = new CSVGenerator(filename, items, 0);
		results.writeOutput();
	}

	public static void run(String domain, String domainfile, String desirablefile, String a_prob, String a_dotpre, 
			String a_out, String criticalfile, String a_init, String a_dotsuf, String u_prob, String u_out, String u_init, 
			String u_dotsuf, String obs, String wt_csv, String ds_csv, String ow, String lm_out, boolean writedot, boolean full) {
		int reverseConfig = 1;
		Decider decider = new Decider(domain, domainfile, desirablefile, a_prob, a_out, criticalfile , a_init, a_dotpre, a_dotsuf);
		ArrayList<String> obFiles = getObservationFiles(obs);
		ArrayList<RelaxedPlanningGraph> a_rpg = getRelaxedPlanningGraph(decider, domain);
		ArrayList<ConnectivityGraph> a_con = getConnectivityGraph(decider, domain);
		for (String file : obFiles) {
			String name[] = file.split("/");
//						if(Integer.parseInt(name[name.length-1])==70){ //DEBUG;;;; remove after fixing
			LOGGER.log(Level.INFO, "Processing observation file: "+name[name.length-1]);
			Observation curobs = setObservations(file); //TODO: how to handle noise in trace. what counts as noise?
			LOGGER.log(Level.INFO, "Generating attacker state graphs for domain: "+ domain);
			ArrayList<StateGraph> attackerState = generateStateGraphsForObservations(decider, domain, curobs, decider.getInitialState(), reverseConfig, Integer.parseInt(name[name.length-1]), writedot, full);//generate graph for attacker and user
			if(full) {
				computeMetricsWeighted(domain, curobs, decider, attackerState, wt_csv+name[name.length-1]+".csv", ow);
				computeMetricsForDecisionTree(domain, curobs, decider, attackerState, a_rpg.get(0), a_con.get(0), ds_csv+name[name.length-1]+".csv",lm_out);				//rewrites landmarks for each observation. landmarks are generated from the intial state-> goal. i dont change it when the graph is generated for the updated state. TODO: check with dw to see if a change is needed
			}else {
				Observation cleaned = new Observation();
				ArrayList<String> cl = new ArrayList<>();
				for (String o : curobs.getObservations()) {
					if(!o.contains("*")) {
						cl.add(o);
					}
				}
				cleaned.setObservations(cl);
				computeMetricsWeighted(domain, cleaned, decider, attackerState, wt_csv+name[name.length-1]+"lm.csv", ow);
				computeMetricsForDecisionTree(domain, cleaned, decider, attackerState, a_rpg.get(0), a_con.get(0), ds_csv+name[name.length-1]+"lm.csv",lm_out);				//rewrites landmarks for each observation. landmarks are generated from the intial state-> goal. i dont change it when the graph is generated for the updated state. TODO: check with dw to see if a change is needed
			}
//						}
		}
	}
	
	public static void main(String[] args) { 
		int mode = 0; //-1=sample train 0=train, 1=test TODO README:: CHANGE CONFIGS HERE FIRST WHEN TRAINING 
		if(mode==TrainConfigs.runmode) {
			LOGGER.log(Level.INFO, "Run mode: TRAINING");
			String domain = TrainConfigs.domain;
			for (int casenum=0; casenum<TrainConfigs.cases; casenum++) {
				String domainfile = TrainConfigs.root+casenum+TrainConfigs.domainFile;
				String desirablefile = TrainConfigs.root+casenum+TrainConfigs.dstates;
				String criticalfile = TrainConfigs.root+casenum+TrainConfigs.cstates;
				String dotpre = TrainConfigs.root+casenum+TrainConfigs.dotdir;
				String a_prob = TrainConfigs.root+casenum+TrainConfigs.a_problemFile;
				String a_out = TrainConfigs.root+casenum+TrainConfigs.outsdir+TrainConfigs.a_output;
				String a_init = TrainConfigs.root+casenum+TrainConfigs.a_initFile;
				String a_dotsuf = TrainConfigs.a_dotFileSuffix;
				String u_prob = TrainConfigs.root+casenum+TrainConfigs.u_problemFile;
				String u_out = TrainConfigs.root+casenum+TrainConfigs.outsdir+TrainConfigs.u_output;
				String u_init = TrainConfigs.root+casenum+TrainConfigs.u_initFile;
				String u_dotsuf = TrainConfigs.u_dotFileSuffix;
				String wt_csv = TrainConfigs.root+casenum+TrainConfigs.datadir+TrainConfigs.weightedCSV;
				String ds_csv = TrainConfigs.root+casenum+TrainConfigs.datadir+TrainConfigs.decisionCSV;
				String ow = TrainConfigs.owFile;
				String lm_out = TrainConfigs.root+casenum+TrainConfigs.datadir+TrainConfigs.lmoutputFile;
				String obs = TrainConfigs.root+casenum+TrainConfigs.obsdir;
				boolean writedot = TrainConfigs.writeDOT;
				System.out.println(domainfile);
				System.out.println(desirablefile);
				System.out.println(a_prob);
				System.out.println(dotpre);
				System.out.println(a_out);
				System.out.println(criticalfile);
				System.out.println(a_init);
				System.out.println(a_dotsuf);
				System.out.println(u_prob);
				System.out.println(u_out);
				System.out.println(u_init);
				System.out.println(u_dotsuf);
				System.out.println(obs);
				System.out.println(wt_csv);
				System.out.println(ds_csv);
				System.out.println(lm_out);
				run(domain, domainfile, desirablefile, a_prob, dotpre, 
						a_out, criticalfile, a_init, a_dotsuf, u_prob, u_out, u_init, 
						u_dotsuf, obs, wt_csv, ds_csv, ow, lm_out, writedot, true);
				break;
			}
			LOGGER.log(Level.INFO, "Completed trained model for domain:" + domain);
		}else {
			String domain = TestConfigs.domain;
			LOGGER.log(Level.INFO, "Run mode: TESTING domain ["+ domain +"]");
			boolean writedot = TestConfigs.writeDOT; //TODO README:: CHANGE instance number HERE FIRST WHEN TESTING 
			for (int instance=2; instance<=TestConfigs.instances; instance++) { //blocks-3, navigator-3 easyipc-3, ferry-3 instances
				for (int x=0; x<TestConfigs.instanceCases; x++) { //blocks,navigator,easyipc, ferry -each instance has 20 problems
					String domainfile = TestConfigs.prefix+TestConfigs.instancedir+String.valueOf(instance)+TestConfigs.instscenario+String.valueOf(x)+TestConfigs.domainFile;
					String desirablefile = TestConfigs.prefix+TestConfigs.instancedir+String.valueOf(instance)+TestConfigs.instscenario+String.valueOf(x)+TestConfigs.desirableStateFile;
					String criticalfile = TestConfigs.prefix+TestConfigs.instancedir+String.valueOf(instance)+TestConfigs.instscenario+String.valueOf(x)+TestConfigs.criticalStateFile;
					String a_prob = TestConfigs.prefix+TestConfigs.instancedir+String.valueOf(instance)+TestConfigs.instscenario+String.valueOf(x)+TestConfigs.a_problemFile;
					String a_out = TestConfigs.prefix+TestConfigs.instancedir+String.valueOf(instance)+TestConfigs.instscenario+String.valueOf(x)+TestConfigs.a_outputPath;
					String a_init = TestConfigs.prefix+TestConfigs.instancedir+String.valueOf(instance)+TestConfigs.instscenario+String.valueOf(x)+TestConfigs.a_initFile;
					String a_dotpre_full = TestConfigs.prefix+TestConfigs.instancedir+String.valueOf(instance)+TestConfigs.instscenario+String.valueOf(x)+TestConfigs.a_dotFilePrefix;
					String a_dotpre_lm = TestConfigs.prefix+TestConfigs.instancedir+String.valueOf(instance)+TestConfigs.instscenario+String.valueOf(x)+TestConfigs.a_dotFileLMPrefix;
					String a_dotsuf = TestConfigs.a_dotFileSuffix;
					String u_prob = TestConfigs.prefix+TestConfigs.instancedir+String.valueOf(instance)+TestConfigs.instscenario+String.valueOf(x)+TestConfigs.u_problemFile;
					String u_out = TestConfigs.prefix+TestConfigs.instancedir+String.valueOf(instance)+TestConfigs.instscenario+String.valueOf(x)+TestConfigs.u_outputPath;
					String u_init = TestConfigs.prefix+TestConfigs.instancedir+String.valueOf(instance)+TestConfigs.instscenario+String.valueOf(x)+TestConfigs.u_initFile;
//					String u_dotpre_full = TestConfigs.prefix+TestConfigs.instancedir+String.valueOf(instance)+TestConfigs.instscenario+String.valueOf(x)+TestConfigs.u_dotFilePrefix;
//					String u_dotpre_lm = TestConfigs.prefix+TestConfigs.instancedir+String.valueOf(instance)+TestConfigs.instscenario+String.valueOf(x)+TestConfigs.u_dotFileLMPrefix;
					String u_dotsuf = TestConfigs.u_dotFileSuffix;
					String wt_csv = TestConfigs.prefix+TestConfigs.instancedir+String.valueOf(instance)+TestConfigs.instscenario+String.valueOf(x)+TestConfigs.weightedCSV;
					String ds_csv = TestConfigs.prefix+TestConfigs.instancedir+String.valueOf(instance)+TestConfigs.instscenario+String.valueOf(x)+TestConfigs.decisionCSV;
					String lm_out_full = TestConfigs.prefix+TestConfigs.instancedir+String.valueOf(instance)+TestConfigs.instscenario+String.valueOf(x)+TestConfigs.lmoutputFull;
					String lm_out_short = TestConfigs.prefix+TestConfigs.instancedir+String.valueOf(instance)+TestConfigs.instscenario+String.valueOf(x)+TestConfigs.lmoutputShort;
					String obs = TestConfigs.prefix+TestConfigs.instancedir+String.valueOf(instance)+TestConfigs.instscenario+String.valueOf(x)+TestConfigs.observationFiles;
					String obslm = TestConfigs.prefix+TestConfigs.instancedir+String.valueOf(instance)+TestConfigs.instscenario+String.valueOf(x)+TestConfigs.observationLMFiles; 
					String ow = TestConfigs.owFile;
					run(domain, domainfile, desirablefile, a_prob, a_dotpre_full, 
							a_out, criticalfile, a_init, a_dotsuf, u_prob, u_out, u_init, 
							u_dotsuf, obs, wt_csv, ds_csv, ow, lm_out_full, writedot, true);
					LOGGER.log(Level.INFO, "Finished full case: "+ x +" for test instance:" +instance );
					run(domain, domainfile, desirablefile, a_prob, a_dotpre_lm, 
							a_out, criticalfile, a_init, a_dotsuf, u_prob, u_out, u_init, 
							u_dotsuf, obslm, wt_csv, ds_csv, ow, lm_out_short, writedot, false);
					LOGGER.log(Level.INFO, "Finished reduced case: "+ x +" for test instance:" +instance );
				}
				LOGGER.log(Level.INFO, "Test instance: "+ instance + " done" );
			}
		}
	}
}
