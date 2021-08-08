package train;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import con.ConnectivityGraph;
import plans.SASPlan;
import plans.TopKPlanner;
import run.CriticalState;
import run.DesirableState;
import run.InitialState;

public class TraceGeneratorDepot {
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
	
	public static DesirableState getUserGoal(String desirable) {
		DesirableState d = new DesirableState(desirable);
		d.readStatesFromFile();
		return d;
	}
	
	public static CriticalState getAttackerGoal(String critical) {
		CriticalState c = new CriticalState(critical);
		c.readCriticalState();
		return c;
	}
	
	public static InitialState getInitialState(String init) {
		InitialState in = new InitialState();
		in.readInitsFromFile(init);
		return in;
	}
	
	public static void generateTopKPlans(String domainfile, String problem_a, String problem_u, CriticalState c, DesirableState d) {
		int K=3;
		HashMap<ArrayList<String>, ArrayList<SASPlan>> alts = new HashMap<>();
		TopKPlanner tka = new TopKPlanner(domainfile, problem_a, K );
		ArrayList<SASPlan> atplans = tka.getPlans();
		TopKPlanner tku = new TopKPlanner(domainfile, problem_u, K);
		ArrayList<SASPlan> uplans = tku.getPlans();
		alts.put(c.getCriticalStatePredicates(),atplans);
		alts.put(d.getDesirableStatePredicates(),uplans);
	}
	
	public static void labelObservationTrace() {
		String domain = "";
		String init = "";
		String connectivity = "";
		String critical = "";
		ArrayList<String> runningstate = new ArrayList<String>();
		
	}
	
	public ArrayList<String> updateStateForAction(String action, ArrayList<String> currentState, ConnectivityGraph conGraph){
		Set<String> set = new HashSet<String>();	//no duplicates
		ArrayList<String> add = conGraph.findStatesAddedByAction(action);
		ArrayList<String> del = conGraph.findStatesDeletedByAction(action);
		set.addAll(currentState); 		//copy currentstate to newState
		for(int i=0; i<del.size(); i++){	//remove del from newstate
			set.remove(del.get(i));
		}
		for(int i=0; i<add.size(); i++){	//add add to newState
			set.add(add.get(i));
		}
		ArrayList<String> newState = new ArrayList<String>();
		newState.addAll(set);
		return newState;
	}
	
	public static void generateAuxFiles(String domain, String problem_a, String problem_u, String out_a, String out_u) { //for user and attacker produce FF connectivity
		FFPlanner ff_a = new FFPlanner(domain, problem_a, out_a);
		FFPlanner ff_u = new FFPlanner(domain, problem_u, out_u);

	}
	
	public static void main(String[] args) {
		String domainfile = "/home/sachini/domains/DEPOT/scenarios/0/train/cases/0/domain.pddl";
		String problem_a = "/home/sachini/domains/DEPOT/scenarios/0/train/cases/0/problem_a.pddl";
		String problem_u = "/home/sachini/domains/DEPOT/scenarios/0/train/cases/0/problem_u.pddl";
		String atgoal = "/home/sachini/domains/DEPOT/scenarios/0/train/cases/0/critical.txt";
		String ugoal = "/home/sachini/domains/DEPOT/scenarios/0/train/cases/0/desirable.txt";
		String init_a = "/home/sachini/domains/DEPOT/scenarios/0/train/cases/0/init_a.txt";
		String init_u = "/home/sachini/domains/DEPOT/scenarios/0/train/cases/0/init_u.txt";
		DesirableState d = getUserGoal(ugoal);
		CriticalState  c = getAttackerGoal(atgoal);
		InitialState in_a = getInitialState(init_a);
		InitialState in_u = getInitialState(init_u);
		generateTopKPlans(domainfile, problem_a, problem_u, c, d);
	}
}
