package run;

import java.text.DecimalFormat;

import actors.Attacker;
import actors.User;
import con.ConnectivityGraph;
import landmark.RelaxedPlanningGraph;


public class Metrics {
	private Attacker attacker;
	private User user;
	private double [] metrics;
	private int distanceToCritical;
	private int distanceToDesirable;
	private int lmRemaining;
	private double stateContainsLandmark;
	private double statesAddingLM;
	private String domain;
	
	public Metrics(Attacker at, User us, String dom){
		this.attacker = at;
		this.user = us;
		metrics = new double[3];
		distanceToCritical = 0;
		distanceToDesirable = 0;
		lmRemaining = 0;
		domain = dom;
		stateContainsLandmark = 0.0;
		statesAddingLM = 0.0;
	}
	
	public void computeMetrics(){
		double [] at = attacker.computeMetric();
		double [] us = user.computeMetric();
		System.arraycopy(at, 0, metrics, 0, at.length);
		System.arraycopy(us, 0, metrics, metrics.length-1, us.length);
		DecimalFormat df = new DecimalFormat("#.00"); 
		for (int i=0; i<metrics.length; i++) {
			metrics[i] = Double.valueOf(df.format(metrics[i]));
		}
	}

	public void computeDistanceToCrtical(){
		distanceToCritical = attacker.computeDistanceToCriticalStateFromRoot(domain);
	}
	
	public void computeDistanceToDesirable(){
		distanceToDesirable= user.computeDistanceToDesirableStateFromRoot();
	}
	
	public void generateAttackerLandmarks(RelaxedPlanningGraph arpg, ConnectivityGraph con, String lmoutput) {
		attacker.setVerifiedLandmarks(arpg, con, lmoutput);
	}
	
	public void computeAttackLandmarksRemaining(){
		lmRemaining = attacker.countRemainingLandmarks();
	}
	
	public void percentOfLandmarksStateContain() {
		stateContainsLandmark = attacker.percentOfLandmarksStateContain();
	}
	
	public void currentAddsLandmark() {
		statesAddingLM = attacker.stateAddsFactLandmark();
	}
	
	public double[] getMetrics() {
		return metrics;
	}

	public void setMetrics(double[] metrics) {
		this.metrics = metrics;
	}
	
	public String toString(){
		String s = "";
		for (double d : metrics) {
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
