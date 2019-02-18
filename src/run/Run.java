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

public class Run {

	private static final Logger LOGGER = Logger.getLogger(Run.class.getName());
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
		if((domain.equalsIgnoreCase("EASYIPC") && mode==TestConfigs.runmode && limit>TestConfigs.fileLimit) ||
				(domain.equalsIgnoreCase("NAVIGATOR") && mode==TestConfigs.runmode && limit>TestConfigs.fileLimit) || 
				(domain.equalsIgnoreCase("FERRY") && mode==TestConfigs.runmode && limit>TestConfigs.fileLimit) ) { //when testing the trained model in EASYIPC, pick only 10 observation files from each cs/ds pair in current test instance
			return true;
		}else {
			if( (domain.equalsIgnoreCase("EASYIPC") && mode==TrainConfigs.runmode && limit>TrainConfigs.fileLimit) || 
					(domain.equalsIgnoreCase("FERRY") && mode==TrainConfigs.runmode && limit>TrainConfigs.fileLimit) ||
					(domain.equalsIgnoreCase("NAVIGATOR") && mode==TrainConfigs.runmode && limit>TrainConfigs.fileLimit)) { //when training navigator (creates too many instances), put a limit on num of observation files (100)
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
		LOGGER.log(Level.INFO, "Run mode: TRAINING with "+ TrainConfigs.cases + " cases");
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
			String ds_csv = TrainConfigs.root+casenum+TrainConfigs.datadir+TrainConfigs.decisionCSV;
			String lm_out = TrainConfigs.root+casenum+TrainConfigs.datadir+TrainConfigs.lmoutputFile;
			String obs = TrainConfigs.root+casenum+TrainConfigs.obsdir;
			boolean writedot = TrainConfigs.writeDOT;
			run(mode, domain, domainfile, desirablefile, a_prob, dotpre, 
					a_out, criticalfile, a_init, a_dotsuf, obs, ds_csv, lm_out, 0, writedot, true);
		}
		LOGGER.log(Level.INFO, "Completed data generation to train a model for domain:" + domain);
	}

	public static void runAsDebug(int mode) {
		LOGGER.log(Level.INFO, "Run mode: DEBUG for scenario "+ DebugConfigs.scenario);
		String domain = DebugConfigs.domain;
		String domainfile = DebugConfigs.root+DebugConfigs.domainFile;
		String desirablefile = DebugConfigs.root+DebugConfigs.dstates;
		String criticalfile = DebugConfigs.root+DebugConfigs.cstates;
		String dotpre = DebugConfigs.root+DebugConfigs.dotdir;
		String a_prob = DebugConfigs.root+DebugConfigs.a_problemFile;
		String a_out = DebugConfigs.root+DebugConfigs.outsdir+DebugConfigs.a_output;
		String a_init = DebugConfigs.root+DebugConfigs.a_initFile;
		String a_dotsuf = DebugConfigs.a_dotFileSuffix;
		String ds_csv = DebugConfigs.root+DebugConfigs.datadir+DebugConfigs.decisionCSV;
		String lm_out = DebugConfigs.root+DebugConfigs.datadir+DebugConfigs.lmoutputFile;
		String obs = DebugConfigs.root+DebugConfigs.obsdir;
		boolean writedot = DebugConfigs.writeDOT;
		run(mode, domain, domainfile, desirablefile, a_prob, dotpre, 
				a_out, criticalfile, a_init, a_dotsuf, obs, ds_csv, lm_out, 0, writedot, true);
		LOGGER.log(Level.INFO, "Completed data generation to train a model for domain:" + domain);
	}

	public static void runAsTesting(int mode, int start) {
		String domain = TestConfigs.domain;
		boolean writedot = TestConfigs.writeDOT; 
		LOGGER.log(Level.INFO, "Run mode: TESTING domain ["+ domain +"]");
		for (int instance=start; instance<=TestConfigs.instances; instance++) { //blocks-3, navigator-3 easyipc-3, ferry-3 instances
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
				String ds_csv = TestConfigs.prefix+TestConfigs.instancedir+String.valueOf(instance)+TestConfigs.instscenario+String.valueOf(x)+TestConfigs.decisionCSV;
				String lm_out_full = TestConfigs.prefix+TestConfigs.instancedir+String.valueOf(instance)+TestConfigs.instscenario+String.valueOf(x)+TestConfigs.lmoutputFull;
				String lm_out_short50 = TestConfigs.prefix+TestConfigs.instancedir+String.valueOf(instance)+TestConfigs.instscenario+String.valueOf(x)+TestConfigs.lmoutputShort50;
				String lm_out_short75 = TestConfigs.prefix+TestConfigs.instancedir+String.valueOf(instance)+TestConfigs.instscenario+String.valueOf(x)+TestConfigs.lmoutputShort75;
				String obs = TestConfigs.prefix+TestConfigs.instancedir+String.valueOf(instance)+TestConfigs.instscenario+String.valueOf(x)+TestConfigs.observationFiles;
				String obslm50 = TestConfigs.prefix+TestConfigs.instancedir+String.valueOf(instance)+TestConfigs.instscenario+String.valueOf(x)+TestConfigs.limitedObservationFiles50;
				String obslm75 = TestConfigs.prefix+TestConfigs.instancedir+String.valueOf(instance)+TestConfigs.instscenario+String.valueOf(x)+TestConfigs.limitedObservationFiles75; 
				run(mode, domain, domainfile, desirablefile, a_prob, a_dotpre_full, a_out, criticalfile, a_init, a_dotsuf, obs, ds_csv, lm_out_full, 0, writedot, true);
				LOGGER.log(Level.INFO, "Finished full case: "+ x +" for test instance:" +instance );
				run(mode, domain, domainfile, desirablefile, a_prob, a_dotpre_lm, 
						a_out, criticalfile, a_init, a_dotsuf, obslm50, ds_csv, lm_out_short50, 50, writedot, false);
				LOGGER.log(Level.INFO, "Finished 50% reduced case: "+ x +" for test instance:" +instance );
				run(mode, domain, domainfile, desirablefile, a_prob, a_dotpre_lm, 
						a_out, criticalfile, a_init, a_dotsuf, obslm75, ds_csv, lm_out_short75, 75, writedot, false);
				LOGGER.log(Level.INFO, "Finished 75% reduced case: "+ x +" for test instance:" +instance );
			}
			LOGGER.log(Level.INFO, "Test instance: "+ instance + " done" );
		}
	}

	public static void main(String[] args) { 
		int mode = 1; //-1=debug train 0=train, 1=test TODO README:: CHANGE CONFIGS HERE FIRST 
		if(mode==DebugConfigs.runmode){
			runAsDebug(mode);
		}else if(mode==TrainConfigs.runmode) {
			runAsTraining(mode);
		}else if(mode==TestConfigs.runmode){
			int start = 1; //TODO README:: provide a starting number to test instances (1-3) 1, will test all 3 instances; 2, will test instances 1,2 and 3 will only run instance 3
			runAsTesting(mode,start);
		}
	}
}
