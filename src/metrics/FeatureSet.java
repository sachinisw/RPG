package metrics;

import java.io.File;
import java.io.FileNotFoundException;
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

	//new: min action distance*, min causal link distance*, min state seq distance* for good (desirability) and bad (risk)
	//new: min generalized edit distance (state)*, min generalised edit distance (action)*
	//old: mean distance to critical state*, mean distance to desirable state*
	//comment: not all plans are optimal because of k value in top-k. mean distances (used in previous version) will push the value
	//closer to non optimal lengths. if I assume the agent to be optimal this movement is counterproductive. So use the min here also on the assumption of optimality?
	//(why does the agent have to be optimal? how would non-optimal agency change things?)
	//old: modified completed landmark percentage, modified landmark distance (domshlak/bryce)
	public FeatureSet(HashMap<ArrayList<String>, ArrayList<SASPlan>> altplans, 
			HashMap<ArrayList<String>, ArrayList<String>> refplans, ConnectivityGraph con, RelaxedPlanningGraph pg, ArrayList<String> current, 
			ArrayList<String> inits, ArrayList<String> c, ArrayList<String> d, String lmo) {
		alternativePlanSet = altplans;
		referencePlans = refplans;
		connectivity = con;
		criticalstate = c;
		desirablestate = d;
		init = inits;
		lmout = lmo;
		rpg = pg;
		currentstate = current;
		featurevals = new double[14]; 
	}

	public double getMinActionSetDistanceFromAltPlans(ArrayList<String> goal) {
		ArrayList<SASPlan> alts = alternativePlanSet.get(goal);
		ArrayList<String> ref = referencePlans.get(goal);
		double[] dists = new double[alts.size()];
		int index = 0;
		for (SASPlan sp : alts) {
			ActionSetDistance acd = new ActionSetDistance(ref, sp.getActions());
			dists[index++] = acd.getActionSetDistance();
		}
		Arrays.sort(dists);
		return dists[0];
	}
	
	public double getMinCausalLinkDistanceFromAltPlans(ArrayList<String> goal) {
		ArrayList<SASPlan> alts = alternativePlanSet.get(goal);
		ArrayList<String> ref = referencePlans.get(goal);
		double[] dists = new double[alts.size()];
		int index = 0;
		for (SASPlan sp : alts) {
			CausalLinkDistance cld = new CausalLinkDistance(ref, sp.getActions(), connectivity, init, goal);
			dists[index++] = cld.getCausalLinkDistance();
		}
		Arrays.sort(dists);
		return dists[0];
	}
	
	public double getMinStateSequenceDistanceFromAltPlans(ArrayList<String> goal) {
		ArrayList<SASPlan> alts = alternativePlanSet.get(goal);
		ArrayList<String> ref = referencePlans.get(goal);
		double[] dists = new double[alts.size()];
		int index = 0;
		for (SASPlan sp : alts) {
			StateSequenceDistance ssd = new StateSequenceDistance(ref, sp.getActions(), connectivity, init, goal);
			dists[index++] = ssd.getStateSequenceDistance();
		}
		Arrays.sort(dists);
		return dists[0];
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
			dists[index++] = ged.getGeneralizedEditSimilarity();
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
			dists[index++] = ged.getGeneralizedEditSimilarity();
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
	
	public int computeAchievedLandmarks() { //Karpaz,domshlak 2009 (cost optimal planning with landmarks)
		HashMap<String,TreeSet<String>> a_landmarks = readLandmarksGNOrders(lmout);
		OrderedLMGraph aGraph = new OrderedLMGraph();
		aGraph.produceOrders(a_landmarks, criticalstate);
		String s = "";
		for (String string : criticalstate) {
			s+=string;
		}
		OrderedLMNode criticalnode = new OrderedLMNode(s);
		//when counting landmarks, predicates that need to be together should be counted together. if not that landmark is not achieved
		//if i am seeing a higher-order predicate, also assume all predicates below it has been achieved. (ordered landmarks principle)
		HashMap<OrderedLMNode, TreeSet<OrderedLMNode>> adj = aGraph.getAdj();
		Iterator<OrderedLMNode> itr = adj.keySet().iterator();
		while(itr.hasNext()){
			OrderedLMNode key = itr.next();
			if(key.equals(criticalnode) && adj.get(key).isEmpty()) {
				//this is the root
				
			}
			
		}
		return 0;
	}
	
	public void evaluateFeatureValuesForCurrentObservation() {
		double r_actionsetdistance = getMinActionSetDistanceFromAltPlans(criticalstate);
		double d_actionsetdistance = getMinActionSetDistanceFromAltPlans(desirablestate);
		double r_causallinkdistance = getMinCausalLinkDistanceFromAltPlans(criticalstate);
		double d_causallinkdistance = getMinCausalLinkDistanceFromAltPlans(desirablestate);
		double r_stateseqdistance = getMinStateSequenceDistanceFromAltPlans(criticalstate);
		double d_stateseqdistance = getMinStateSequenceDistanceFromAltPlans(desirablestate);
		double r_minDistToCritical = getMinDistanceToState(criticalstate);
		double d_minDistToDesirable = getMinDistanceToState(desirablestate);
		double r_minEditDistanceAc = getActionGED(criticalstate);
		double d_minEditDistanceAc = getActionGED(desirablestate);
		double r_minEditDistanceSt = getStateGED(criticalstate);
		double d_minEditDistanceSt = getStateGED(desirablestate);
		double achievedLandmarks = computeAchievedLandmarks();
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
