package rg;

import fdplan.FDPlan;
import fdplan.FDPlanner;

public class Run {

	public static void runObservationFile(Observations obs, Domain dom, Problem problem, Hypotheses hyp) {
		Domain domain = dom;
		String goalpredicate = "";
		for (int i=0; i<obs.getObs().size(); i++) {
			String now = obs.getObs().get(i);
			String prev = "";
			if(i>0) {
				prev = obs.getObs().get(i-1);
			}
			Domain copy = domain.compileObservation(now,prev);
			String obpred = copy.createPrediateFromObservation(now);
			goalpredicate += obpred;
			System.out.println(goalpredicate);
			copy.writeDomainFile("/home/sachini/domains/RG/domain-compiled-"+i+".pddl");
			for (int j=0; j<hyp.hyps.size(); j++) {
				Problem copyProb = problem.addPredicateToGoal(goalpredicate, hyp.hyps.get(j)); //G+O
				Problem negProb = copyProb.negateGoal(); //G + not O
				copyProb.writeProblemFile("/home/sachini/domains/RG/p_"+i+"_"+j+".pddl");
				negProb.writeProblemFile("/home/sachini/domains/RG/pneg_"+i+"_"+j+".pddl");
				FDPlan gpluso = producePlans(copy, copyProb);
				FDPlan gnoto = producePlans(copy, negProb);
				int diff = getPlanCostDifference(gpluso, gnoto);
				System.out.println(hyp.hyps.get(j)+":  "+ now + "===" + diff);
			}
			if(i==0) {
				domain = copy; //pass the domain from this round to the next observation
			}
		}
	}
	
	public static FDPlan producePlans(Domain dom, Problem prob) {
		FDPlanner fd = new FDPlanner(dom.getDomainPath(), prob.getProblemPath());
		return fd.getFDPlan();
	}
	
	public static int getPlanCostDifference(FDPlan gpluso, FDPlan gnoto) {
		return gpluso.getPlanCost() - gnoto.getPlanCost();
	}
	
	public static void main(String[] args) {
		String hypin = "/home/sachini/domains/RG/hyps";
		String obsin = "/home/sachini/domains/RG/obs";
		String originaldomain = "/home/sachini/domains/RG/domain.pddl";
		Hypotheses hyp = new Hypotheses();
		Observations obs = new Observations();
		obs.readObs(obsin);
		hyp.readHyps(hypin);
		String pFile = "/home/sachini/domains/RG/ptemplate.pddl";
		Domain dom = new Domain();
		dom.readDominPDDL(originaldomain);
		Problem probTemplate = new Problem();
		probTemplate.readProblemPDDL(pFile);
		runObservationFile(obs, dom, probTemplate, hyp);
	}

}
