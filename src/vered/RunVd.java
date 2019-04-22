package vered;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import con.ConnectivityGraph;
import landmark.RelaxedPlanningGraphGenerator;
import log.EventLogger;
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

	public static ArrayList<String> readLandmarks(String lmfile){
		Scanner sc;
		ArrayList<String> lms = new ArrayList<String>();	
		boolean start = false;
		try {
			sc = new Scanner(new File(lmfile));
			while(sc.hasNextLine()) {
				String line = sc.nextLine().trim();
				if(line.contains(":LGG GREEDY NECESSARY ORDERS")) {
					start = true;
				}
				if(start && !line.contains("LGG GREEDY NECESSARY ORDERS") && !line.isEmpty()) {
					lms.add(line.substring(1,line.length()-1));
				}
				if(start && line.contains("UNVERIFIED")) {
					start = false;
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
				String landmarkfile = TestConfigsVd.prefix + TestConfigsVd.instancedir + inst + TestConfigsVd.instscenario + scen + TestConfigsVd.lmfile;
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
				String confile = TestConfigsVd.prefix + TestConfigsVd.instancedir + inst + TestConfigsVd.instscenario + scen + TestConfigsVd.connectivityGraphFile;
				String rpgfile = TestConfigsVd.prefix + TestConfigsVd.instancedir + inst + TestConfigsVd.instscenario + scen + TestConfigsVd.rpgFile;
				Hypotheses hyp = setHypothesis(criticals, desirables);
				ConnectivityGraph con = readConnectivityGraphs(confile);
				ArrayList<String> runningstate = setInits(initfile); //set the state to init.
				ArrayList<String> critical = new ArrayList<String>();
				String [] parts = hyp.getHyps().get(0).split("\\)");
				for (String s : parts) {
					critical.add(s+")");
				}
				TreeSet<String> obfiles = filterFiles(getFilesInPath(testedObservations), getFilesInPath(actualObservations));
				redoLandmarks(rpgfile, confile, critical, runningstate, landmarkfile);
				ArrayList<String> landmarks = readLandmarks(landmarkfile);
				Iterator<String> itr = obfiles.iterator();
				while(itr.hasNext()) {
					String s = itr.next();
					EventLogger.LOGGER.log(Level.INFO, "Current file::   "+s);
					String path[] = s.split("/");
					createDirectory(outdir, path[path.length-1]);
					Domain dom = new Domain();
					dom.readDominPDDL(domfile);
					Problem probTemplate = new Problem();
					probTemplate.readProblemPDDL(probfile); //use the problem for the attacker's definition.
					Observations obs = new Observations(); //this one has the class labels
					obs.readObs(s);
					try {
						Observations noLabel = (Observations) obs.clone();
						noLabel.removeLabels();
						HashMap<String, String> decisions = doGoalMirroringWithLandmarks(noLabel, runningstate, dom, probTemplate, con, hyp, landmarks,
								outdir+path[path.length-1]+"/");
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
				}
				break;
			}
			break;
		}
	}

	public static HashMap<String, String> doGoalMirroringWithLandmarks(Observations obs, ArrayList<String> runningstate, 
			Domain dom, Problem prob, ConnectivityGraph con, Hypotheses hyp, ArrayList<String> lms, String outdir){ //prob comes from problem_a.txt
		HashMap<String, String> obsTolikelgoal = new HashMap<String, String>();
		TreeSet<String> achievedFL = new TreeSet<String>();
		TreeSet<String> activeFL = new TreeSet<String>();
		ArrayList<JavaFFPlan> i_k = new ArrayList<>();
		ArrayList<String> prefix = new ArrayList<>();
		String hypcur = hyp.getHyps().get(1); //only get desirable. because prob already has the undesirable hypothesis
		Problem copyProb = prob.replaceGoal(hypcur); 
		i_k.add(producePlansJavaFF(dom, prob)); //need to do it twice because I have two goals.
		i_k.add(producePlansJavaFF(dom, copyProb));
		for (int i=0; i<obs.getObs().size(); i++) {
			String now = obs.getObs().get(i);
			prefix.add(now);
			achieveLandmark(now, dom, con, runningstate, achievedFL, activeFL, lms);
			//			System.out.println(Arrays.toString(achievedFL.toArray()));
			//			System.out.println(Arrays.toString(activeFL.toArray()));
			for (String hp : hyp.getHyps()) {

			}
		}
		return obsTolikelgoal;
	}

	//observation is a state. 
	//achievedFL=facts that were satisfied before but no longer satisfied
	//activeFL = facts that are satisfied in current state
	//prune out goals from hyp if last achieved ordered landmark is associated with that goal
	//for the remaining goals, rank them by the decreasing order of percentage of achievedlandmarks.
	public static void achieveLandmark(String ob, Domain dom, ConnectivityGraph con, ArrayList<String> state,
			TreeSet<String> achievedFL, TreeSet<String> activeFL, ArrayList<String> landmarks) {
		ArrayList<String> obstate = convertObservationToState(state, dom, con, ob);
		if((!activeFL.isEmpty()) && (observationIntersectswithActiveFL(ob, dom, con, state, activeFL))) {
			achievedFL.addAll(activeFL);
			activeFL.clear();
		}else if(activeFL.isEmpty()) {
			for (String lm : landmarks) {
				for (String obs : obstate) {
					if(lm.equalsIgnoreCase(obs)) {
						achievedFL.add(lm);
					}
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
			EventLogger.LOGGER.log(Level.SEVERE, "ERROR:: JavaFF Plan not found");
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