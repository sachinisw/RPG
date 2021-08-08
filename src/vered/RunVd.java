package vered;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import con.ConnectivityGraph;
import landmark.OrderedLMGraph;
import landmark.OrderedLMNode;
import landmark.RelaxedPlanningGraphGenerator;
import log.EventLogger;
import plans.FFPlanner;
import plans.HSPFPlan;
import plans.HSPPlanner;
import plans.JavaFFPlan;
import plans.JavaFFPlanner;
import rg.Domain;
import rg.Hypotheses;
import rg.Observations;
import rg.Problem;


public class RunVd {

	public static TreeSet<String> getFilesInPath(String filepath){
		TreeSet<String> filepaths = new TreeSet<String>();
		try {
			File dir = new File(filepath);
			List<File> files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
			for (File fileItem : files) {
				filepaths.add(fileItem.getCanonicalPath());
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
		return filepaths;	
	}

	public static TreeSet<String> filterFiles(TreeSet<String> files, TreeSet<String> actualobs){
		TreeSet<String> filtered = new TreeSet<String>();
		for (String string : files) {
			if(!string.contains("lm")) {
				String parts[] = string.split("/");
				for (String o : actualobs) {
					String oparts[] = o.split("/");
					if(oparts[oparts.length-1].equalsIgnoreCase(parts[parts.length-1].substring(0, parts[parts.length-1].indexOf(".")))) {
						filtered.add(o);
					}
				}
			}
		}
		return filtered;
	}

	public static HashMap<String,TreeSet<String>> readLandmarksGNOrders(String lmfile){
		Scanner sc;
		HashMap<String,TreeSet<String>> lms = new HashMap<String, TreeSet<String>>();	
		boolean start = false;
		try {
			sc = new Scanner(new File(lmfile));
			while(sc.hasNextLine()) {
				String line = sc.nextLine().trim();
				if(line.contains(":LGG GREEDY NECESSARY ORDERS")) {
					start = true;
				}
				if(start && line.contains("UNVERIFIED")) {
					start = false;
				}
				if(start && !line.contains("LGG GREEDY NECESSARY ORDERS") && !line.isEmpty()) {
					String parts [] = line.split(":");
					String key = parts[0].substring(parts[0].indexOf("[")+1,parts[0].indexOf("]"));
					if(!lms.containsKey(key)) {
						lms.put(key,new TreeSet<String>());
					}
					TreeSet<String> set = lms.get(key);
					String val = parts[1].substring(2,parts[1].length()-1);
					if(!val.isEmpty()) {
						String valparts[] = val.split(",");
						for (String s : valparts) {
							set.add(s.substring(s.indexOf("[")+1,s.indexOf("]")));
						}
					}
				}
			}
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return  lms;
	}

	//online goal recognition with landmarks - Vered 2017
	//Online goal Recognition as Reasoning over Landmarks, Towards online goal recognition combining goal mirroring and landmarks
	public static void runVered(int start) {
		ArrayList<Long> runtimes = new ArrayList<>();
		for (int inst=start; inst<=TestConfigsVd.instances; inst++) { //blocks-3, navigator-3 easyipc-3, ferry-3 instances
			long duration = 0L; long numReqs = 0L;
			for (int scen=0; scen<TestConfigsVd.instanceCases; scen++) { //blocks,navigator,easyipc, ferry -each instance has 20 problems
				String landmarkfile = TestConfigsVd.prefix + TestConfigsVd.instancedir + inst + TestConfigsVd.instscenario + scen + TestConfigsVd.atLmfile;
				String ulandmarkfile = TestConfigsVd.prefix + TestConfigsVd.instancedir + inst + TestConfigsVd.instscenario + scen + TestConfigsVd.uLmfile;
				String desirables = TestConfigsVd.prefix + TestConfigsVd.instancedir + inst + TestConfigsVd.instscenario + scen + TestConfigsVd.desirableStateFile;
				String criticals = TestConfigsVd.prefix + TestConfigsVd.instancedir + inst + TestConfigsVd.instscenario + scen + TestConfigsVd.criticalStateFile;
				String initfile = TestConfigsVd.prefix + TestConfigsVd.instancedir + inst + TestConfigsVd.instscenario + scen + TestConfigsVd.a_initFile;
				String testedObservations = TestConfigsVd.prefix + TestConfigsVd.instancedir + inst + TestConfigsVd.instscenario + scen + TestConfigsVd.testedObservationFiles;
				String actualObservations = TestConfigsVd.prefix + TestConfigsVd.instancedir + inst + TestConfigsVd.instscenario + scen + TestConfigsVd.observationFiles;
				String domfile = TestConfigsVd.prefix + TestConfigsVd.instancedir + inst + TestConfigsVd.instscenario + scen + TestConfigsVd.domainFile;
				String probfile = TestConfigsVd.prefix + TestConfigsVd.instancedir + inst + TestConfigsVd.instscenario + scen + TestConfigsVd.a_problemFile;
				String outdir = TestConfigsVd.prefix + TestConfigsVd.instancedir + inst + TestConfigsVd.instscenario + scen + TestConfigsVd.verout + TestConfigsVd.planner;
				String a_out = TestConfigsVd.prefix + TestConfigsVd.instancedir + inst + TestConfigsVd.instscenario + scen + TestConfigsVd.a_out;
				Hypotheses hyp = setHypothesis(criticals, desirables);
				ArrayList<String> critical = new ArrayList<String>();
				ArrayList<String> desirable = new ArrayList<String>();
				String [] parts = hyp.getHyps().get(0).split("\\)");
				for (String s : parts) {
					critical.add(s+")");
				}
				String [] partsd = hyp.getHyps().get(1).replace(",","").substring(hyp.getHyps().get(1).indexOf(":")+1).split("\\)");
				for (String s : partsd) {
					desirable.add(s+")");
				}
				long scenario_start_time = System.currentTimeMillis();
				generateRPGConForDesirableGoal(domfile, probfile, desirable, a_out);
				ConnectivityGraph a_con = readConnectivityGraphs(a_out + TestConfigsVd.a_connectivityGraphFile);
				ConnectivityGraph u_con = readConnectivityGraphs(a_out + TestConfigsVd.u_connectivityGraphFile);
				ArrayList<String> originalInit = setInits(initfile); //set the state to init.
				TreeSet<String> obfiles = filterFiles(getFilesInPath(testedObservations), getFilesInPath(actualObservations));
				redoLandmarks(a_out + TestConfigsVd.a_rpgFile, a_out + TestConfigsVd.a_connectivityGraphFile, critical, originalInit, landmarkfile);
				redoLandmarks(a_out + TestConfigsVd.u_rpgFile, a_out + TestConfigsVd.u_connectivityGraphFile, desirable, originalInit, ulandmarkfile);
				HashMap<String,TreeSet<String>> a_landmarks = readLandmarksGNOrders(landmarkfile);
				HashMap<String,TreeSet<String>> u_landmarks = readLandmarksGNOrders(ulandmarkfile);
				OrderedLMGraph aGraph = new OrderedLMGraph(critical);
				OrderedLMGraph uGraph = new OrderedLMGraph(desirable);
				aGraph.produceOrders(a_landmarks, critical);
				uGraph.produceOrders(u_landmarks, desirable);
				aGraph.assignSiblingLevels();
				uGraph.assignSiblingLevels();
				Iterator<String> itr = obfiles.iterator();
				int filestart = 0;
				while(itr.hasNext()) {//iterates over observation files in the current scenario in the current instance
					long file_start_time = System.currentTimeMillis();
					String s = itr.next();
					EventLogger.LOGGER.log(Level.INFO, "Current file::   "+s);
					String path[] = s.split("/");
					createDirectory(outdir, path[path.length-1]);
					Domain dom = new Domain();
					dom.readDominPDDL(domfile);
					Problem probC = new Problem(); //critical problem //use the problem for the attacker's definition.
					probC.readProblemPDDL(probfile); 
					Problem probD = probC.replaceGoal(hyp.getHyps().get(1)); //same attacker's problem with desirable goal added
					probD.writeProblemFile(outdir+"p_desirable"+".pddl"); //don't have to write the domain file, or prob (critical goal). no modification is made to that file
					Observations obs = new Observations(); //this one has the class labels
					obs.readObs(s);
					try {
						Observations noLabel = (Observations) obs.clone();
						noLabel.removeLabels();
						long start_time = 0L;
						if(filestart==0) {
							start_time = scenario_start_time;
						}else {
							start_time = file_start_time;
						}
						HashMap<String, String> decisions = doGoalMirroringWithLandmarks(noLabel, originalInit, dom, probC, probD, a_con, 
								u_con, hyp, a_landmarks, u_landmarks, uGraph, aGraph, outdir+path[path.length-1]+"/", start_time);

						writeResultFile(decisions, obs, outdir+path[path.length-1]+"/"+TestConfigsVd.outputfile + "_" + path[path.length-1] + ".csv");

					} catch (CloneNotSupportedException e) {
						EventLogger.LOGGER.log(Level.SEVERE, "ERROR:: "+e.getMessage());
					}
					filestart++;
				}
				String current = computeProcessingTime(inst, scen);
				duration += Long.parseLong(current.split(",")[0]);
				numReqs += Long.parseLong(current.split(",")[1]);
			}
			runtimes.add(duration);
			runtimes.add((duration/numReqs));
		}
		System.out.println(runtimes);
	}

	public static TreeSet<String> getVdCSV(String path){
		TreeSet<String> csv = new TreeSet<String>();
		try {
			File dir = new File(path);
			List<File> files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
			for (File fileItem : files) {
				if(fileItem.getCanonicalPath().contains(".csv")) {
					csv.add(fileItem.getCanonicalPath());
				}
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
		return csv;	
	}
	
	public static String computeProcessingTime(int instance, int casenum) {
		String ds_csv_path = TestConfigsVd.prefix + TestConfigsVd.instancedir + instance + TestConfigsVd.instscenario + casenum + TestConfigsVd.verout + TestConfigsVd.planner;
		TreeSet<String> csv = getVdCSV(ds_csv_path);
		int number_of_decisions = 0;
		long total_duration = 0L;
		for (String path : csv) {
			Scanner scan;
			try {
				scan = new Scanner (new File(path));
				while(scan.hasNextLine()) {
					String line = scan.nextLine();
					String parts[] = line.split(",");
					int duration = Integer.parseInt(parts[parts.length-1]); 
					total_duration += duration;
					number_of_decisions++;
				}
				scan.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return String.valueOf(total_duration)+","+String.valueOf(number_of_decisions);
	}
	
	private static void generateRPGConForDesirableGoal(String domainfile, String probfile, ArrayList<String> desirabe, String out) {
		Problem probC = new Problem(); //critical problem //use the problem for the attacker's definition.
		probC.readProblemPDDL(probfile); 
		String des = "";
		for (String s : desirabe) {
			des += s;
		}
		Problem probD = probC.replaceGoal(des); //same attacker's problem with desirable goal added
		probD.writeProblemFile(out+"problem_au.pddl");
		FFPlanner ffp = new FFPlanner(domainfile, probD.getProblemPath());
		ffp.runFF(3, out+TestConfigsVd.u_connectivityGraphFile);
		ffp.runFF(2, out+TestConfigsVd.u_rpgFile);
	}

	public static HashMap<String, String> doGoalMirroringWithLandmarks(Observations obs, ArrayList<String> init,
			Domain dom, Problem pcri, Problem pdes, ConnectivityGraph a_con, ConnectivityGraph u_con, Hypotheses hyp, 
			HashMap<String,TreeSet<String>> lmsa, HashMap<String,TreeSet<String>> lmsd, OrderedLMGraph uGraph,
			OrderedLMGraph aGraph, String outdir, long start_time){ //prob comes from problem_a.txt
		HashMap<String, String> obsTolikelgoal = new HashMap<String, String>();
		ArrayList<HSPFPlan> i_k = new ArrayList<>();
		ArrayList<String> m_k = new ArrayList<>();
		TreeSet<String> activeAFL = new TreeSet<String>();
		TreeSet<String> activeDFL = new TreeSet<String>();
		TreeSet<String> achievedAFL = new TreeSet<String>();
		TreeSet<String> achievedDFL = new TreeSet<String>();
		i_k.add(produceIdealPlanHSP(dom, pcri)); //need to do it twice because I have two goals. This is the ideal plans for the 2 goals
		i_k.add(produceIdealPlanHSP(dom, pdes));

		ArrayList<String> state = new ArrayList<>(init);
		for (int i=0; i<obs.getObs().size(); i++) {
			long ob_start = System.currentTimeMillis();
			HashMap<String, Double> goalranks = new HashMap<String, Double>();
			ArrayList<String> activeHypotheses = new ArrayList<>();
			String now = obs.getObs().get(i);
			//System.out.println("OB--"+now);
			m_k.add(now); //cur obs becomes part of the prefix
			ArrayList<String> obstate = convertObservationToState(state, dom, a_con, now); //to take add/del effects can take either one
			achieveLandmark(now, dom, a_con, obstate, achievedAFL, activeAFL, lmsa, aGraph); //attack landmarks
			achieveLandmark(now, dom, a_con, obstate, achievedDFL, activeDFL, lmsd, uGraph); //desirable landmarks, with same connectivity because, intervener has full observability
			//wait until vered/ramon tells me how to prune hypotheses. they never got back to me. #sad. 
			//to prune goals, I assumed the last ordered landmark to be the goal predicates. if the last ordered landmark is in achieved, that means the goal is already met. prune those goals out
			for (int j=0; j<hyp.getHyps().size(); j++) {
				String goal = hyp.getHyps().get(j);
				ArrayList<String> goalpredicates = new ArrayList<String>();
				String[] gparts = goal.substring(goal.indexOf(":")+2,goal.length()-1).split("\\)\\(");
				for (String s : gparts) {
					goalpredicates.add("("+s+")");
				}
				if(goal.contains("desirable")) {
					if(!achievedLMcontainsLastOrderedLandmark(achievedDFL,uGraph,goalpredicates)) {
						activeHypotheses.add(goal);
					}
				}else if(!achievedLMcontainsLastOrderedLandmark(achievedAFL,aGraph,goalpredicates)) {
					activeHypotheses.add(goal);
				}
			}
			if(!activeHypotheses.isEmpty()) {
				for (int j=0; j<activeHypotheses.size(); j++) {
					ArrayList<String> curstate = new ArrayList<>();
					Problem probC = new Problem(); //critical problem //use the problem for the attacker's definition.
					probC.readProblemPDDL(pcri.getProblemPath()); 
					Problem probD = probC.replaceGoal(activeHypotheses.get(j).substring(activeHypotheses.indexOf(":")+1)); //same attacker's problem with desirable goal added
					curstate.add(0,"(:init");
					curstate.addAll(obstate);
					curstate.add("\n)");
					probD.setInit(curstate);
					probD.writeProblemFile(outdir+"/p_"+i+"_"+j+".pddl"); //don't have to write the domain 
					JavaFFPlan suffix = producePlansJavaFF(dom,probD);
					m_k.addAll(suffix.getActions()); // m_k = prefix+suffix
					if(activeHypotheses.get(j).contains("desirable")) {
						goalranks.put(activeHypotheses.get(j),(double)i_k.get(1).getPlanCost()/(double)m_k.size());
					}else {
						goalranks.put(activeHypotheses.get(j),(double)i_k.get(0).getPlanCost()/(double)m_k.size());
					}
					m_k.removeAll(suffix.getActions());//System.out.println("pref======"+m_k);
				}
				Entry<String, Double> ent = maxLikelyGoal(goalranks); //if ent = null, then the agent wasn't able to decide what the most likely goal is
				long ob_end = System.currentTimeMillis();
				long duration = 0L;
				if(i==0) {
					duration = ob_end - start_time;
				}else {
					duration = ob_end - ob_start;
				}
				if(ent != null) {
					obsTolikelgoal.put(now, ent.getKey()+","+duration);
				}else {
					obsTolikelgoal.put(now, null+","+duration);
				}
			}else { //all hypotheses are removed because of respective landmarks are active
				long duration = System.currentTimeMillis() - ob_start;
				obsTolikelgoal.put(now, null+","+duration);
			}
			state.clear();
			state.addAll(obstate);
		}
		state.clear(); //reset state to original init before moving on to the  next observation file
		state.addAll(init);//System.out.println(obsTolikelgoal);
		return obsTolikelgoal;
	}

	//find from stuff in achieved, the predicate(s) closest to the root of the landmark generation graph
	//assuming ***last ordered landmark*** to be the goal predicates
	private static boolean achievedLMcontainsLastOrderedLandmark(TreeSet<String> achieved, OrderedLMGraph orderedGraph, ArrayList<String> goal) {
		ArrayList<String> achievedRoots = new ArrayList<String>();
		for (String s : achieved) { 		//check if achieved[] contains goal predicates (ordered int = 0)
			Iterator<OrderedLMNode> itr = orderedGraph.getAdj().keySet().iterator();
			while(itr.hasNext()) {
				OrderedLMNode key = itr.next();
				if(key.getTreeLevel()==0 && key.getNodecontent().contains(s)) { //this is a root, that has been achieved.
					achievedRoots.add(s);
				}
			}
		}
		for (String g : goal) {
			if(!achievedRoots.contains(g)) {
				return false;
			}
		}
		return true;
	}

	public static Entry<String, Double> maxLikelyGoal(HashMap<String, Double> map) {
		//for each goal compute P(Gi|O) (i=1,2) using method above. highest P(Gi|O) is the most likely goal
		double neta = computeNeta(map);
		Entry<String, Double> e = null;
		double max = Double.MIN_VALUE;
		Iterator<Entry<String, Double>> itr = map.entrySet().iterator();
		while(itr.hasNext()) {
			Entry<String, Double> ent = itr.next();
			ent.setValue(ent.getValue()*neta);
			if(ent.getValue() > max) {
				e = ent;
				max = ent.getValue();
			}
		}
		Collection<Double> valuesList = map.values();
		Set<Double> valuesSet = new HashSet<Double>(map.values());
		if(valuesList.size()!=valuesSet.size() && valuesSet.contains(max)) {//check if there are ties. if there is a tie, that means the agent can't decide whats the likely goal.
			e = null;
		}
		return e;
	}

	private static double computeNeta(HashMap<String, Double> goalranks) { //Vered,Kaminka 2017 Heuristic online goal recognition in continuous domains.
		double rank = 0.0;
		Iterator<String> itr = goalranks.keySet().iterator();
		while(itr.hasNext()) {
			rank += goalranks.get(itr.next());
		}
		if(rank>0) {
			return 1/rank;
		}else {
			return 0.0;
		}
	}

	//observation is a state. 
	//achievedFL=facts that were satisfied before but no longer satisfied
	//activeFL = facts that are satisfied in current state
	//prune out goals from hyp if (last achieved ordered landmark) is associated with that goal. What is this ***last achieved ordered lm***???
	//for the remaining goals, rank them by the decreasing order of percentage of achievedlandmarks. This is not the one i am doing (From paper: Meneguzzi 2017 landmark based plan recognition ECAI, Landmark based heuristics for goal-recognition)
	//this experiment is for the algorithm:: goal mirroring with landmarks, where landmarks are used to filter out impossible goals and remaining goals are ranked based on cost. (Towards online goal-recognition combining goal mirroring and landmarks - vered, ramon, kaminka, meneguzzi
	public static void achieveLandmark(String ob, Domain dom, ConnectivityGraph con, ArrayList<String> stateafterob,
			TreeSet<String> achievedFL, TreeSet<String> activeFL, HashMap<String, TreeSet<String>> landmarks, OrderedLMGraph graph) {
		if((!activeFL.isEmpty()) && (!observationContainsActiveFL(ob, dom, con, stateafterob, activeFL, graph))) {
			//state resulting from this observation ob contains activeFact Landmarks. this means the facts are now achieved.//can move the active to achieved.
			achievedFL.addAll(activeFL);
			activeFL.clear();
		} else if(activeFL.isEmpty()) {//no active landmarks. find all fact landmarks closest to root in observed state. add to activeFL
			Iterator<Entry<String,TreeSet<String>>> itrg = landmarks.entrySet().iterator();
			ArrayList<String> active = new ArrayList<String>();
			while(itrg.hasNext()) {
				Entry<String, TreeSet<String>> e = itrg.next();
				String cur = e.getKey();
				if(stateafterob.contains(cur)) { //don't have to check for children. cur is the head. if head is in stateafterob, also add it's children to it. because landmarks are ordered
					active.add(cur);
				}
			}
			int closeToRoot = Integer.MAX_VALUE;
			String close = "";
			for (String s : active) { 
				Iterator<OrderedLMNode> itr = graph.getAdj().keySet().iterator();
				while(itr.hasNext()) {
					OrderedLMNode key = itr.next();
					if(key.getNodecontent().contains(s)) {
						if(key.getTreeLevel()<=closeToRoot && key.getTreeLevel()>=0) {
							closeToRoot = key.getTreeLevel();
							close = s;
						}
					}
				}
				activeFL.add(close);
			}
		}
//		System.out.println("=================================");
//		System.out.println("active    : "+activeFL);
//		System.out.println("achieved  : "+achievedFL);
//		System.out.println("================================");
	}

	public static ArrayList<String> convertObservationToState(ArrayList<String> state, Domain dom, ConnectivityGraph con, String obs){
		ArrayList<String> statenew = new ArrayList<String>();
		statenew.addAll(state);
		ArrayList<String> applicables = con.findApplicableActionsInState(statenew);
		for (String ac : applicables) {
			if(obs.equalsIgnoreCase(ac)) {
				ArrayList<String> del = con.findStatesDeletedByAction(ac);
				ArrayList<String> add = con.findStatesAddedByAction(ac);
				statenew.removeAll(del);
				statenew.addAll(add);
				break;
			}
		}
		return statenew;
	}

	//check whether ob has caused activeFL to become achieved. i.e. ob contains a higher order landmark.
	public static boolean observationContainsActiveFL(String ob, Domain dom, ConnectivityGraph con, 
			ArrayList<String> state, TreeSet<String> activeFL, OrderedLMGraph graph) {
		ArrayList<String> newstate = convertObservationToState(state, dom, con, ob);
		ArrayList<OrderedLMNode> closest = new ArrayList<>();
		int currentmin = 0;
		HashMap<OrderedLMNode, TreeSet<OrderedLMNode>> sortedbylevel = graph.sortByTreeLevel();
		Iterator<OrderedLMNode> sorteditr = sortedbylevel.keySet().iterator();
		while(sorteditr.hasNext()) {//from newstate find the predicate(s) with lowest tree level value
			OrderedLMNode ord = sorteditr.next();
			if(ord.getTreeLevel()<=currentmin && ord.getTreeLevel()>=0) {
				currentmin = ord.getTreeLevel();
				closest.add(ord);
			}
		}
		int count = 0;
		for (String s : newstate) {
			for (OrderedLMNode o : closest) {
				if(o.getNodecontent().contains(s)) {
					++count;
				}
			}
		}
		if(count==closest.size()) {
			return true;
		}
		return false;
	}

	public static void writeResultFile(HashMap<String, String> decisions, Observations actuals, String outfile) {
		FileWriter writer = null;
		try {
			File file = new File(outfile);
			writer = new FileWriter(file);
			for (String o : actuals.getObs()) {
				String ob = o.substring(2);			
				String decisionparts[] = decisions.get(ob).split(","); //0 = most likely goal, 1=time
				writer.write(o.substring(0,2)+","+ob+","+decisionparts[0]+","+ decisionparts[1]+"\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	public static Hypotheses setHypothesis(String criticalfile, String desirablefile) {
		Hypotheses hyp = new Hypotheses();
		hyp.readHyps(criticalfile);
		hyp.readHyps(desirablefile);
		return hyp;
	}

	public static ArrayList<String> setInits(String initfile) {
		ArrayList<String> inits = new ArrayList<String>();
		Scanner sc;
		try {
			sc = new Scanner(new File(initfile));
			while(sc.hasNextLine()) {
				inits.add( sc.nextLine().trim());
			}
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return inits;
	}

	public static void createDirectory(String outputpath, String obfilename) {
		new File(outputpath+obfilename+"/").mkdirs();
	}

	public static JavaFFPlan producePlansJavaFF(Domain dom, Problem prob) {
		JavaFFPlanner ffp = new JavaFFPlanner(dom.getDomainPath(), prob.getProblemPath());
		return ffp.getJavaFFPlan();
	}

	public static HSPFPlan produceIdealPlanHSP(Domain dom, Problem prob) {
		HSPPlanner hsp = new HSPPlanner(dom.getDomainPath(), prob.getProblemPath());
		return hsp.getHSPPlan();
	}
	
	public static ConnectivityGraph readConnectivityGraphs(String confile){
		ConnectivityGraph graph = new ConnectivityGraph(confile);
		graph.readConGraphOutput(confile);
		return graph;
	}

	//only need this to run vered. the old version of lmoutput file does not have the ordering constrains.
	public static void redoLandmarks(String rpgfile, String confile, ArrayList<String> critical, ArrayList<String> init, String lmout) {
		RelaxedPlanningGraphGenerator rpgen= new RelaxedPlanningGraphGenerator();
		rpgen.runLandmarkGenerator(rpgfile, confile, critical, init, lmout);
	}

	//TNR,TPR,FNR,FPR values for R&G, using current planner
	public static void computeResults(int start) {
		for (int inst=start; inst<=TestConfigsVd.instances; inst++) { //blocks-3, navigator-3 easyipc-3, ferry-3 instances
			int tp=0, tn=0, fp=0, fn=0;
			for (int scen=0; scen<TestConfigsVd.instanceCases; scen++) { //blocks,navigator,easyipc, ferry -each instance has 20 problems
				String outdir = TestConfigsVd.prefix + TestConfigsVd.instancedir + inst + TestConfigsVd.instscenario + scen + TestConfigsVd.verout + TestConfigsVd.planner;
				String desirables = TestConfigsVd.prefix + TestConfigsVd.instancedir + inst + TestConfigsVd.instscenario + scen + TestConfigsVd.desirableStateFile;
				String criticals = TestConfigsVd.prefix + TestConfigsVd.instancedir + inst + TestConfigsVd.instscenario + scen + TestConfigsVd.criticalStateFile;
				Hypotheses hyp = setHypothesis(criticals, desirables);
				TreeSet<String> paths = getFilesInPath(outdir);
				for (String s : paths) {
					if(s.contains("out_")) {
						ArrayList<String> result = readResultOutput(s);
						tp+=countTP(result, hyp);
						tn+=countTN(result, hyp);
						fp+=countFP(result, hyp);
						fn+=countFN(result, hyp);
					}
				}
			}
			writeRatesToFile(tp, tn, fp, fn, TestConfigsVd.prefix+TestConfigsVd.instancedir + inst +TestConfigsVd.resultOutpath+"vd_jff.csv");//this is the tp, tn totals for the 20 cases for the current instance.
		}
	}

	public static int countTP(ArrayList<String> result, Hypotheses hyp) {
		int count = 0;
		String critical = hyp.getHyps().get(0);
		for (String string : result) {
			String[] parts = string.split(",");
			if(parts[0].equalsIgnoreCase("Y:")) {
				if(parts[2].equalsIgnoreCase(critical) ){
					count++;
				}
			}
		}
		return count;
	}

	public static int countTN(ArrayList<String> result, Hypotheses hyp) {
		int count = 0;
		String desirable = hyp.getHyps().get(1);
		for (String string : result) {
			String[] parts = string.split(",");
			if(parts[0].equalsIgnoreCase("N:")) {
				if(parts[2].equalsIgnoreCase(desirable) || parts[2].equalsIgnoreCase("null")) {
					count++;
				}
			}
		}
		return count;
	}

	public static int countFP(ArrayList<String> result, Hypotheses hyp) {
		int count = 0;
		String critical = hyp.getHyps().get(0);
		for (String string : result) {
			String[] parts = string.split(",");
			if(parts[0].equalsIgnoreCase("N:")) {
				if(parts[2].equalsIgnoreCase(critical)) {
					count++;
				}
			}
		}
		return count;
	}

	public static int countFN(ArrayList<String> result, Hypotheses hyp) {
		int count = 0;
		String desirable = hyp.getHyps().get(1);
		for (String string : result) {
			String[] parts = string.split(",");
			if(parts[0].equalsIgnoreCase("Y:")) {
				if(parts[2].equalsIgnoreCase(desirable) || parts[2].equalsIgnoreCase("null")) {
					count++;
				}
			}
		}
		return count;
	}

	public static ArrayList<String> readResultOutput(String filename){
		ArrayList<String> results = new ArrayList<String>();
		Scanner sc;
		try {
			sc = new Scanner(new File(filename));
			while(sc.hasNextLine()) {
				results.add(sc.nextLine().trim());
			}
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return results;
	}

	public static void writeRatesToFile(int TP, int TN, int FP, int FN, String filename) {
		FileWriter writer = null;
		double tpr = (double) TP/(double) (TP+FN);
		double tnr = (double) TN/(double) (TN+FP);
		double fnr = (double) FN/(double) (TP+FN);
		double fpr = (double) FP/(double) (TN+FP);
		double precision = (double)TP/(double)(TP+FP); //tp/tp+fp
		double recall = (double)TP/(double)(TP+FN);   //tp/tp+fn
		double f1 = 2.0 * ( (precision*recall) / (precision+recall));
		String mcc = computeMCC(TP, TN, FP, FN);
		try {
			File file = new File(filename);
			writer = new FileWriter(file);
			writer.write("TPR,TNR,FPR,FNR"+"\n");
			writer.write(String.valueOf(tpr)+","+String.valueOf(tnr)+","+String.valueOf(fpr)+","+String.valueOf(fnr)+"\n");
			writer.write("PRECISION,RECALL,F1,MCC\n");
			writer.write(String.valueOf(precision)+","+String.valueOf(recall)+","+String.valueOf(f1)+","+String.valueOf(mcc)+"\n");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	public static String computeMCC(double TP, double TN, double FP, double FN) {
		//MCC = ( (TP*TN) - (FP*FN) ) / SQRT((TP+FP)*(TP+FN)*(TN+FP)*(TN+FN))
		BigDecimal btp = new BigDecimal(TP);
		BigDecimal btn = new BigDecimal(TN);
		BigDecimal bfp = new BigDecimal(FP);
		BigDecimal bfn = new BigDecimal(FN);
		BigDecimal top = btp.multiply(btn).subtract(bfp.multiply(bfn)) ;
		BigDecimal a = (btp.add(bfp));
		BigDecimal b = (btp.add(bfn));
		BigDecimal c = (btn.add(bfp));
		BigDecimal d = (btn.add(bfn));
		BigDecimal bot = a.multiply(b.multiply(c).multiply(d));
		if(bot.compareTo(new BigDecimal(0))==0) {
			return "-"; //division by zero
		}else {
			BigDecimal mcc = top.divide(bot.sqrt(new MathContext(10)),3,RoundingMode.UP);
			return mcc.toString();
		}
	}
	
	public static void main(String[] args) {
		int start = 1;
		runVered(start);
		computeResults(start);
	}
}