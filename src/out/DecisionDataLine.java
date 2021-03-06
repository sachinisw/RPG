package out;

import run.Metrics;

public class DecisionDataLine {
	private String observation;
	private String classLabel;
	private Metrics metrics;
	private int fDistanceToCriticalState;
	private int fDistanceToDesirableState;
	private double fPercentActiveALM;
	public double funcValue;
	
	public DecisionDataLine(String o, Metrics m){
		observation = o.split(":")[1];
		classLabel = o.split(":")[0];
		metrics = m;
		funcValue = 0.0;
		fDistanceToCriticalState = m.getDistanceToCritical();
		fDistanceToDesirableState = m.getDistanceToDesirable();
		fPercentActiveALM = m.getStateContainsLandmark(); 
	}
	
	public void computeObjectiveFunctionValue(){
		funcValue = metrics.getCRD()[0] + metrics.getCRD()[1] + (1-metrics.getCRD()[2]);
	}
	
	public String toString(){ //for current observation return full string of weighted metrics for all weights
		return observation + "," + metrics.toString() + "," + funcValue + ","
			+ fDistanceToCriticalState +","+ fDistanceToDesirableState +","+ fPercentActiveALM +
			","+ classLabel + "\n";
	}
	
	public String getObservation() {
		return observation;
	}
	
	public void setObservation(String observation) {
		this.observation = observation;
	}
	
	public Metrics getMetrics() {
		return metrics;
	}
	public void setMetrics(Metrics metrics) {
		this.metrics = metrics;
	}

	public String getClassLabel() {
		return classLabel;
	}

	public void setClassLabel(String classLabel) {
		this.classLabel = classLabel;
	}

	public int getfDistanceToCriticalState() {
		return fDistanceToCriticalState;
	}

	public void setfDistanceToCriticalState(int f_distanceToCriticalState) {
		this.fDistanceToCriticalState = f_distanceToCriticalState;
	}

	public int getfDistanceToDesirableState() {
		return fDistanceToDesirableState;
	}

	public void setfDistanceToDesirableState(int fDistanceToDesirableState) {
		this.fDistanceToDesirableState = fDistanceToDesirableState;
	}

	public double isfContainsUndersirableLandmark() {
		return fPercentActiveALM;
	}

	public void setfContainsUndersirableLandmark(double fContainsUndersirableLandmark) {
		this.fPercentActiveALM = fContainsUndersirableLandmark;
	}
}
