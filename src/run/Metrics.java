package run;

import java.text.DecimalFormat;

import actors.Decider;
import con.ConnectivityGraph;
import landmark.RelaxedPlanningGraph;


public class Metrics {
	private Decider decider;
	private double [] crd;
	private int distanceToCritical;
	private int distanceToDesirable;
	private int lmRemaining;
	private double stateContainsLandmark;
	private double statesAddingLM;
	private String domain;
	
	public Metrics(Decider d, String dom){
		this.decider = d;
		domain = dom;
		crd = new double[3];
		distanceToCritical = 0;
		distanceToDesirable = 0;
		lmRemaining = 0;
		stateContainsLandmark = 0.0; 
		statesAddingLM = 0.0;
	}
	
	public void computeCRD(){
		double [] at = decider.computeMetrics(); //certainty/timeliness/desirability
		System.arraycopy(at, 0, crd, 0, at.length);
		DecimalFormat df = new DecimalFormat("#.00"); 
		for (int i=0; i<crd.length; i++) {
			crd[i] = Double.valueOf(df.format(crd[i]));
		}
	}

	public void computeDistanceMetrics() {
		int d[] = decider.computeDistanceMetrics(domain);
		distanceToCritical = d[0];
		distanceToDesirable = d[1];
	}
	
	public void generateAttackerLandmarks(RelaxedPlanningGraph arpg, ConnectivityGraph con, String lmoutput) {
		decider.setVerifiedLandmarks(arpg, con, lmoutput);
	}
	
	public void computeAttackLandmarksRemaining(){
		lmRemaining = decider.countRemainingLandmarks();
	}
	
	public void percentOfLandmarksInState() {
		stateContainsLandmark = decider.percentOfLandmarksStateContain();
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

	public int getRemainingLandmarks() {
		return lmRemaining;
	}

	public void setLandmarkMetric(int landmarkMetric) {
		this.lmRemaining = landmarkMetric;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public double getStateContainsLandmark() {
		return stateContainsLandmark;
	}

	public void setStateContainsLandmark(double stateContainsLandmark) {
		this.stateContainsLandmark = stateContainsLandmark;
	}

	public double getStateAddsLandmark() {
		return statesAddingLM;
	}

	public void setStateAddsLandmark(double stateAddsLandmark) {
		this.statesAddingLM = stateAddsLandmark;
	}
	
}
