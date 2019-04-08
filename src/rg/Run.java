package rg;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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

	//ramirez geffener 2009
	public static HashMap<String, String> doPRPforObservationsWithFD(Observations obs, Domain dom, Problem problem, Hypotheses hyp, int inst, int scen) {
		Domain domain = dom;
		String goalpredicate = "";
		HashMap<String, String> obsTolikelgoal = new HashMap<String, String>();
		String out = TestConfigs.prefix + TestConfigs.instancedir + inst + TestConfigs.instscenario + scen + TestConfigs.rgout + TestConfigs.planner;
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
				System.out.println(hyp.getHyps().get(j)+":    ["+ now + "]    diff=" + goalprob);
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

	public static void runRandG() {
		for (int inst=1; inst<=TestConfigs.instances; inst++) { //blocks-3, navigator-3 easyipc-3, ferry-3 instances
			for (int scen=0; scen<TestConfigs.instanceCases; scen++) { //blocks,navigator,easyipc, ferry -each instance has 20 problems
				String desirables = TestConfigs.prefix + TestConfigs.instancedir + inst + TestConfigs.instscenario + scen + TestConfigs.desirableStateFile;
				String criticals = TestConfigs.prefix + TestConfigs.instancedir + inst + TestConfigs.instscenario + scen + TestConfigs.criticalStateFile;
				String observations = TestConfigs.prefix + TestConfigs.instancedir + inst + TestConfigs.instscenario + scen + TestConfigs.observationFiles;
				String domfile = TestConfigs.prefix + TestConfigs.instancedir + inst + TestConfigs.instscenario + scen + TestConfigs.domainFile;
				String probfile = TestConfigs.prefix + TestConfigs.instancedir + inst + TestConfigs.instscenario + scen + TestConfigs.a_problemFile;
				String outputfile = TestConfigs.prefix + TestConfigs.instancedir + inst + TestConfigs.instscenario + scen + TestConfigs.rgout + TestConfigs.planner + TestConfigs.outputfile + "_";
				TreeSet<String> obfiles = getObservationFiles(observations);
				for (int i=0; i<obfiles.size(); i++) {
					Domain dom = new Domain();
					dom.readDominPDDL(domfile);
					Problem probTemplate = new Problem();
					probTemplate.readProblemPDDL(probfile); //use the problem for the attacker's definition.
					Hypotheses hyp = setHypothesis(criticals, desirables);
					Observations obs = new Observations(); //this one has the class labels
					obs.readObs(obfiles.pollFirst());
					try {
						Observations noLabel = (Observations) obs.clone();
						noLabel.removeLabels();
						HashMap<String, String> decisions = doPRPforObservationsWithFD(noLabel, dom, probTemplate, hyp, inst, scen);
						writeResultFile(decisions, obs, outputfile + i + ".csv");
					} catch (CloneNotSupportedException e) {
						System.err.println(e.getMessage());
					}
					break;
				}
				break;
			}
			break;
		}
	}

	public static void main(String[] args) {
		runRandG();
	}
}
