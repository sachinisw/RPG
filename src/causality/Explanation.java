package causality;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import con.ConnectivityGraph;
import landmark.RelaxedPlanningGraph;
import metrics.CausalLink;
import plans.SASPlan;

public class Explanation {
	//explanation module
	//Template Observer assumes the undesirable state can be reached via ref-plan
	//Statement 1 : [obs] enables [findEnablers()] // because
	//Statement 2 : Actions executed thus far has enabled [findActiveSatisfiers()] //give me active state
	//Statement 3 : Undesirable state requires [findSatisfiersOfUndesirableState()]
	//Statement 4 : What steps should take place to reach undesirable state from current state
	public static void explain(String observation, ArrayList<String> causalstate, ArrayList<CausalLink> refPlanCausal, 
			HashMap<ArrayList<String>, ArrayList<SASPlan>> altplans, HashMap<ArrayList<String>, ArrayList<String>> refplans,
			ConnectivityGraph con, RelaxedPlanningGraph rpg, ArrayList<String> init, ArrayList<String> currentstate, 
			ArrayList<String> critical, ArrayList<String> desirable, String lm_out) {
		CausalGraph cg = new CausalGraph(currentstate);
		cg.generateCausalGraph(refPlanCausal);
		ArrayList<CGEdge> enablers = cg.findImmediateEnablers(observation);
		ArrayList<CGEdge> goalsatisfier = cg.findSatisfiersOfUndesirableState();
		ArrayList<ArrayList<CGNode>>  longterm = cg.findLongTermEnablers(observation);
		ArrayList<CGEdge> alreadysatisfied = cg.findActiveSatisfiers(observation);
		ArrayList<CGEdge> contributors = cg.findContributorsToGoalInPath(longterm);
		TreeSet<String> state = cg.modifyStateThroughPath(alreadysatisfied, enablers, causalstate);
		System.out.println(causesToText(observation,enablers,state, goalsatisfier, longterm, contributors));
	}

	public static String causesToText(String obs, ArrayList<CGEdge> immediateEn, TreeSet<String> state, 
			ArrayList<CGEdge> goalsatisfier, ArrayList<ArrayList<CGNode>> longtermEn, ArrayList<CGEdge> contributors) {
		String enablingac = "", g = "";
		for (CGEdge cgEdge : immediateEn) { //immediate enabler
			enablingac += cgEdge.getTo().toString();
		}
		for (CGEdge cgEdge : goalsatisfier) {
			g += cgEdge.getEdgeLabel()+" ";
		}
		ArrayList<CGEdge> missing = new ArrayList<>();
		for (CGEdge cgEdge : goalsatisfier) { //which goal preconds are missing from currentstate?
			if(!state.contains(cgEdge.getEdgeLabel())){
				missing.add(cgEdge);
			}
		}
		boolean reachableImmediately = false;
		CGEdge connection = null;
		String statement4 = "\n";
		for (CGEdge cgEdge : missing) { //does immediate enablers add missing preconds?
			if(immediateEn.contains(cgEdge)){
				reachableImmediately = true;
				connection = cgEdge;
				break;
			}
		}
		String causes = "";
		for (CGEdge e : contributors) {
			causes += e.getEdgeLabel();
		}
		if(reachableImmediately) { //reachable in one step
			statement4 += g + " is immediately reachable by action " + connection.getFrom();
		}else { //reachable long term
			statement4 += obs + " may eventually lead to goal requirement by enabling "+ causes + " with "+ longtermEn; //longtermen is only the path current -> A_G
		}
		String statement1 = obs + " leads to " + enablingac  + " because ";
		String statement2 = "observed actions have enabled " + state;
		String statement3 = "\n undesirable state is satisfied by " + g;
		return "============================\n"+statement1+statement2+statement3+statement4
				+"\n============================";
	}
}
