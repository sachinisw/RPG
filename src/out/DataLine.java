package out;

import run.Metrics;

public class DataLine {
	private String observation;
	private Metrics metrics;
	
	public DataLine(String o, Metrics m){
		observation = o;
		metrics = m;
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
	
	public String toString(){
		return observation + "," + metrics.toString();
	}
}
