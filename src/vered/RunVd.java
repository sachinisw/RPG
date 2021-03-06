package vered;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
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
import landmark.RelaxedPlanningGraphGenerator;
import log.EventLogger;
import plans.FFPlanner;
import plans.JavaFFPlan;
import plans.JavaFFPlanner;
import rg.Domain;
import rg.Hypotheses;
import rg.Observations;
import rg.Problem;


public class RunVd {

	//reads the observations corresponding to files in /data/decision
	public static TreeSet<String> getFilesInPath(String filepath){
		TreeSet<String> obFiles = new TreeSet<String>();
		try {
			File dir = new File(filepath);
			List<File> files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
			for (File fileItem : files) {
				obFiles.add(fileItem.getCanonicalPath());
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
		return obFiles;	
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

	public static HashMap<String,TreeSet<String>> readLandmarks(String lmfile){
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
	public static void runVered(int start, int mode) {
		for (int inst=start; inst<=TestConfigsVd.instances; inst++) { //blocks-3, navigator-3 easyipc-3, ferry-3 instances
			for (int scen=0; scen<TestConfigsVd.instanceCases; scen++) { //blocks,navigator,easyipc, ferry -each instance has 20 problems
				String landmarkfile = TestConfigsVd.prefix + TestConfigsVd.instancedir + inst + TestConfigsVd.instscenario + scen + TestConfigsVd.atLmfile;
				String ulandmarkfile = TestConfigsVd.prefix + TestConfigsVd.instancedir + inst + TestConfigsVd.instscenario + scen + TestConfigsVd.uLmfile;
				String desirables = TestConfigsVd.prefix + TestConfigsVd.instancedir + inst + TestConfigsVd.instscenario + scen + TestConfigsVd.desirableStateFile;
				String criticals = TestConfigsVd.prefix + TestConfigsVd.instancedir + inst + TestConfigsVd.instscenario + scen + TestConfigsVd.criticalStateFile;
				String initfile = TestConfigsVd.prefix + TestConfigsVd.instancedir + inst + TestConfigsVd.instscenario + scen + TestConfigsVd.a_initFile;
				String testedObservations = TestConfigsVd.prefix + TestConfigsVd.instancedir + inst + TestConfigsVd.instscenario + scen + TestConfigsVd.testedObservationFiles;
				String actualObservations = "";
				if(mode==TestConfigsVd.obFull) { //full trace
					actualObservations = TestConfigsVd.prefix + TestConfigsVd.instancedir + inst + TestConfigsVd.instscenario + scen + TestConfigsVd.observationFiles;
				}else if(mode==TestConfigsVd.ob50lm) { //50lm
					actualObservations = TestConfigsVd.prefix + TestConfigsVd.instancedir + inst + TestConfigsVd.instscenario + scen + TestConfigsVd.observation50Files;
				}else if(mode==TestConfigsVd.ob75lm) { //75lm
					actualObservations = TestConfigsVd.prefix + TestConfigsVd.instancedir + inst + TestConfigsVd.instscenario + scen + TestConfigsVd.observation75Files;  
				}
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
				generateRPGConForDesirableGoal(domfile, probfile, desirable, a_out);
				ConnectivityGraph a_con = readConnectivityGraphs(a_out + TestConfigsVd.a_connectivityGraphFile);
				ConnectivityGraph u_con = readConnectivityGraphs(a_out + TestConfigsVd.u_connectivityGraphFile);
				ArrayList<String> runningstate = setInits(initfile); //set the state to init.
				TreeSet<String> obfiles = filterFiles(getFilesInPath(testedObservations), getFilesInPath(actualObservations));
				redoLandmarks(a_out + TestConfigsVd.a_rpgFile, a_out + TestConfigsVd.a_connectivityGraphFile, critical, runningstate, landmarkfile);
				redoLandmarks(a_out + TestConfigsVd.u_rpgFile, a_out + TestConfigsVd.u_connectivityGraphFile, desirable, runningstate, ulandmarkfile);
				HashMap<String,TreeSet<String>> a_landmarks = readLandmarks(landmarkfile);
				HashMap<String,TreeSet<String>> u_landmarks = readLandmarks(ulandmarkfile);
				OrderedLMGraph aGraph = new OrderedLMGraph();
				OrderedLMGraph uGraph = new OrderedLMGraph();
				aGraph.produceOrders(a_landmarks, critical);
				uGraph.produceOrders(u_landmarks, desirable);
				Iterator<String> itr = obfiles.iterator();
				while(itr.hasNext()) {//iterates over observation files in the current scenario in the current instance
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
						HashMap<String, String> decisions = doGoalMirroringWithLandmarks(noLabel, runningstate, dom, probC, probD, a_con, 
								u_con, hyp, a_landmarks, u_landmarks, outdir+path[path.length-1]+"/");
						if(mode==TestConfigsVd.obFull) {
							writeResultFile(decisions, obs, outdir+path[path.length-1]+"/"+TestConfigsVd.outputfile + "_" + path[path.length-1] + ".csv");
						}else if (mode==TestConfigsVd.ob50lm) {
							writeResultFile(decisions, obs, outdir+path[path.length-1]+"/"+TestConfigsVd.outputfile + "_" + path[path.length-1] + "50.csv");
						}else if (mode==TestConfigsVd.ob75lm) {
							writeResultFile(decisions, obs, outdir+path[path.length-1]+"/"+TestConfigsVd.outputfile + "_" + path[path.length-1] + "75.csv");
						}
					} catch (CloneNotSupportedException e) {
						EventLogger.LOGGER.log(Level.SEVERE, "ERROR:: "+e.getMessage());
					}
					break; //just read one observation file. TODO: remove after debug
				}
				break;
			}
			break;
		}
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

	public static HashMap<String, String> doGoalMirroringWithLandmarks(Observations obs, ArrayList<String> state, 
			Domain dom, Problem pcri, Problem pdes, ConnectivityGraph a_con, ConnectivityGraph u_con, Hypotheses hyp, 
			HashMap<String,TreeSet<String>> lmsa, HashMap<String,TreeSet<String>> lmsd, String outdir){ //prob comes from problem_a.txt
		HashMap<String, String> obsTolikelgoal = new HashMap<String, String>();
		TreeSet<String> achievedAFL = new TreeSet<String>();
		TreeSet<String> activeAFL = new TreeSet<String>();
		TreeSet<String> achievedDFL = new TreeSet<String>();
		TreeSet<String> activeDFL = new TreeSet<String>();
		ArrayList<JavaFFPlan> i_k = new ArrayList<>();
		ArrayList<String> prefix = new ArrayList<>();
		i_k.add(producePlansJavaFF(dom, pcri)); //need to do it twice because I have two goals.
		i_k.add(producePlansJavaFF(dom, pdes));
		for (int i=0; i<obs.getObs().size(); i++) {
			HashMap<String, Double> goalranks = new HashMap<String, Double>();
			ArrayList<String> activeHypotheses = new ArrayList<>();
			String now = obs.getObs().get(i);
			prefix.add(now);
			ArrayList<String> obstate = convertObservationToState(state, dom, a_con, now); //to take add/del effects can take either one
			System.out.println(now);
			System.out.println(obstate);
			achieveLandmark(now, dom, a_con, obstate, achievedAFL, activeAFL, lmsa); //attack landmarks
			achieveLandmark(now, dom, u_con, obstate, achievedDFL, activeDFL, lmsd); //desirable landmarks
			for (int j=0; j<hyp.getHyps().size(); j++) { //wait until vered/ramon tells me how to prune hypotheses
				String goal = hyp.getHyps().get(j);
//				if(goal.contains("desirable")) {
//					if(!achievedDFL.contains(goal.substring(goal.indexOf(":")+1))) {
						activeHypotheses.add(goal);
//					}
//				}else if(!achievedAFL.contains(goal)) {
//					activeHypotheses.add(goal);
//				}
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
					prefix.addAll(suffix.getActions()); // now this is m_k
					if(activeHypotheses.get(j).contains("desirable")) {
						goalranks.put(activeHypotheses.get(j),(double)prefix.size()/(double)i_k.get(1).getPlanCost());
					}else {
						goalranks.put(activeHypotheses.get(j),(double)prefix.size()/(double)i_k.get(0).getPlanCost());
					}
					prefix.removeAll(suffix.getActions());
				}
				Entry<String, Double> ent = maxLikelyGoal(goalranks); //if ent = null, then the agent wasn't able to decide what the most likely goal is
				if(ent != null) {
					obsTolikelgoal.put(now, ent.getKey());
				}else {
					obsTolikelgoal.put(now, null);
				}
			}else { //all hypotheses are removed because of respective landmarks are active
				obsTolikelgoal.put(now, null);
			}
			state.clear();
			state.addAll(obstate);
		}
		return obsTolikelgoal;
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
	
	private static double computeNeta(HashMap<String, Double> goalranks) {
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
	//prune out goals from hyp if last achieved ordered landmark is associated with that goal
	//for the remaining goals, rank them by the decreasing order of percentage of achievedlandmarks.
	public static void achieveLandmark(String ob, Domain dom, ConnectivityGraph con, ArrayList<String> stateafterob,
			TreeSet<String> achievedAFL, TreeSet<String> activeAFL, HashMap<String, TreeSet<String>> landmarks) {
		if((!activeAFL.isEmpty()) && (observationIntersectswithActiveFL(ob, dom, con, stateafterob, activeAFL))) {
			achievedAFL.addAll(activeAFL);
			activeAFL.clear();
		}else if(activeAFL.isEmpty()) {
			Iterator<Entry<String,TreeSet<String>>> itrg = landmarks.entrySet().iterator();
			while(itrg.hasNext()) {
				Entry<String, TreeSet<String>> e = itrg.next();
				String cur = e.getKey();
				TreeSet<String> curval = e.getValue();
				boolean done = false;
				if(stateafterob.contains(cur) && curval.isEmpty()) {
					done = true;
				}else if(stateafterob.contains(cur) && !curval.isEmpty()) {
					int count = 0;
					for (String pred : curval) {
						for (String st : stateafterob) {
							if(st.equalsIgnoreCase(pred)) {
								count++;
							}
						}
					}
					if(count==curval.size()) {
						done = true;
					}
				}
				if(done) {
					activeAFL.add(cur);
				}
			}
		}
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

	public static boolean observationIntersectswithActiveFL(String ob, Domain dom, ConnectivityGraph con, 
			ArrayList<String> state, TreeSet<String> activeFL) {
		ArrayList<String> newstate = convertObservationToState(state, dom, con, ob);
		for (String string : activeFL) {
			for (String s : newstate) {
				if(string.equalsIgnoreCase(s)) {
					return true;
				}
			}
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
				String dec = decisions.get(ob);
				writer.write(o.substring(0,2)+","+ob+","+dec+"\n");
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
		if(ffp.getJavaFFPlan().getActions().isEmpty()) {
			EventLogger.LOGGER.log(Level.WARNING, "JavaFF Plan not found");
		}
		return ffp.getJavaFFPlan();
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

	public static void main(String[] args) {
		int start = 1;
		int mode = 1;
		runVered(start, mode);
	}
}