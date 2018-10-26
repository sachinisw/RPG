package out;

import run.Metrics;

public class DataLine {
	private String observation;
	private String classLabel;
	private Metrics metrics;
	private int fDistanceToCriticalState;
	private int fDistanceToDesirableState;
	private int fRemainingUndesirableLandmarks;
	private double fPercentLMIn;
	public double funcValue;
	
	public DataLine(String o, Metrics m){
		observation = o.split(":")[1];
		classLabel = o.split(":")[0];
		metrics = m;
		funcValue = 0.0;
		fDistanceToCriticalState = m.getDistanceToCritical();
		fDistanceToDesirableState = m.getDistanceToDesirable();
		fRemainingUndesirableLandmarks = m.getRemainingLandmarks();
		fPercentLMIn = m.getStateContainsLandmark();
	}
	
	public void computeObjectiveFunctionValue(){
		funcValue = metrics.getMetrics()[0] + metrics.getMetrics()[1] + (1-metrics.getMetrics()[2]);
	}
	
	public String toString(){ //for current observation return full string of weighted metrics for all weights
		return observation + "," + metrics.toString() + "," + funcValue + ","
			+ fDistanceToCriticalState +","+ fDistanceToDesirableState +","+ fRemainingUndesirableLandmarks + "," + fPercentLMIn +
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

	public int getfRemainingUndesirableLandmarks() {
		return fRemainingUndesirableLandmarks;
	}

	public void setfRemainingUndesirableLandmarks(int fRemainingUndesirableLandmarks) {
		this.fRemainingUndesirableLandmarks = fRemainingUndesirableLandmarks;
	}

	public double isfContainsUndersirableLandmark() {
		return fPercentLMIn;
	}

	public void setfContainsUndersirableLandmark(double fContainsUndersirableLandmark) {
		this.fPercentLMIn = fContainsUndersirableLandmark;
	}
}
