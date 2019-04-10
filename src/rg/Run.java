package rg;

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
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import plans.FDPlan;
import plans.FDPlanner;
import plans.HSPFPlan;
import plans.HSPPlanner;
import plans.Plan;

public class Run {

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

	//ramirez geffener 2009
	public static HashMap<String, String> doPRPforObservationsWithFD(Observations obs, Domain dom, Problem problem, Hypotheses hyp, int inst, int scen, String out) {
		Domain domain = dom;
		String goalpredicate = "";
		HashMap<String, String> obsTolikelgoal = new HashMap<String, String>();
		for (int i=0; i<obs.getObs().size(); i++) {
			String now = obs.getObs().get(i);
			String prev = "";
			if(i>0) {
				prev = obs.getObs().get(i-1);
			}
			HashMap<String, Double> map = new HashMap<String, Double>();
			Domain copy = domain.compileObservation(now,prev);
			String obpred = copy.createPrediateFromObservation(now);
			Problem copyProb = null, negProb = null;
			goalpredicate += obpred;
			copy.writeDomainFile(out+"domain-compiled-"+i+".pddl");
			for (int j=0; j<hyp.getHyps().size(); j++) {
				double goalprob = 0.0;
				copyProb = problem.addPredicateToGoal(goalpredicate, hyp.getHyps().get(j)); //G+O
				negProb = copyProb.negateGoal(); //G + not O
				copyProb.writeProblemFile(out+"p_"+i+"_"+j+".pddl");
				negProb.writeProblemFile(out+"pneg_"+i+"_"+j+".pddl");
				if(TestConfigs.planner.contains("lama")) { //satisfising planner
					FDPlan gpluso = producePlansFD(copy, copyProb);
					FDPlan gnoto = producePlansFD(copy, negProb);
					goalprob = getGoalProbabilityGivenObservations(gpluso, gnoto);
				}else if(TestConfigs.planner.contains("hsp")) { //optimal planner
					HSPFPlan gpluso = producePlansHSP(copy, copyProb);
					HSPFPlan gnoto = producePlansHSP(copy, negProb);
					goalprob = getGoalProbabilityGivenObservations(gpluso, gnoto);
				}
				map.put(hyp.getHyps().get(j), goalprob);
			}
			Entry<String, Double> ent = maxLikelyGoal(map); //if ent = null, then the agent wasn't able to decide what the most likely goal is
			if(ent != null) {
				obsTolikelgoal.put(now, ent.getKey());
			}else {
				obsTolikelgoal.put(now, null);
			}
			domain = copy; //pass the domain from this round to the next observation
		}
		System.out.println(obsTolikelgoal);
		return obsTolikelgoal;
	}

	public static FDPlan producePlansFD(Domain dom, Problem prob) {
		FDPlanner fd = new FDPlanner(dom.getDomainPath(), prob.getProblemPath());
		FDPlan fdp =  fd.getFDPlan();
		fd.removeOutputFiles();
		return fdp;
	}

	public static HSPFPlan producePlansHSP(Domain dom, Problem prob) {
		HSPPlanner hp = new HSPPlanner(dom.getDomainPath(), prob.getProblemPath());
		return hp.getHSPPlan();
	}

	public static double getGoalProbabilityGivenObservations(Plan gpluso, Plan gnoto) { 
		//Pr(G|O) = alpha. Pr(O|G) . Pr(G)
		//Pr(O|G) is computed by plan cost difference. Assume uniform distribution for Pr(G)
		int costdiff = gpluso.getPlanCost() - gnoto.getPlanCost();
		double alpha = 1.0, beta = -1,  PrG = 1.0;
		double PrOG = (double)(Math.exp(beta*costdiff))/(double)(1+(Math.exp(beta*costdiff)));
		return alpha*PrOG*PrG;
	}

	public static Entry<String, Double> maxLikelyGoal(HashMap<String, Double> map) {
		Entry<String, Double> e = null;
		double max = Double.MIN_VALUE;
		Iterator<Entry<String, Double>> itr = map.entrySet().iterator();
		while(itr.hasNext()) {
			Entry<String, Double> ent = itr.next();
			if(ent.getValue()>max) {
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

	public static Hypotheses setHypothesis(String criticalfile, String desirablefile) {
		Hypotheses hyp = new Hypotheses();
		hyp.readHyps(criticalfile);
		hyp.readHyps(desirablefile);
		return hyp;
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

	public static void createDirectory(String outputpath, String obfilename) {
		new File(outputpath+obfilename+"/").mkdirs();
	}

	public static void runRandG() {
		for (int inst=1; inst<=TestConfigs.instances; inst++) { //blocks-3, navigator-3 easyipc-3, ferry-3 instances
			for (int scen=0; scen<TestConfigs.instanceCases; scen++) { //blocks,navigator,easyipc, ferry -each instance has 20 problems
				String desirables = TestConfigs.prefix + TestConfigs.instancedir + inst + TestConfigs.instscenario + scen + TestConfigs.desirableStateFile;
				String criticals = TestConfigs.prefix + TestConfigs.instancedir + inst + TestConfigs.instscenario + scen + TestConfigs.criticalStateFile;
				String testedObservations = TestConfigs.prefix + TestConfigs.instancedir + inst + TestConfigs.instscenario + scen + TestConfigs.testedObservationFiles;
				String actualObservations = TestConfigs.prefix + TestConfigs.instancedir + inst + TestConfigs.instscenario + scen + TestConfigs.observationFiles;
				String domfile = TestConfigs.prefix + TestConfigs.instancedir + inst + TestConfigs.instscenario + scen + TestConfigs.domainFile;
				String probfile = TestConfigs.prefix + TestConfigs.instancedir + inst + TestConfigs.instscenario + scen + TestConfigs.a_problemFile;
				String outdir = TestConfigs.prefix + TestConfigs.instancedir + inst + TestConfigs.instscenario + scen + TestConfigs.rgout + TestConfigs.planner;
				Hypotheses hyp = setHypothesis(criticals, desirables);
				TreeSet<String> obfiles = filterFiles(getFilesInPath(testedObservations), getFilesInPath(actualObservations));
				Iterator<String> itr = obfiles.iterator();
				while(itr.hasNext()) {
					String s = itr.next();
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
						HashMap<String, String> decisions = doPRPforObservationsWithFD(noLabel, dom, probTemplate, hyp, inst, scen, outdir+path[path.length-1]+"/");
						writeResultFile(decisions, obs, outdir+path[path.length-1]+"/"+TestConfigs.outputfile + "_" + path[path.length-1] + ".csv");
					} catch (CloneNotSupportedException e) {
						System.err.println(e.getMessage());
					}
				}
				break;
			}
			break;
		}
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

	//TNR,TPR,FNR,FPR values for R&G, using current planner
	public static void computeResults() {
		for (int inst=1; inst<=TestConfigs.instances; inst++) { //blocks-3, navigator-3 easyipc-3, ferry-3 instances
			int tp=0, tn=0, fp=0, fn = 0;
			for (int scen=0; scen<TestConfigs.instanceCases; scen++) { //blocks,navigator,easyipc, ferry -each instance has 20 problems
				String outdir = TestConfigs.prefix + TestConfigs.instancedir + inst + TestConfigs.instscenario + scen + TestConfigs.rgout + TestConfigs.planner;
				String desirables = TestConfigs.prefix + TestConfigs.instancedir + inst + TestConfigs.instscenario + scen + TestConfigs.desirableStateFile;
				String criticals = TestConfigs.prefix + TestConfigs.instancedir + inst + TestConfigs.instscenario + scen + TestConfigs.criticalStateFile;
				Hypotheses hyp = setHypothesis(criticals, desirables);
				TreeSet<String> paths = getFilesInPath(outdir);
				for (String s : paths) {
					if(s.contains("out_")) {
						ArrayList<String> result = readResultOutput(s);
						System.out.println("****"+result);
						tp+=countTP(result, hyp);
						tn+=countTN(result, hyp);
						fp+=countFP(result, hyp);
						fn+=countFN(result, hyp);
					}
				}
				break;
			}
			if(TestConfigs.planner.contains("lama")) {
				writeRatesToFile(tp, tn, fp, fn, TestConfigs.prefix+TestConfigs.instancedir + inst +TestConfigs.resultOutpath+"rg_lama.csv");//this is the tp, tn totals for the 20 cases for the current instance.
			}else if(TestConfigs.planner.contains("hsp")) {
				writeRatesToFile(tp, tn, fp, fn, TestConfigs.prefix+TestConfigs.instancedir + inst +TestConfigs.resultOutpath+"rg_hsp.csv");
			}
			break;
		}
	}

	public static void writeRatesToFile(int TP, int TN, int FP, int FN, String filename) {
		FileWriter writer = null;
		double tpr = (double) TP/(double) (TP+FN);
		double tnr = (double) TN/(double) (TN+FP);
		double fnr = (double) FN/(double) (TP+FN);
		double fpr = (double) FP/(double) (TN+FP);
		System.out.println(tpr+","+tnr+","+fnr+","+fpr);
		try {
			File file = new File(filename);
			writer = new FileWriter(file);
			writer.write("TPR,TNR,FPR,FNR"+"\n");
			writer.write(String.valueOf(tpr)+","+String.valueOf(tnr)+","+String.valueOf(fpr)+","+String.valueOf(fnr)+"\n");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public static int countTP(ArrayList<String> result, Hypotheses hyp) {
		int count = 0;
		String critical = hyp.getHyps().get(0);
		System.out.println("-----"+critical);
		for (String string : result) {
			String[] parts = string.split(",");
			System.out.println("#####"+parts[2]);
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
		System.out.println(desirable);
		for (String string : result) {
			String[] parts = string.split(",");
			System.out.println(parts[2]);
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
	
	public static void main(String[] args) {
		runRandG();
		computeResults();
	}
}
