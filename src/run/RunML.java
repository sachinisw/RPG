package run;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
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

public class RunML {
	/**
	 * Enumerate the full state space for the observer. Extract Feature values risk, desirability, distances to undesirable/desirable, landmarks
	 * Learn a decision tree. Predict intervention in small problems (few blocks, 1-good 1 bad) [DONE]
	 */
	private static final Logger LOGGER = Logger.getLogger(RunML.class.getName());
	private static final boolean asTraceGenerator = false;

	public static TreeSet<String> getObservationFiles(String obsfiles){
		TreeSet<String> obFiles = new TreeSet<String>();
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

	public static ArrayList<StateGraph> process(String domain, ArrayList<State> states, StateGenerator gen, int config, 
			String obFileId, boolean writedot){
		ArrayList<StateGraph> graphs = new ArrayList<>();
		for (int i=0; i<states.size(); i++) {
			ArrayList<State> statesSeen = copyStates(states, i);
			StateGraph graphAgent = gen.enumerateStates(states.get(i), statesSeen);
			if(config==0){
				gen.graphToDOT(graphAgent,i, obFileId, writedot);
			}else{
				System.out.println(states.get(i)+"=======================================================================observation"+i);
				StateGraph treeAgent = graphAgent.convertToTree(gen.getInitVertex(graphAgent, states.get(i)), domain);
				gen.applyUniformProbabilitiesToStates(treeAgent, states.get(i));
				gen.graphToDOT(treeAgent, i, obFileId, writedot);
				graphs.add(treeAgent);
			}
		}
		return graphs;
	}

	public static ArrayList<StateGraph> generateStateGraphsForObservations(Agent agent, String dom, Observation ob, InitialState init, int reverseConfig, 
			String obFileId, boolean writedot, boolean fulltrace){
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

	public static void computeMetricsForDecisionTree(String domain, Observation ob, Decider decider, ArrayList<StateGraph> trees, 
			RelaxedPlanningGraph arpg, ConnectivityGraph con, String outputfilename, String lmoutput){
		ArrayList<String> items = new ArrayList<String>();
		for (int i=1; i<trees.size(); i++) { //skip the first one. It's the initial state or in the case of half traces, it's the state before I start making decisions
			decider.setState(trees.get(i)); //add stategraphs to user, attacker objects
			Metrics metrics = new Metrics(decider, domain, lmoutput, arpg, con); //compute features for decision tree
			metrics.computeFeatureSet();
			DecisionDataLine data = new DecisionDataLine(ob.getObservations().get(i-1), metrics);
			data.computeObjectiveFunctionValue();
			items.add(data.toString());
		}
		CSVGenerator results = new CSVGenerator(outputfilename, items, 0);
		results.writeOutput();
	}
	
	private static boolean restrict(int mode, int limit, String domain) {
		if((domain.equalsIgnoreCase("EASYIPC") && mode==TestConfigsML.runmodeTest && limit>TestConfigsML.fileLimit) ||
				(domain.equalsIgnoreCase("NAVIGATOR") && mode==TestConfigsML.runmodeTest && limit>TestConfigsML.fileLimit) || 
				(domain.equalsIgnoreCase("FERRY") && mode==TestConfigsML.runmodeTest && limit>TestConfigsML.fileLimit) ) { //when testing the trained model in EASYIPC, pick only 10 observation files from each cs/ds pair in current test instance
			return true;
		}else {
			if( (domain.equalsIgnoreCase("EASYIPC") && mode==TrainConfigsML.runmodeTrain && limit>TrainConfigsML.fileLimit) || 
					(domain.equalsIgnoreCase("FERRY") && mode==TrainConfigsML.runmodeTrain && limit>TrainConfigsML.fileLimit) ||
					(domain.equalsIgnoreCase("NAVIGATOR") && mode==TrainConfigsML.runmodeTrain && limit>TrainConfigsML.fileLimit)) { //when training navigator (creates too many instances), put a limit on num of observation files (100)
				return true;
			}
		}
		return false;
	}

	public static void run(int mode, String domain, String domainfile, String desirablefile, String a_prob, String a_dotpre, 
			String a_out, String criticalfile, String a_init, String a_dotsuf, String obs, 
			String ds_csv, String lm_out, int delay, boolean writedot, boolean full) {
		int reverseConfig = 1;
		Decider decider = new Decider(domain, domainfile, desirablefile, a_prob, a_out, criticalfile , a_init, a_dotpre, a_dotsuf);
		TreeSet<String> obFiles = getObservationFiles(obs);
		ArrayList<RelaxedPlanningGraph> a_rpg = getRelaxedPlanningGraph(decider, domain);
		ArrayList<ConnectivityGraph> a_con = getConnectivityGraph(decider, domain);
		int obFileLimit = 1; 
		for (String file : obFiles) { 
			if (restrict(mode, obFileLimit, domain)) {
				break;
			}
			obFileLimit++;
			String name[] = file.split("/");
			Observation curobs = setObservations(file); //TODO: how to handle noise in trace.
			LOGGER.log(Level.INFO, "Generating attacker state graphs for domain: "+ domain);
			ArrayList<StateGraph> attackerState = generateStateGraphsForObservations(decider, domain, curobs, decider.getInitialState(), reverseConfig, 
					name[name.length-1], writedot, full);//generate graph for attacker and user
			if(full) {
				LOGGER.log(Level.INFO, "Processing FULL observation file: "+ file);
				computeMetricsForDecisionTree(domain, curobs, decider, attackerState, a_rpg.get(0), a_con.get(0), 
						ds_csv+name[name.length-1]+".csv",lm_out); //rewrites landmarks for each observation. landmarks are generated from the intial state-> goal. i dont change it when the graph is generated for the updated state.
			}else {
				LOGGER.log(Level.INFO, "Processing PARTIAL observation file: "+ file);
				Observation cleaned = new Observation();
				ArrayList<String> cl = new ArrayList<>();
				for (String o : curobs.getObservations()) {
					if(!o.contains("*")) { //add observations that does not have a *. the others are the ones that can be skipped
						cl.add(o);
					}
				}
				cleaned.setObservations(cl);
				computeMetricsForDecisionTree(domain, cleaned, decider, attackerState, a_rpg.get(0), a_con.get(0), 
						ds_csv+name[name.length-1]+"lm"+String.valueOf(delay)+".csv",lm_out); //file path -- scenarios/x/data/decision/0_lm50.csv
			}
		}
	}

	public static void runAsTraining(int mode) {
		LOGGER.log(Level.INFO, "Run mode: TRAINING with "+ TrainConfigsML.cases + " cases");
		String domain = TrainConfigsML.domain;
		for (int casenum=0; casenum<TrainConfigsML.cases; casenum++) {
			String domainfile = TrainConfigsML.root+casenum+TrainConfigsML.domainFile;
			String desirablefile = TrainConfigsML.root+casenum+TrainConfigsML.dstates;
			String criticalfile = TrainConfigsML.root+casenum+TrainConfigsML.cstates;
			String dotpre = TrainConfigsML.root+casenum+TrainConfigsML.dotdir;
			String a_prob = TrainConfigsML.root+casenum+TrainConfigsML.a_problemFile;
			String a_out = TrainConfigsML.root+casenum+TrainConfigsML.outsdir+TrainConfigsML.a_output;
			String a_init = TrainConfigsML.root+casenum+TrainConfigsML.a_initFile;
			String a_dotsuf = TrainConfigsML.a_dotFileSuffix;
			String ds_csv = TrainConfigsML.root+casenum+TrainConfigsML.datadir+TrainConfigsML.decisionCSV;
			String lm_out = TrainConfigsML.root+casenum+TrainConfigsML.datadir+TrainConfigsML.lmoutputFile;
			String obs = TrainConfigsML.root+casenum+TrainConfigsML.obsdir;
			boolean writedot = TrainConfigsML.writeDOT;
			run(mode, domain, domainfile, desirablefile, a_prob, dotpre, 
					a_out, criticalfile, a_init, a_dotsuf, obs, ds_csv, lm_out, 0, writedot, true);
		}
		LOGGER.log(Level.INFO, "Completed data generation to train a model for domain:" + domain);
	}

	public static void runAsDebug(int mode) {
		LOGGER.log(Level.INFO, "Run mode: DEBUG for scenario "+ DebugConfigsML.scenario);
		String domain = DebugConfigsML.domain;
		String domainfile = DebugConfigsML.root+DebugConfigsML.domainFile;
		String desirablefile = DebugConfigsML.root+DebugConfigsML.dstates;
		String criticalfile = DebugConfigsML.root+DebugConfigsML.cstates;
		String dotpre = DebugConfigsML.root+DebugConfigsML.dotdir;
		String a_prob = DebugConfigsML.root+DebugConfigsML.a_problemFile;
		String a_out = DebugConfigsML.root+DebugConfigsML.outsdir+DebugConfigsML.a_output;
		String a_init = DebugConfigsML.root+DebugConfigsML.a_initFile;
		String a_dotsuf = DebugConfigsML.a_dotFileSuffix;
		String ds_csv = DebugConfigsML.root+DebugConfigsML.datadir+DebugConfigsML.decisionCSV;
		String lm_out = DebugConfigsML.root+DebugConfigsML.datadir+DebugConfigsML.lmoutputFile;
		String obs = DebugConfigsML.root+DebugConfigsML.obsdir;
		boolean writedot = DebugConfigsML.writeDOT;
		run(mode, domain, domainfile, desirablefile, a_prob, dotpre, 
				a_out, criticalfile, a_init, a_dotsuf, obs, ds_csv, lm_out, 0, writedot, true);
		LOGGER.log(Level.INFO, "Completed data generation to train a model for domain:" + domain);
	}

	public static void runAsTesting(int mode, int start) {
		String domain = TestConfigsML.domain;
		boolean writedot = TestConfigsML.writeDOT; 
		LOGGER.log(Level.INFO, "Run mode: TESTING domain ["+ domain +"]");
		for (int instance=start; instance<=TestConfigsML.instances; instance++) { //blocks-3, navigator-3 easyipc-3, ferry-3 instances
			for (int x=0; x<TestConfigsML.instanceCases; x++) { //blocks,navigator,easyipc, ferry -each instance has 20 problems
				String domainfile = TestConfigsML.prefix+TestConfigsML.instancedir+String.valueOf(instance)+TestConfigsML.instscenario+String.valueOf(x)+TestConfigsML.domainFile;
				String desirablefile = TestConfigsML.prefix+TestConfigsML.instancedir+String.valueOf(instance)+TestConfigsML.instscenario+String.valueOf(x)+TestConfigsML.desirableStateFile;
				String criticalfile = TestConfigsML.prefix+TestConfigsML.instancedir+String.valueOf(instance)+TestConfigsML.instscenario+String.valueOf(x)+TestConfigsML.criticalStateFile;
				String a_prob = TestConfigsML.prefix+TestConfigsML.instancedir+String.valueOf(instance)+TestConfigsML.instscenario+String.valueOf(x)+TestConfigsML.a_problemFile;
				String a_out = TestConfigsML.prefix+TestConfigsML.instancedir+String.valueOf(instance)+TestConfigsML.instscenario+String.valueOf(x)+TestConfigsML.a_outputPath;
				String a_init = TestConfigsML.prefix+TestConfigsML.instancedir+String.valueOf(instance)+TestConfigsML.instscenario+String.valueOf(x)+TestConfigsML.a_initFile;
				String a_dotpre_full = TestConfigsML.prefix+TestConfigsML.instancedir+String.valueOf(instance)+TestConfigsML.instscenario+String.valueOf(x)+TestConfigsML.a_dotFilePrefix;
				String a_dotsuf = TestConfigsML.a_dotFileSuffix;
				String ds_csv = TestConfigsML.prefix+TestConfigsML.instancedir+String.valueOf(instance)+TestConfigsML.instscenario+String.valueOf(x)+TestConfigsML.decisionCSV;
				String lm_out_full = TestConfigsML.prefix+TestConfigsML.instancedir+String.valueOf(instance)+TestConfigsML.instscenario+String.valueOf(x)+TestConfigsML.lmoutputFull;
				String obs = TestConfigsML.prefix+TestConfigsML.instancedir+String.valueOf(instance)+TestConfigsML.instscenario+String.valueOf(x)+TestConfigsML.observationFiles;
				run(mode, domain, domainfile, desirablefile, a_prob, a_dotpre_full, a_out, criticalfile, a_init, a_dotsuf, obs, ds_csv, lm_out_full, 0, writedot, true);
				LOGGER.log(Level.INFO, "Finished full case: "+ x +" for test instance:" +instance );
			}
			LOGGER.log(Level.INFO, "Test instance: "+ instance + " done" );
		}
	}

	public static void main(String[] args) { 
		int mode = 1; //-1=debug train 0=train, 1=test TODO README:: CHANGE CONFIGS HERE FIRST 
		if(mode==DebugConfigsML.runmode){
			runAsDebug(mode);
		}else if(mode==TrainConfigsML.runmodeTrain) {
			runAsTraining(mode);
		}else if(mode==TestConfigsML.runmodeTest){
			int start = 1; //TODO README:: provide a starting number to test instances (1-3) 1, will test all 3 instances; 2, will test instances 1,2 and 3 will only run instance 3
			runAsTesting(mode,start);
		}
	}
}
