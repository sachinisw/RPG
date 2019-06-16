package metrics;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.TreeSet;

import con.ConnectivityGraph;
import landmark.OrderedLMGraph;
import landmark.OrderedLMNode;
import landmark.RelaxedPlanningGraph;
import plans.SASPlan;

public class FeatureSet {

	private HashMap<ArrayList<String>, ArrayList<SASPlan>> alternativePlanSet; //1-critical, 2-desirable
	private HashMap<ArrayList<String>, ArrayList<String>> referencePlans; //1-critical optimal plan, 2-desirable optimal plan
	private ConnectivityGraph connectivity;
	private RelaxedPlanningGraph rpg;
	private double[] featurevals;
	private ArrayList<String> criticalstate;
	private ArrayList<String> desirablestate;
	private ArrayList<String> init;
	private ArrayList<String> currentstate;
	private String lmout;

	//new: median action distance*, median causal link distance*, median state seq distance* for good (desirability) and bad (risk)
	//new: median generalized edit distance (state)*, median generalised edit distance (action)*
	//old: median distance to critical state*, median distance to desirable state*
	//comment: not all plans are optimal because of k value in top-k. mean distances (used in previous version) will push the value
	//closer to non optimal lengths. if I assume the agent to be optimal this movement is counterproductive. 
	//If i assumed the minimum, even if there is just one identical plan (distance=0) i will be saying the ref is matching the good/bad goal plan.
	//what I want is to identify when most of the sample is producing identical plans to my ref (good/bad) plan. in this case i am somewhat confident about the risk/desirablity of the ref.plan
	//So use the median	//(why does the agent have to be optimal? how would non-optimal agency change things?)
	//old: modified completed landmark percentage (meneguzzi2017), modified landmark distance (domshlak/bryce)
	public FeatureSet(HashMap<ArrayList<String>, ArrayList<SASPlan>> altplans, 
			HashMap<ArrayList<String>, ArrayList<String>> refplans, ConnectivityGraph con, RelaxedPlanningGraph pg, 
			ArrayList<String> inits, ArrayList<String> current, ArrayList<String> c, ArrayList<String> d, String lmo) {
		alternativePlanSet = altplans;
		referencePlans = refplans;
		connectivity = con;
		criticalstate = c;
		desirablestate = d;
		init = inits;
		lmout = lmo;
		rpg = pg;
		currentstate = current;
		featurevals = new double[13]; 
	}

	private double findMedianDistance(double [] dists) {
		Arrays.sort(dists);
		if(dists.length % 2 != 0) { 		// check for even case 
			return (double)dists[dists.length/2]; 
		}
		return (double)(dists[(dists.length-1)/2] + dists[dists.length/2]) / 2.0; 
	}

	public double getMedianActionSetDistanceFromAltPlans(ArrayList<String> goal) {
		ArrayList<SASPlan> alts = alternativePlanSet.get(goal);
		ArrayList<String> ref = referencePlans.get(goal);
		double[] dists = new double[alts.size()];
		int index = 0;
		for (SASPlan sp : alts) {
			ActionSetDistance acd = new ActionSetDistance(ref, sp.getActions());
			dists[index++] = acd.getActionSetDistance();
		}
		return findMedianDistance(dists);
	}

	public double getMedianCausalLinkDistanceFromAltPlans(ArrayList<String> goal) {
		ArrayList<SASPlan> alts = alternativePlanSet.get(goal);
		ArrayList<String> ref = referencePlans.get(goal);
		double[] dists = new double[alts.size()];
		int index = 0;
		for (SASPlan sp : alts) {
			CausalLinkDistance cld = new CausalLinkDistance(ref, sp.getActions(), connectivity, init, goal);
			dists[index++] = cld.getCausalLinkDistance();
		}
		return findMedianDistance(dists);
	}

	public double getMedianStateSequenceDistanceFromAltPlans(ArrayList<String> goal) {
		ArrayList<SASPlan> alts = alternativePlanSet.get(goal);
		ArrayList<String> ref = referencePlans.get(goal);
		double[] dists = new double[alts.size()];
		int index = 0;
		for (SASPlan sp : alts) {
			StateSequenceDistance ssd = new StateSequenceDistance(ref, sp.getActions(), connectivity, init, goal);
			dists[index++] = ssd.getStateSequenceDistance();
		}
		return findMedianDistance(dists);
	}

	public double getMinDistanceToState(ArrayList<String> goal) {
		ArrayList<SASPlan> alts = alternativePlanSet.get(goal);
		ArrayList<String> ref = referencePlans.get(goal);
		double[] dists = new double[alts.size()];
		int index = 0;
		for (SASPlan sp : alts) {
			StateSequenceDistance ssd = new StateSequenceDistance(ref, sp.getActions(), connectivity, init, goal);
			dists[index++] = ssd.minDsistanceToGoal();
		}
		Arrays.sort(dists);
		return dists[0];
	}

	public double getActionGED(ArrayList<String> goal) {
		ArrayList<SASPlan> alts = alternativePlanSet.get(goal);
		ArrayList<String> ref = referencePlans.get(goal);
		double[] dists = new double[alts.size()];
		int index = 0;
		for (SASPlan sp : alts) {
			GeneralizedEditDistance ged = new GeneralizedEditDistance(ref, sp.getActions());
			dists[index++] = ged.getGeneralizedEditSimilarity(1);
		}
		Arrays.sort(dists);
		return dists[0];
	}

	public double getStateGED(ArrayList<String> goal) {
		ArrayList<SASPlan> alts = alternativePlanSet.get(goal);
		ArrayList<String> ref = referencePlans.get(goal);
		double[] dists = new double[alts.size()];
		int index = 0;
		for (SASPlan sp : alts) {
			StateSequenceDistance ssd = new StateSequenceDistance(ref, sp.getActions(), connectivity, init, goal);
			ssd.producePlanStateSeq();
			ArrayList<String> reftoks = new ArrayList<String>();
			ArrayList<String> intoks = new ArrayList<String>();
			for (State s : ssd.refseq) {
				String tok = "";
				for (String st : s.getPredicates()) {
					tok+=st;
				}
				reftoks.add(tok);
			}
			for (State s : ssd.inseq) {
				String tok = "";
				for (String st : s.getPredicates()) {
					tok+=st;
				}
				intoks.add(tok);
			}
			GeneralizedEditDistance ged = new GeneralizedEditDistance(reftoks, intoks);
			dists[index++] = ged.getGeneralizedEditSimilarity(2);
		}
		Arrays.sort(dists);
		return dists[0];
	}

	public HashMap<String,TreeSet<String>> readLandmarksGNOrders(String lmfile){
		Scanner sc;
		HashMap<String,TreeSet<String>> lms = new HashMap<String, TreeSet<String>>();	
		boolean start = false;
		try {
			sc = new Scanner(new File(lmfile));
			while(sc.hasNextLine()) {
				String line = sc.nextLine().trim();
				if(line.contains(":LGG GREEDY NECESSARY ORDERS")) {
					start = true;
				}
				if(start && line.contains("UNVERIFIED")) {
					start = false;
				}
				if(start && !line.contains("LGG GREEDY NECESSARY ORDERS") && !line.isEmpty()) {
					String parts [] = line.split(":");
					String key = parts[0].substring(parts[0].indexOf("[")+1,parts[0].indexOf("]"));
					if(!lms.containsKey(key)) {
						lms.put(key,new TreeSet<String>());
					}
					TreeSet<String> set = lms.get(key);
					String val = parts[1].substring(2,parts[1].length()-1);
					if(!val.isEmpty()) {
						String valparts[] = val.split(",");
						for (String s : valparts) {
							set.add(s.substring(s.indexOf("[")+1,s.indexOf("]")));
						}
					}
				}
			}
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return  lms;
	}

	public double computeAchievedLandmarks(ArrayList<String> goal, ArrayList<String> currentstate) {  //Meneguzzi 2017 landmark based goal completion heuristic
		HashMap<String,TreeSet<String>> a_landmarks = readLandmarksGNOrders(lmout);
		HashMap<OrderedLMNode, Boolean> active = new HashMap<OrderedLMNode, Boolean>();
		OrderedLMGraph aGraph = new OrderedLMGraph(goal);
		aGraph.produceOrders(a_landmarks, goal);
		//when counting landmarks, predicates that need to be together should be counted together. if not that landmark is not achieved
		//if i am seeing a higher-order predicate, also assume all predicates below it has been achieved. (ordered landmarks principle)
		HashMap<OrderedLMNode, TreeSet<OrderedLMNode>> orders = aGraph.getAdj();
		Iterator<OrderedLMNode> itr = orders.keySet().iterator();
		while(itr.hasNext()){
			OrderedLMNode key = itr.next();
			active.put(key, false);
		}
		//iterate over the current state. mark each predicate as active true/false
		Iterator<OrderedLMNode> acitr = active.keySet().iterator();
		while(acitr.hasNext()){
			OrderedLMNode key = acitr.next();
			for (String string : currentstate) {
				if(key.getNodecontent().contains(string)) {
					active.put(key, true);
				}
			}
		}//iterate over active nodes and find each node's all siblings. mark them as active.
		//since landmarks are ordered, if higher node is true, all it's siblings must assume to be achieved
		Iterator<OrderedLMNode> ac = active.keySet().iterator();
		ArrayList<OrderedLMNode> allTrue = new ArrayList<OrderedLMNode>();
		HashMap<OrderedLMNode, ArrayList<OrderedLMNode>> nodesiblngs = new HashMap<OrderedLMNode, ArrayList<OrderedLMNode>>();
		while(ac.hasNext()){
			OrderedLMNode key = ac.next();
			ArrayList<OrderedLMNode> sib = aGraph.findAllSiblingsofNode(key);
			if(active.get(key)) {
				allTrue.addAll(sib);
			}
			nodesiblngs.put(key,sib);
		}//mark everything in allTrue as active
		Iterator<OrderedLMNode> acTrue = active.keySet().iterator();
		while(acTrue.hasNext()){
			OrderedLMNode key = acTrue.next();
			if(allTrue.contains(key)) {
				active.put(key, true);
			}
		}//mark everything in allTrue as active
		aGraph.assignSiblingLevels();
		HashMap<OrderedLMNode, ArrayList<ArrayList<OrderedLMNode>>> subgoallevels = aGraph.getLevelsPerSubgoal();
		HashMap<OrderedLMNode, Integer> lmCompleteLevels = getMaxCompleteLevel(subgoallevels,active);
		HashMap<OrderedLMNode, Integer> subGoalLevels = getSubgoalLevels(subgoallevels);
		return computeLandmarkCompletionHeuristic(lmCompleteLevels, subGoalLevels);
	}

	public double computeLandmarkCompletionHeuristic(HashMap<OrderedLMNode, Integer> lmcompletelevels, HashMap<OrderedLMNode, Integer> subgoallevels) {	
		double numofsubgoals = subgoallevels.size(); 
		double sum = 0.0;
//		System.out.println("$$  "+lmcompletelevels);System.out.println("@@   "+subgoallevels);
		Iterator<OrderedLMNode> itr = subgoallevels.keySet().iterator();
		while(itr.hasNext()) {
			OrderedLMNode subgoal = itr.next(); //lmcompletelevels shows for each subgoal, how many of it's subtree levels are complete
			double complete = lmcompletelevels.get(subgoal); //complete can be negative. That means the levels are not complete. reset to zero
			double levels = subgoallevels.get(subgoal);
//			System.out.println(subgoal +"   "+complete + "|"+ levels);
			sum += (levels-complete)/levels;
		}
		return sum/numofsubgoals;
	}

	public HashMap<OrderedLMNode, Integer> getMaxCompleteLevel(HashMap<OrderedLMNode, ArrayList<ArrayList<OrderedLMNode>>> subgoallevels, 
			HashMap<OrderedLMNode, Boolean> active) {
		HashMap<OrderedLMNode, Integer> lmCompletedLevel = new HashMap<>();//contains the highest fully complete tree level
		Iterator<OrderedLMNode> itr = subgoallevels.keySet().iterator();
		while(itr.hasNext()) {
			OrderedLMNode key = itr.next();
			ArrayList<ArrayList<OrderedLMNode>> levels = subgoallevels.get(key);
//			System.out.println("current subgoal=="+key);
			if(active.get(key)) { //subgoal root is active. assume everything below is also active. why? Landmarks are partial orders
//				System.out.println("goal complete");
				lmCompletedLevel.put(key, 0);
			}else { //subgoal tree is half complete. first find that half complete level. go from top to bottom in the current subtree
//				System.out.println("half tree complete");
				if(levels.size()>0) { //subtree has a few levels. one of these levels is half complete
					int completeLevel =  levels.size()-1;
					for (int i=levels.size()-1; i>=0; i--) { //move from bottom up. when this loop finishes, I will have the highest level with all active nodes
						ArrayList<OrderedLMNode> level = levels.get(i);
						int curlevelcompletenodes = 0;
						for (OrderedLMNode node : level) {
							if(active.get(node)) {
								curlevelcompletenodes++;
							}
						}
//						System.out.println("curlevelcompletenodes="+curlevelcompletenodes);
						if(curlevelcompletenodes==level.size()) { //if completelevel=0 that means the tree is fully done. complete level=1 means all but root is complete.
//							System.out.println("current level is fully complete ");
							completeLevel = i;
						}
					}
					lmCompletedLevel.put(key, completeLevel+1); //i+1 subgoallevels structure doesn't have the 0th (goal) level.
				}else { //this subgoal doesn't have children
//					System.out.println("no children subgoal");
					if(active.get(key)) {
						lmCompletedLevel.put(key, 0); 
					}else {
						lmCompletedLevel.put(key, -1); 
					}
				}
			}
//			System.out.println("......."+lmCompletedLevel);
		}
		return lmCompletedLevel;
	}

	public HashMap<OrderedLMNode, Integer> getSubgoalLevels(HashMap<OrderedLMNode, ArrayList<ArrayList<OrderedLMNode>>> subgoallevels) {
		HashMap<OrderedLMNode, Integer> lmLevels = new HashMap<>();
		Iterator<OrderedLMNode> itr = subgoallevels.keySet().iterator();
		while(itr.hasNext()) {
			OrderedLMNode key = itr.next();
			ArrayList<ArrayList<OrderedLMNode>> levels = subgoallevels.get(key);
			if(!levels.isEmpty()) {
				lmLevels.put(key, levels.size()+1);
			}else {
				lmLevels.put(key, 1);
			}
		}
		return lmLevels;
	}

	public void evaluateFeatureValuesForCurrentObservation() {
//				System.out.println(alternativePlanSet);System.out.println(referencePlans);
		double r_actionsetdistance = getMedianActionSetDistanceFromAltPlans(criticalstate); //ok
		double d_actionsetdistance = getMedianActionSetDistanceFromAltPlans(desirablestate); //ok
		double r_causallinkdistance = getMedianCausalLinkDistanceFromAltPlans(criticalstate); //ok
		double d_causallinkdistance = getMedianCausalLinkDistanceFromAltPlans(desirablestate); //ok
		double r_stateseqdistance = getMedianStateSequenceDistanceFromAltPlans(criticalstate); //ok
		double d_stateseqdistance = getMedianStateSequenceDistanceFromAltPlans(desirablestate); //ok
		double r_minDistToCritical = getMinDistanceToState(criticalstate); //ok
		double d_minDistToDesirable = getMinDistanceToState(desirablestate); //ok
		double r_minEditDistanceAc = getActionGED(criticalstate); //on paper definition ok. could improve
		double d_minEditDistanceAc = getActionGED(desirablestate);//on paper definition ok.
		double r_minEditDistanceSt = getStateGED(criticalstate); //ok
		double d_minEditDistanceSt = getStateGED(desirablestate);//ok
		double r_achievedLandmarks = computeAchievedLandmarks(criticalstate,currentstate);//ok
		featurevals[0] = r_actionsetdistance; //R1
		featurevals[1] = d_actionsetdistance; //D1 
		featurevals[2] = r_causallinkdistance; //R2
		featurevals[3] = d_causallinkdistance; //D2
		featurevals[4] = r_stateseqdistance; //R3
		featurevals[5] = d_stateseqdistance; //D3
		featurevals[6] = r_minDistToCritical; //R4
		featurevals[7] = d_minDistToDesirable; //D4
		featurevals[8] = r_minEditDistanceAc; //R5
		featurevals[9] = d_minEditDistanceAc; //D5
		featurevals[10] = r_minEditDistanceSt; //R6 
		featurevals[11] = d_minEditDistanceSt; //D6 
		featurevals[12] = r_achievedLandmarks;//R7
		DecimalFormat df = new DecimalFormat("#.##");
		int index = 0;
		for (double d : featurevals) {
			String formatted = df.format(d);
			featurevals[index++] = Double.valueOf(formatted);
		}
	}

	public HashMap<ArrayList<String>, ArrayList<SASPlan>> getAlternativePlanSet() {
		return alternativePlanSet;
	}

	public void setAlternativePlanSet(HashMap<ArrayList<String>, ArrayList<SASPlan>> alternativePlanSet) {
		this.alternativePlanSet = alternativePlanSet;
	}

	public HashMap<ArrayList<String>, ArrayList<String>> getReferencePlanSet() {
		return referencePlans;
	}

	public void setReferencePlanSet(HashMap<ArrayList<String>, ArrayList<String>> referencePlanSet) {
		this.referencePlans = referencePlanSet;
	}

	public ConnectivityGraph getConnectivity() {
		return connectivity;
	}

	public void setConnectivity(ConnectivityGraph connectivity) {
		this.connectivity = connectivity;
	}

	public double[] getFeaturevals() {
		return featurevals;
	}

	public void setFeaturevals(double[] featurevals) {
		this.featurevals = featurevals;
	}

	public ArrayList<String> getCriticalstate() {
		return criticalstate;
	}

	public void setCriticalstate(ArrayList<String> criticalstate) {
		this.criticalstate = criticalstate;
	}

	public ArrayList<String> getDesirablestate() {
		return desirablestate;
	}

	public void setDesirablestate(ArrayList<String> desirablestate) {
		this.desirablestate = desirablestate;
	}

	public String getLmout() {
		return lmout;
	}

	public void setLmout(String lmout) {
		this.lmout = lmout;
	}

	public RelaxedPlanningGraph getRpg() {
		return rpg;
	}

	public void setRpg(RelaxedPlanningGraph rpg) {
		this.rpg = rpg;
	}

	public HashMap<ArrayList<String>, ArrayList<String>> getReferencePlans() {
		return referencePlans;
	}

	public void setReferencePlans(HashMap<ArrayList<String>, ArrayList<String>> referencePlans) {
		this.referencePlans = referencePlans;
	}

	public ArrayList<String> getInit() {
		return init;
	}

	public void setInit(ArrayList<String> init) {
		this.init = init;
	}

}
