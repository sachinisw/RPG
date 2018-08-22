package decisiontree;

import java.text.DecimalFormat;
import java.util.Arrays;

public class BinnedDataLine {
	private String observation;
	private String label;
	private int distToCritical;
	private int distToDesirable;
	private int remainingLandmarks;
	private double fo;
	private int [] fValBins;//break range of min, max for bins of 0.1. can change granularity later
	private double min, max;
	
	public BinnedDataLine(String ob, String lb, int cri, int des, int rem, double f, double mn, double mx){
		observation = ob;
		label = lb;
		fo = f;
		min = mn;
		max = mx;
		int size = 1;
		for(double i=min; i<=max; i+=0.10){ //doesn't have a bin for max val. need to add a buffer bucket for that.
			size++;
		}
		fValBins = new int[size];//1=true, 0=false
		distToCritical = cri;
		distToDesirable = des;
		remainingLandmarks = rem;
	}

	private int [] allocate(double val){
		double lo = 0.0, hi = 0.0, seed = min;
		int [] temp = new int [fValBins.length];
		double [] binVals = new double [fValBins.length];
		DecimalFormat df = new DecimalFormat("#.00"); 
		double cx = Double.parseDouble(df.format(val));	
		for (int i=0; i<binVals.length; i++) { 	//assign a value to bins based on min max of fV. The values here show the upper limit of each bin
			binVals[i] = Double.parseDouble(df.format(seed));
			seed+=0.10;
		}
		for (int x=0; x<binVals.length; x++) {
			if(x==0){
				lo = Double.parseDouble(df.format(0.00));
				hi = Double.parseDouble(df.format(binVals[x]));
			}else{
				lo = Double.parseDouble(df.format(binVals[x-1]));
				hi = Double.parseDouble(df.format(binVals[x]));
			}
			if(Double.compare(lo, cx)<=0  && Double.compare(cx, hi)<=0){
				temp[x]= 1;
				break;
			}
		}
		return temp;
	}
	
	public void assignToBins(double c){
		int [] arrC = allocate(c); 
		System.arraycopy(arrC, 0, fValBins, 0, arrC.length);
	}
	
	public String toString(){
		return observation + "," + fo +"," + Arrays.toString(fValBins).substring(1, Arrays.toString(fValBins).length()-1) + "," + distToCritical+ "," + distToDesirable +"," + remainingLandmarks +"," + label;
	}
	
	public int[] getCbins() {
		return fValBins;
	}
	
	public void setCbins(int[] cbins) {
		this.fValBins = cbins;
	}

	public String getObservation() {
		return observation;
	}

	public void setObservation(String observation) {
		this.observation = observation;
	}

	public double getFo() {
		return fo;
	}

	public void setFo(double fo) {
		this.fo = fo;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public int getDistToCritical() {
		return distToCritical;
	}

	public void setDistToCritical(int distToCritical) {
		this.distToCritical = distToCritical;
	}

	public int getDistToDesirable() {
		return distToDesirable;
	}

	public void setDistToDesirable(int distToDesirable) {
		this.distToDesirable = distToDesirable;
	}

	public int getRemainingLandmarks() {
		return remainingLandmarks;
	}

	public void setRemainingLandmarks(int remainingLandmarks) {
		this.remainingLandmarks = remainingLandmarks;
	}
}
