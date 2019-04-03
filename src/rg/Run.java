package rg;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import fdplan.FDPlan;
import fdplan.FDPlanner;

public class Run {

	public static void runObservationFile(Observations obs, Domain dom, Problem problem, Hypotheses hyp) {
		Domain domain = dom;
		String goalpredicate = "";
		HashMap<String, String> obsTolikelgoal = new HashMap<String, String>();
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
			copy.writeDomainFile("/home/sachini/domains/RG/domain-compiled-"+i+".pddl");
			for (int j=0; j<hyp.getHyps().size(); j++) {
				copyProb = problem.addPredicateToGoal(goalpredicate, hyp.getHyps().get(j)); //G+O
				negProb = copyProb.negateGoal(); //G + not O
				copyProb.writeProblemFile("/home/sachini/domains/RG/p_"+i+"_"+j+".pddl");
				negProb.writeProblemFile("/home/sachini/domains/RG/pneg_"+i+"_"+j+".pddl");
				FDPlan gpluso = producePlans(copy, copyProb);
				FDPlan gnoto = producePlans(copy, negProb);
				int diff = getPlanCostDifference(gpluso, gnoto);
				System.out.println(hyp.getHyps().get(j)+":  "+ now + "===" + diff);
				map.put(hyp.getHyps().get(j), diff);
			}
			Entry<String, Integer> ent = maxLikelyGoal(map);
			obsTolikelgoal.put(now, ent.getKey());
			if(i==0) {
				domain = copy; //pass the domain from this round to the next observation
			}
		}
		System.out.println(obsTolikelgoal);
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
		Iterator<Entry<String, Integer>> itr = map.entrySet().iterator();
		while(itr.hasNext()) {
			Entry<String, Integer> ent = itr.next();
			if(ent.getValue()>max) {
				e = ent;
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
	
	public static void main(String[] args) {
		int inst = 1; //per domain 1-3
		int scen = 0; // per instance 1-20
		int obf = 0;
		String desirables = TestConfigs.prefix + TestConfigs.instancedir + inst + TestConfigs.instscenario + scen + TestConfigs.desirableStateFile;
		String criticals = TestConfigs.prefix + TestConfigs.instancedir + inst + TestConfigs.instscenario + scen + TestConfigs.criticalStateFile;
		String observations = TestConfigs.prefix + TestConfigs.instancedir + inst + TestConfigs.instscenario + scen + TestConfigs.observationFiles + obf;
		String domfile = TestConfigs.prefix + TestConfigs.instancedir + inst + TestConfigs.instscenario + scen + TestConfigs.domainFile;
		String probfile = TestConfigs.prefix + TestConfigs.instancedir + inst + TestConfigs.instscenario + scen + TestConfigs.a_problemFile;
		Hypotheses hyp = setHypothesis(criticals, desirables);
		Observations obs = new Observations();
		obs.readObs(observations);
		Domain dom = new Domain();
		dom.readDominPDDL(domfile);
		Problem probTemplate = new Problem();
		probTemplate.readProblemPDDL(probfile); //use the problem for the attacker's definition.
//		String hypin = "/home/sachini/domains/RG/hyps";
//		String obsin = "/home/sachini/domains/RG/obs";
//		String originaldomain = "/home/sachini/domains/RG/domain.pddl";
//		String pFile = "/home/sachini/domains/RG/ptemplate.pddl";
		runObservationFile(obs, dom, probTemplate, hyp);
	}
}
