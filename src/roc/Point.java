package roc;

public class Point {
	private double TP;
	private double FP;
	
	public Point(){
		TP = 0.0;
		FP = 0.0;
	}
	
	public Point(double a, double b){
		TP = a;
		FP = b;
	}
	
	public double getTP() {
		return TP;
	}
	public void setTP(int tP) {
		TP = tP;
	}
	public double getFP() {
		return FP;
	}
	public void setFP(int fP) {
		FP = fP;
	}
	
	public String toString(){
		return "<" + TP + "," + FP +">";
	}
}
