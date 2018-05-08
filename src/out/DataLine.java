package out;

import java.text.DecimalFormat;

import run.Metrics;

public class DataLine {
	private String observation;
	private Metrics metrics;
	private WeightGroup group;
	private double[] weightedMetrics;
	private double funcValue;

	public DataLine(String o, Metrics m, WeightGroup gp){ //one data line for 1 weight group.
		observation = o;
		metrics = m;
		group = gp;
		weightedMetrics = new double [3];
		funcValue = 0.0;
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

	public void computeWeightedMetrics(){
		DecimalFormat df = new DecimalFormat("#.0000"); 
		double c = Double.valueOf(df.format(metrics.getMetrics()[0]*group.getcW())); //w*c
		double r = Double.valueOf(df.format(metrics.getMetrics()[1]*group.getrW())); //w*r
		double d = Double.valueOf(df.format((1-metrics.getMetrics()[2])*group.getdW()));//w*(1-d)
		weightedMetrics[0]=c;
		weightedMetrics[1]=r;
		weightedMetrics[2]=d;
	}
	
	public void computeObjectiveFunctionValue(){
		funcValue = weightedMetrics[0] + weightedMetrics[1] + weightedMetrics[2];
	}
	
	public String toString(){ //for current observation return full string of weighted metrics for all weights
		return observation + "," + metrics.toString() + "," + group.getcW() + "," + group.getrW() + "," + group.getdW() + "," 
				+ weightedMetrics[0] + "," + weightedMetrics[1] + "," + weightedMetrics[2] + "," + funcValue + "\n";
	}

	public double getFuncValue() {
		return funcValue;
	}

	public void setFuncValue(double funcValue) {
		this.funcValue = funcValue;
	}
	
	public WeightGroup getGroup() {
		return group;
	}

	public void setGroup(WeightGroup group) {
		this.group = group;
	}

	public double[] getWeightedMetrics() {
		return weightedMetrics;
	}

	public void setWeightedMetrics(double[] weightedMetrics) {
		this.weightedMetrics = weightedMetrics;
	}
}
