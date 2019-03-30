package rg;

import fdplan.FDPlan;
import fdplan.FDPlanner;

public class Run {

	public static void runObservationFile(Observations obs, Domain dom, Problem problem) {
		Domain domain = dom;
		for (int i=0; i<obs.getObs().size(); i++) {
			String now = obs.getObs().get(i);
			String prev = "";
			if(i>0) {
				prev = obs.getObs().get(i-1);
			}
			Domain copy = domain.compileObservation(now,prev);
			String obpred = copy.createPrediateFromObservation(now);
			Problem copyProb = problem.addPredicateToGoal(obpred); //G+O
			Problem negProb = copyProb.negateGoal(); //G + not O
			copy.writeDomainFile("/home/sachini/domains/RG/domain-compiled-"+i+".pddl");
			copyProb.writeProblemFile("/home/sachini/domains/RG/p-compiled_"+i+".pddl");
			negProb.writeProblemFile("/home/sachini/domains/RG/pneg-compiled_"+i+".pddl");
			FDPlan gpluso = producePlans(copy, copyProb);
			FDPlan gnoto = producePlans(copy, negProb);
			int diff = getPlanCostDifference(gpluso, gnoto);
			System.out.println(now + "===" + diff);
			if(i==0) {
				domain = copy; //pass the domain from this round to the next observation
			}
			break;
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
		String pFile = "/home/sachini/domains/RG/p.pddl";
		Domain dom = new Domain();
		dom.readDominPDDL(originaldomain);
		Problem problem = new Problem();
		problem.readProblemPDDL(pFile);
		runObservationFile(obs, dom, problem);
	}

}
