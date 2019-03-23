package run;

import java.text.DecimalFormat;
import java.util.ArrayList;
import actors.Decider;
import con.ConnectivityGraph;
import graph.StateVertex;
import landmark.RelaxedPlanningGraph;


public class Metrics {
	private Decider decider;
	private double [] crd;
	private int distanceToCritical;
	private int distanceToDesirable;
	private double stateContainsAttackLm;
	private double statesAddingLM;
	private String domain;
	private String lmoutputpath;
	private RelaxedPlanningGraph arpg;
	private ConnectivityGraph con;
	
	public Metrics(Decider d, String dom, String lmoutput, RelaxedPlanningGraph r, ConnectivityGraph c){
		this.decider = d;
		domain = dom;
		crd = new double[3];
		distanceToCritical = 0;
		distanceToDesirable = 0;
		stateContainsAttackLm = 0.0; 
		statesAddingLM = 0.0;
		lmoutputpath = lmoutput;
		arpg = r;
		con = c;
	}
	
	public void computeFeatureSet() {
		ArrayList<ArrayList<StateVertex>> dpaths = decider.getPathsLeadingToDesirable(); //find paths in tree that end in user's goal
		computeCRD(dpaths);
		computeDistanceMetrics(dpaths);
		computePercentOfLandmarksInState();
	}
	
	private void computeCRD( ArrayList<ArrayList<StateVertex>> dpaths){
		double [] at = decider.computeProbabilityFeatures(dpaths); //certainty/risk/desirability
		System.arraycopy(at, 0, crd, 0, at.length);
		DecimalFormat df = new DecimalFormat("#.00"); 
		for (int i=0; i<crd.length; i++) {
			crd[i] = Double.valueOf(df.format(crd[i]));
		}
	}

	private void computeDistanceMetrics(ArrayList<ArrayList<StateVertex>> dpaths) {
		int d[] = decider.computeDistanceFeatures(dpaths);
		distanceToCritical = d[0];
		distanceToDesirable = d[1];
	}
		
	private void computePercentOfLandmarksInState() {
		decider.generateVerifiedLandmarks(arpg, con, lmoutputpath);
		stateContainsAttackLm = decider.computePrecentActiveAttackLm();
	}
		
	public double[] getCRD() {
		return crd;
	}

	public void setCRD(double[] metrics) {
		this.crd = metrics;
	}
	
	public String toString(){
		String s = "";
		for (double d : crd) {
			s += d+",";
		}
		return s.substring(0, s.length()-1);
	}

	public int getDistanceToCritical() {
		return distanceToCritical;
	}

	public void setDistanceToCritical(int distanceToCritical) {
		this.distanceToCritical = distanceToCritical;
	}

	public int getDistanceToDesirable() {
		return distanceToDesirable;
	}

	public void setDistanceToDesirable(int distanceToDesirable) {
		this.distanceToDesirable = distanceToDesirable;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public double getStateContainsLandmark() {
		return stateContainsAttackLm;
	}

	public void setStateContainsLandmark(double stateContainsLandmark) {
		this.stateContainsAttackLm = stateContainsLandmark;
	}

	public double getStateAddsLandmark() {
		return statesAddingLM;
	}

	public void setStateAddsLandmark(double stateAddsLandmark) {
		this.statesAddingLM = stateAddsLandmark;
	}

	public String getLmoutputpath() {
		return lmoutputpath;
	}

	public void setLmoutputpath(String lmoutputpath) {
		this.lmoutputpath = lmoutputpath;
	}

	public RelaxedPlanningGraph getArpg() {
		return arpg;
	}

	public void setArpg(RelaxedPlanningGraph arpg) {
		this.arpg = arpg;
	}

	public ConnectivityGraph getCon() {
		return con;
	}

	public void setCon(ConnectivityGraph con) {
		this.con = con;
	}
}
