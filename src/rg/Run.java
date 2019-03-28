package rg;

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
			Problem copyProb = problem.addPredicateToGoal(obpred);
			Problem negProb = copyProb.negateGoal();
			copy.writeDomainFile("/home/sachini/domains/R&G/domain-compiled-"+i+".pddl");
			copyProb.writeProblemFile("/home/sachini/domains/R&G/p-compiled_"+i+".pddl");
			negProb.writeProblemFile("/home/sachini/domains/R&G/pneg-compiled_"+i+".pddl");
			if(i==0) {
				domain = copy; //pass the domain from this round to the next observation
			}
		}
	}
	
	public static void main(String[] args) {
		String hypin = "/home/sachini/domains/R&G/hyps";
		String obsin = "/home/sachini/domains/R&G/obs";
		String originaldomain = "/home/sachini/domains/R&G/domain.pddl";
		Hypotheses hyp = new Hypotheses();
		Observations obs = new Observations();
		obs.readObs(obsin);
		String pFile = "/home/sachini/domains/R&G/p.pddl";
		Domain dom = new Domain();
		dom.readDominPDDL(originaldomain);
		Problem problem = new Problem();
		problem.readProblemPDDL(pFile);
		runObservationFile(obs, dom, problem);
	}

}
