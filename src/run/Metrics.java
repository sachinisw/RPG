package run;

import java.text.DecimalFormat;

import actors.Attacker;
import actors.User;


public class Metrics {
	private Attacker attacker;
	private User user;
	private double [] metrics;
	
	public Metrics(Attacker at, User us){
		this.attacker = at;
		this.user = us;
		metrics = new double[3];
	}
	
	public void computeMetrics(){
		double [] at = attacker.computeMetric();
		double [] us = user.computeMetric();
		System.arraycopy(at, 0, metrics, 0, at.length);
		System.arraycopy(us, 0, metrics, metrics.length-1, us.length);
		DecimalFormat df = new DecimalFormat("#.000"); 
		for (int i=0; i<metrics.length; i++) {
			metrics[i] = Double.valueOf(df.format(metrics[i]));
		}
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
	
}
