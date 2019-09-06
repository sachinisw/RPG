package run;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import actors.Agent;
import actors.Decider;
//import causality.Explanation;
import con.ConnectivityGraph;
import landmark.RelaxedPlanningGraph;
import metrics.CausalLink;
import metrics.FeatureSet;
import out.CSVGenerator;
import plans.HSPFPlan;
import plans.HSPPlanner;
import plans.SASPlan;
import plans.TopKPlanner;
import rg.Problem;

public class RunTopK {
	private static final Logger LOGGER = Logger.getLogger(RunML.class.getName());

	/**
	 * Approximate the risk/desirability features by computing plan similarity metrics and landmark distance for the observer. 
	 * Learn a decision tree. Predict intervention in small problems (few blocks, 1-good 1 bad) [DONE]
	 */
	
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
	public static ArrayList<RelaxedPlanningGraph> getRelaxedPlanningGraph(Agent agent, String dom){
		StateGenerator gen = new StateGenerator(agent, dom);
		ArrayList<RelaxedPlanningGraph> rpgs = gen.readRelaxedPlanningGraph();
		return rpgs;
	}

	public static ArrayList<ConnectivityGraph> getConnectivityGraph(Agent agent, String dom){
		StateGenerator gen = new StateGenerator(agent, dom);
		gen.runPlanner();
		ArrayList<ConnectivityGraph> cons = gen.readConnectivityGraphs();
		return cons;
	}

	private static boolean restrict(int mode, int limit, String domain) {
		if((domain.equalsIgnoreCase("EASYIPC") && mode==DebugConfigsML.runmode && limit>DebugConfigsML.fileLimit) ||
				(domain.equalsIgnoreCase("NAVIGATOR") && mode==DebugConfigsML.runmode && limit>DebugConfigsML.fileLimit) || 
				(domain.equalsIgnoreCase("FERRY") && mode==DebugConfigsML.runmode && limit>DebugConfigsML.fileLimit) ) { //when debugging pick only 1 observation file
			return true;
		}else if((domain.equalsIgnoreCase("EASYIPC") && mode==TestConfigsML.runmode && limit>TestConfigsML.fileLimit) ||
				(domain.equalsIgnoreCase("NAVIGATOR") && mode==TestConfigsML.runmode && limit>TestConfigsML.fileLimit) || 
				(domain.equalsIgnoreCase("FERRY") && mode==TestConfigsML.runmode && limit>TestConfigsML.fileLimit) ) { //when testing the trained model in EASYIPC, pick only 10 observation files from each cs/ds pair in current test instance
			return true;
		}else {
			if( (domain.equalsIgnoreCase("EASYIPC") && mode==TrainConfigsML.runmode && limit>TrainConfigsML.fileLimit) || 
					(domain.equalsIgnoreCase("FERRY") && mode==TrainConfigsML.runmode && limit>TrainConfigsML.fileLimit) ||
					(domain.equalsIgnoreCase("NAVIGATOR") && mode==TrainConfigsML.runmode && limit>TrainConfigsML.fileLimit) ||
					(domain.equalsIgnoreCase("RUSHHOUR") && mode==TrainConfigsML.runmode && limit>TrainConfigsML.fileLimit) ) { //when training navigator (creates too many instances), put a limit on num of observation files (100)
			return true;
			}
		}
		return false;
	}

	public static HashMap<ArrayList<String>, ArrayList<SASPlan>> generateAlternativePlans(Decider decider, String domainfile, 
			ArrayList<String> currentstate, List<String> obs, String at_probfile, String ouputpath, int K) {
		//alt plan = prefix (observations made thus far) + suffix (topK plans from current state to hypotheses
		HashMap<ArrayList<String>, ArrayList<SASPlan>> alts = new HashMap<>();
		Problem probA = new Problem(); //critical problem always the same. //use the problem for the attacker's definition.
		probA.readProblemPDDL(at_probfile);
		decider.setDesirableState();
		decider.setUndesirableState();
		String dgoal = Arrays.toString(decider.desirable.getDesirableStatePredicates().toArray());
		Problem probU = probA.replaceGoal(dgoal.substring(1,dgoal.length()-1).replace(",","")); //same attacker's problem with desirable goal added
		currentstate.add(0,"(:init");
		currentstate.add(")");
		probU.setInit(currentstate);
		probA.setInit(currentstate);
		probU.writeProblemFile(ouputpath+"_u.pddl"); //don't have to write the domain 
		probA.writeProblemFile(ouputpath+"_a.pddl");
		TopKPlanner tka = new TopKPlanner(domainfile, probA.getProblemPath(), K );
		ArrayList<SASPlan> atplans = tka.getPlans();
		TopKPlanner tku = new TopKPlanner(domainfile, probU.getProblemPath(), K);
		ArrayList<SASPlan> uplans = tku.getPlans();
		alts.put(decider.critical.getCriticalStatePredicates(),atplans);
		alts.put(decider.desirable.getDesirableStatePredicates(),uplans);
		if(atplans.isEmpty()|| uplans.isEmpty()) {
			LOGGER.log(Level.SEVERE, "Possible attacker/user topK plans generation failed.");
		}
		//		System.out.println(obs+"---------------------");
		int index = 0;
		for (String o : obs) {//add the observation prefix to topK plans
			for (SASPlan sasPlan : uplans) {
				sasPlan.getActions().add(index,o.substring(o.indexOf(":")+1));
			}
			for (SASPlan sasPlan : atplans) {
				sasPlan.getActions().add(index,o.substring(o.indexOf(":")+1));
			}
			index++;
		}
		currentstate.remove("(:init");
		currentstate.remove(")");
		return alts;
	}

	//this decision is made by the intervening agent, who has the full visibility of the domain. so can use the domainfile that has everything in it
	//observations may or may not have harmful actions. Could be executed by the attacker or the user. the goal is to detect the harmful actions as correctly as possible.
	public static HashMap<ArrayList<String>, ArrayList<String>> generateReferencePlans(Decider decider, String domainfile, ArrayList<String> curstate, 
			List<String> obs, String at_probfile, String outputpath, int K) { //ref plan = prefix (observations made thus far) + suffix (optimal plan from current state to hypotheses
		HashMap<ArrayList<String>, ArrayList<String>> refs = new HashMap<>();
		Problem probA = new Problem(); //critical problem always the same. //use the problem for the attacker's definition.
		probA.readProblemPDDL(at_probfile);
		decider.setDesirableState();
		decider.setUndesirableState();
		String dgoal = Arrays.toString(decider.desirable.getDesirableStatePredicates().toArray());
		Problem probU = probA.replaceGoal(dgoal.substring(1,dgoal.length()-1).replace(",","")); //same attacker's problem with desirable goal added
		curstate.add(0,"(:init");
		curstate.add(")");
		probU.setInit(curstate);
		probA.setInit(curstate);
		probU.writeProblemFile(outputpath+"_ref_u.pddl"); //don't have to write the domain 
		probA.writeProblemFile(outputpath+"_ref_a.pddl");
		HSPPlanner hspfa = new HSPPlanner(domainfile, probA.getProblemPath());
		HSPFPlan a_optimal = hspfa.getHSPPlan();
		HSPPlanner hspfu = new HSPPlanner(domainfile, probU.getProblemPath());
		HSPFPlan u_optimal = hspfu.getHSPPlan();
		//		if(a_optimal.getActions().isEmpty()|| u_optimal.getActions().isEmpty()) { //plan can be empty if observations thus far satisfy the goal
		//			LOGGER.log(Level.SEVERE, "Possible attacker/user HSP plan generation failed.");
		//		}
		curstate.remove("(:init");
		curstate.remove(")");
		ArrayList<String> a_refplan = new ArrayList<String>();
		ArrayList<String> u_refplan = new ArrayList<String>();
		for (String o : obs) {
			a_refplan.add(o.substring(o.indexOf(":")+1));
			u_refplan.add(o.substring(o.indexOf(":")+1));
		}
		a_refplan.addAll(a_optimal.getActions());
		u_refplan.addAll(u_optimal.getActions());
		refs.put(decider.critical.getCriticalStatePredicates(),a_refplan);
		refs.put(decider.desirable.getDesirableStatePredicates(),u_refplan);
		return refs;
	}

	public static double[] computeFeatureSet(HashMap<ArrayList<String>, ArrayList<SASPlan>> altplans, 
			HashMap<ArrayList<String>, ArrayList<String>> refplans, ConnectivityGraph con, RelaxedPlanningGraph rpg, 
			ArrayList<String> init, ArrayList<String> curstate, ArrayList<String> critical, ArrayList<String> desirable, String lmo) {
		FeatureSet fs = new FeatureSet(altplans, refplans, con, rpg, init, curstate, critical, desirable, lmo);
		fs.evaluateFeatureValuesForCurrentObservation();
		return fs.getFeaturevals();
	}

	public static ArrayList<CausalLink> findCausalLinksForReferencePlan(HashMap<ArrayList<String>, ArrayList<SASPlan>> altplans, 
			HashMap<ArrayList<String>, ArrayList<String>> refplans, ConnectivityGraph con, RelaxedPlanningGraph rpg, 
			ArrayList<String> init, ArrayList<String> curstate, ArrayList<String> critical, ArrayList<String> desirable, String lmo) {
		FeatureSet fs = new FeatureSet(altplans, refplans, con, rpg, init, curstate, critical, desirable, lmo);
		return fs.getReferencePlanCausalLinksForCriticalState(); //for explanations
	}
	
	public static void writeFeatureValsToFile(String outputfilename, ArrayList<double[]> featurevalsforfiles, Observation obs) {
		ArrayList<String> data = new ArrayList<String>();
		int index = 0;
		for (double[] arr : featurevalsforfiles) {
			String d = "";
			for (double v : arr) {
				d+=String.valueOf(v)+",";
			}
			String ob = obs.getObservations().get(index).substring(2);
			String label = obs.getObservations().get(index).substring(0,1);
			data.add(ob+","+d+label+"\n");
			index++;
		}
		CSVGenerator results = new CSVGenerator(outputfilename, data, 2);
		results.writeOutput();
	}

	public static void run(int mode, String domain, String domainfile, String desirablefile, String a_prob, 
			String a_out, String criticalfile, String a_init, String obs, 
			String ds_csv, String lm_out, int delay, int K, boolean full) {
		Decider decider = new Decider(domain, domainfile, desirablefile, a_prob, a_out, criticalfile , a_init);
		decider.setDesirableState();
		decider.setUndesirableState();
		TreeSet<String> obFiles = getObservationFiles(obs);
		ArrayList<ConnectivityGraph> a_con = getConnectivityGraph(decider, domain);
		ArrayList<RelaxedPlanningGraph> a_rpg = getRelaxedPlanningGraph(decider, domain);
		decider.generateVerifiedLandmarks(a_rpg.get(0), a_con.get(0), lm_out); //verified landmarks generated for this problem. file written to lmout
		int obFileLimit = 1;
		for (String file : obFiles) { 
			if (restrict(mode, obFileLimit, domain)) {
				break;
			}
			obFileLimit++;
			String name[] = file.split("/");
//			if(name[name.length-1].equalsIgnoreCase("4")) { //navigator=5, 0=easyipc,blocks ferry=15
			Observation curobs = setObservations(file); //TODO: how to handle noise in trace.
			ArrayList<double[]> featurevalsforfile = new ArrayList<>();
			ArrayList<String> curstate = new ArrayList<String>();
			ArrayList<String> causalstate = new ArrayList<String>();
			curstate.addAll(decider.getInitialState().getState());
			causalstate.addAll(decider.getInitialState().getState());
//			System.out.println(Arrays.toString(name));
			for (int j=0; j<curobs.getObservations().size(); j++) { //when you make an observation, generate plans with inits set to the effect of that observation
				String outpath = "";
				if(mode==TrainConfigsML.runmode) {
					outpath = TrainConfigsML.root+name[name.length-3]+TrainConfigsML.topkdir+name[name.length-1]+"_"+j;
				}else if(mode==TestConfigsML.runmode) {
					outpath = TestConfigsML.prefix+TestConfigsML.instancedir+name[name.length-5].substring(4)+TestConfigsML.instscenario+
							name[name.length-3]+TestConfigsML.topkdir+name[name.length-1]+"_"+j;
				}
//				System.out.println(outpath);
				ArrayList<String> adds = a_con.get(0).findStatesAddedByAction(curobs.getObservations().get(j).substring(2));
				ArrayList<String> dels = a_con.get(0).findStatesDeletedByAction(curobs.getObservations().get(j).substring(2));
				curstate.removeAll(dels);
				curstate.addAll(adds); //effect of action is visible in the domain
				HashMap<ArrayList<String>, ArrayList<SASPlan>> altplans = generateAlternativePlans(decider, domainfile, curstate, curobs.getObservations().subList(0, j+1), a_prob, outpath, K);
				HashMap<ArrayList<String>, ArrayList<String>> refplans = generateReferencePlans(decider, domainfile, curstate, curobs.getObservations().subList(0, j+1), a_prob, outpath, K);
				double[] featureval = computeFeatureSet(altplans,refplans,a_con.get(0), a_rpg.get(0), decider.getInitialState().getState(), 
						curstate, decider.critical.getCriticalStatePredicates(), decider.desirable.getDesirableStatePredicates(), lm_out);
//				//////////////CAUSAL EXPLANATION MODULE///////////////
//				ArrayList<CausalLink> refPlanCausal = findCausalLinksForReferencePlan(altplans,refplans, a_con.get(0), a_rpg.get(0), decider.getInitialState().getState(), 
//						curstate, decider.critical.getCriticalStatePredicates(), decider.desirable.getDesirableStatePredicates(), lm_out);
				//if(curobs.getObservations().get(j).substring(2).equalsIgnoreCase("Y")) {
//					Explanation.explain(curobs.getObservations().get(j).substring(2), causalstate, refPlanCausal, altplans, refplans, 
//							a_con.get(0), a_rpg.get(0), decider.getInitialState().getState(), curstate,	
//							decider.critical.getCriticalStatePredicates(), decider.desirable.getDesirableStatePredicates(), lm_out);
				//}
				featurevalsforfile.add(featureval);
			} //collect the feature set and write result to csv file for this observation file when this loop finishes
			writeFeatureValsToFile(ds_csv+name[name.length-1]+"_tk.csv", featurevalsforfile, curobs);
//			}
		}
	}
	
	public static void runTopKAsTraining(int mode) {
		LOGGER.log(Level.INFO, "Run mode: TRAINING with "+ TrainConfigsML.cases + " cases");
		String domain = TrainConfigsML.domain;
		for (int casenum=0; casenum<TrainConfigsML.cases; casenum++) {
			LOGGER.log(Level.INFO, "Current case: "+ casenum);
			String domainfile = TrainConfigsML.root+casenum+TrainConfigsML.domainFile;
			String desirablefile = TrainConfigsML.root+casenum+TrainConfigsML.dstates;
			String criticalfile = TrainConfigsML.root+casenum+TrainConfigsML.cstates;
			String a_prob = TrainConfigsML.root+casenum+TrainConfigsML.a_problemFile;
			String a_out = TrainConfigsML.root+casenum+TrainConfigsML.outsdir+TrainConfigsML.a_output;
			String a_init = TrainConfigsML.root+casenum+TrainConfigsML.a_initFile;
			String ds_csv = TrainConfigsML.root+casenum+TrainConfigsML.datadir+TrainConfigsML.decisionCSV;
			String lm_out = TrainConfigsML.root+casenum+TrainConfigsML.datadir+TrainConfigsML.lmoutputFile;
			String obs = TrainConfigsML.root+casenum+TrainConfigsML.obsdir;
			run(mode, domain, domainfile, desirablefile, a_prob, 
					a_out, criticalfile, a_init, obs, ds_csv, lm_out, 0, TrainConfigsML.K, true);
		}
		LOGGER.log(Level.INFO, "Completed data generation to train a model for domain:" + domain);
	}

	public static void runTopKAsDebug(int mode) {
		LOGGER.log(Level.INFO, "Run mode: DEBUG for scenario "+ DebugConfigsML.scenario);
		String domain = DebugConfigsML.domain;
		String domainfile = DebugConfigsML.root+DebugConfigsML.traindir+DebugConfigsML.domainFile;
		String desirablefile = DebugConfigsML.root+DebugConfigsML.traindir+DebugConfigsML.dstates;
		String criticalfile = DebugConfigsML.root+DebugConfigsML.traindir+DebugConfigsML.cstates;
		String a_prob = DebugConfigsML.root+DebugConfigsML.traindir+DebugConfigsML.a_problemFile;
		String a_out = DebugConfigsML.root+DebugConfigsML.traindir+DebugConfigsML.outsdir+DebugConfigsML.a_output;
		String a_init = DebugConfigsML.root+DebugConfigsML.traindir+DebugConfigsML.a_initFile;
		String ds_csv = DebugConfigsML.root+DebugConfigsML.traindir+DebugConfigsML.datadir+DebugConfigsML.decisionCSV;
		String lm_out = DebugConfigsML.root+DebugConfigsML.traindir+DebugConfigsML.datadir+DebugConfigsML.lmoutputFile;
		String obs = DebugConfigsML.root+DebugConfigsML.traindir+DebugConfigsML.obsdir;
		run(mode, domain, domainfile, desirablefile, a_prob, a_out, criticalfile, a_init, obs, ds_csv, lm_out, 0, DebugConfigsML.K, true);
		LOGGER.log(Level.INFO, "Completed data generation to train a model for domain:" + domain);
	}

	public static void runTopKAsTesting(int mode, int start) {
		String domain = TestConfigsML.domain;
		LOGGER.log(Level.INFO, "Run mode: TESTING domain ["+ domain +"]");
		for (int instance=start; instance<=TestConfigsML.instances; instance++) { //blocks-3, navigator-3 easyipc-3, ferry-3 instances
			for (int x=0; x<TestConfigsML.instanceCases; x++) { //blocks,navigator,easyipc, ferry -each instance has 20 problems
				LOGGER.log(Level.INFO, "Current case: "+ x);
				String domainfile = TestConfigsML.prefix+TestConfigsML.instancedir+String.valueOf(instance)+TestConfigsML.instscenario+String.valueOf(x)+TestConfigsML.domainFile;
				String desirablefile = TestConfigsML.prefix+TestConfigsML.instancedir+String.valueOf(instance)+TestConfigsML.instscenario+String.valueOf(x)+TestConfigsML.desirableStateFile;
				String criticalfile = TestConfigsML.prefix+TestConfigsML.instancedir+String.valueOf(instance)+TestConfigsML.instscenario+String.valueOf(x)+TestConfigsML.criticalStateFile;
				String a_prob = TestConfigsML.prefix+TestConfigsML.instancedir+String.valueOf(instance)+TestConfigsML.instscenario+String.valueOf(x)+TestConfigsML.a_problemFile;
				String a_out = TestConfigsML.prefix+TestConfigsML.instancedir+String.valueOf(instance)+TestConfigsML.instscenario+String.valueOf(x)+TestConfigsML.a_outputPath;
				String a_init = TestConfigsML.prefix+TestConfigsML.instancedir+String.valueOf(instance)+TestConfigsML.instscenario+String.valueOf(x)+TestConfigsML.a_initFile;
				String ds_csv = TestConfigsML.prefix+TestConfigsML.instancedir+String.valueOf(instance)+TestConfigsML.instscenario+String.valueOf(x)+TestConfigsML.decisionCSV;
				String lm_out_full = TestConfigsML.prefix+TestConfigsML.instancedir+String.valueOf(instance)+TestConfigsML.instscenario+String.valueOf(x)+TestConfigsML.lmoutputFull;
				String obs = TestConfigsML.prefix+TestConfigsML.instancedir+String.valueOf(instance)+TestConfigsML.instscenario+String.valueOf(x)+TestConfigsML.observationFiles;
				run(mode, domain, domainfile, desirablefile, a_prob, a_out, criticalfile, a_init, obs, ds_csv, lm_out_full, 0, TestConfigsML.K, true);
				LOGGER.log(Level.INFO, "Finished full case: "+ x +" for test instance:" +instance );
			}
			LOGGER.log(Level.INFO, "Test instance: "+ instance + " done" );
		}
	}

	public static void main(String[] args) { 
		//TODO README:: CHANGE CONFIGS HERE FIRST, CHECK K=50 at the top of this file
		int mode = 1; //-1=debug train 0=train, 1=test 
		if(mode==DebugConfigsML.runmode){
			runTopKAsDebug(mode);
		}else if(mode==TrainConfigsML.runmode) {
			runTopKAsTraining(mode);
		}else if(mode==TestConfigsML.runmode){
			int start = 1; //TODO README:: provide a starting number to test instances (1-3) 1, will test all 3 instances; 2, will test instances 1,2 and 3 will only run instance 3
			runTopKAsTesting(mode,start); //TODO: only running the full trace for now. add the observation limited trace if needed later
		}
	}

}
