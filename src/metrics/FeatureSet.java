package metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import con.ConnectivityGraph;
import plans.SASPlan;

public class FeatureSet {

	private HashMap<ArrayList<String>, ArrayList<SASPlan>> alternativePlanSet; //1-critical, 2-desirable
	private HashMap<ArrayList<String>, ArrayList<String>> referencePlans; //1-critical optimal plan, 2-desirable optimal plan
	private ConnectivityGraph connectivity;
	private double[] featurevals;
	private ArrayList<String> criticalstate;
	private ArrayList<String> desirablestate;
	private ArrayList<String> init;

	
	public FeatureSet(HashMap<ArrayList<String>, ArrayList<SASPlan>> altplans, 
			HashMap<ArrayList<String>, ArrayList<String>> refplans, ConnectivityGraph con, ArrayList<String> inits, ArrayList<String> c, ArrayList<String> d) {
		alternativePlanSet = altplans;
		referencePlans = refplans;
		connectivity = con;
		criticalstate = c;
		desirablestate = d;
		init = inits;
		//new: min action distance, min causal link distance, min state seq distance for good (desirability) and bad (risk)
		//new: modified landmark distance (domshlak/bryce), min generalized edit distance (state), min generalised edit distance (action)
		//old: mean distance to critical state, mean distance to desirable state
		//old: modified completed landmark percentage
		featurevals = new double[10]; 
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
	
	public void evaluateFeatureValuesForCurrentObservation() {
		double r_actionsetdistance = getMinActionSetDistanceFromAltPlans(criticalstate);
		double d_actionsetdistance = getMinActionSetDistanceFromAltPlans(desirablestate);
		double r_causallinkdistance = getMinCausalLinkDistanceFromAltPlans(criticalstate);
		double d_causallinkdistance = getMinCausalLinkDistanceFromAltPlans(desirablestate);
		double r_stateseqdistance = getMinStateSequenceDistanceFromAltPlans(criticalstate);
		double d_stateseqdistance = getMinStateSequenceDistanceFromAltPlans(desirablestate);
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
}
