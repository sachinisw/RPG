package rg;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import fdplan.FDPlan;
import fdplan.FDPlanner;

public class Run {

	//ramirez geffener 2009
	public static HashMap<String, String> doPRPforObservations(Observations obs, Domain dom, Problem problem, Hypotheses hyp, int inst, int scen) {
		Domain domain = dom;
		String goalpredicate = "";
		HashMap<String, String> obsTolikelgoal = new HashMap<String, String>();
		String out = TestConfigs.prefix + TestConfigs.instancedir + inst + TestConfigs.instscenario + scen + TestConfigs.rgout;
		for (int i=0; i<obs.getObs().size(); i++) {
			String now = obs.getObs().get(i);
			String prev = "";
			if(i>0) {
				prev = obs.getObs().get(i-1);
			}
			HashMap<String, Integer> map = new HashMap<String, Integer>();
			Domain copy = domain.compileObservation(now,prev);
			String obpred = copy.createPrediateFromObservation(now);
			Problem copyProb = null, negProb = null;
			goalpredicate += obpred;
			copy.writeDomainFile(out+"domain-compiled-"+i+".pddl");
			for (int j=0; j<hyp.getHyps().size(); j++) {
				copyProb = problem.addPredicateToGoal(goalpredicate, hyp.getHyps().get(j)); //G+O
				negProb = copyProb.negateGoal(); //G + not O
				copyProb.writeProblemFile(out+"p_"+i+"_"+j+".pddl");
				negProb.writeProblemFile(out+"pneg_"+i+"_"+j+".pddl");
				FDPlan gpluso = producePlans(copy, copyProb);
				FDPlan gnoto = producePlans(copy, negProb);
				int diff = getPlanCostDifference(gpluso, gnoto);
				System.out.println(hyp.getHyps().get(j)+":  "+ now + "===" + diff);
				map.put(hyp.getHyps().get(j), diff);
			}
			Entry<String, Integer> ent = maxLikelyGoal(map); //if ent = null, then the agent wasn't able to decide what the most likely goal is
			if(ent != null) {
				obsTolikelgoal.put(now, ent.getKey());
			}else {
				obsTolikelgoal.put(now, null);
			}
			if(i==0) {
				domain = copy; //pass the domain from this round to the next observation
			}
		}
		System.out.println(obsTolikelgoal);
		return obsTolikelgoal;
	}
	
	public static FDPlan producePlans(Domain dom, Problem prob) {
		FDPlanner fd = new FDPlanner(dom.getDomainPath(), prob.getProblemPath());
		return fd.getFDPlan();
	}
		
	public static int getPlanCostDifference(FDPlan gpluso, FDPlan gnoto) {
		return gpluso.getPlanCost() - gnoto.getPlanCost();
	}
	
	public static Entry<String, Integer> maxLikelyGoal(HashMap<String, Integer> map) {
		Entry<String, Integer> e = null;
		int max = Integer.MIN_VALUE;
		int count = 1; //check if there are ties. if there is a tie, that means the agent can't decide whats the likely goal.
		Iterator<Entry<String, Integer>> itr = map.entrySet().iterator();
		while(itr.hasNext()) {
			Entry<String, Integer> ent = itr.next();
			if(ent.getValue()>max) {
				count++;
				e = ent;
			}
			if(count>1) {
				e = null;
			}
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
		int inst = 1; //per domain 1-3
		int scen = 0; //per instance 1-20
		int obf = 0;
		String desirables = TestConfigs.prefix + TestConfigs.instancedir + inst + TestConfigs.instscenario + scen + TestConfigs.desirableStateFile;
		String criticals = TestConfigs.prefix + TestConfigs.instancedir + inst + TestConfigs.instscenario + scen + TestConfigs.criticalStateFile;
		String observations = TestConfigs.prefix + TestConfigs.instancedir + inst + TestConfigs.instscenario + scen + TestConfigs.observationFiles + obf;
		String domfile = TestConfigs.prefix + TestConfigs.instancedir + inst + TestConfigs.instscenario + scen + TestConfigs.domainFile;
		String probfile = TestConfigs.prefix + TestConfigs.instancedir + inst + TestConfigs.instscenario + scen + TestConfigs.a_problemFile;
		String outputfile = TestConfigs.prefix + TestConfigs.instancedir + inst + TestConfigs.instscenario + scen + TestConfigs.rgout + TestConfigs.outputfile + "_" + obf + ".csv";
		Domain dom = new Domain();
		dom.readDominPDDL(domfile);
		Problem probTemplate = new Problem();
		probTemplate.readProblemPDDL(probfile); //use the problem for the attacker's definition.
		Hypotheses hyp = setHypothesis(criticals, desirables);
		Observations obs = new Observations(); //this one has the class labels
		obs.readObs(observations);
		try {
			Observations noLabel = (Observations) obs.clone();
			noLabel.removeLabels();
			HashMap<String, String> decisions = doPRPforObservations(noLabel, dom, probTemplate, hyp, inst, scen);
			writeResultFile(decisions, obs, outputfile);
		} catch (CloneNotSupportedException e) {
			System.err.println(e.getMessage());
		}		
	}
	
	public static void main(String[] args) {
		runRandG();
	}
}
