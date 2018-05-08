package out;

public class WeightGroup {
	private double cW;
	private double rW;
	private double dW;
	
	public WeightGroup(double c, double r, double d){
		this.cW = c;
		this.rW = r;
		this.dW = d;
	}

	public double getcW() {
		return cW;
	}

	public void setcW(double cW) {
		this.cW = cW;
	}

	public double getrW() {
		return rW;
	}

	public void setrW(double rW) {
		this.rW = rW;
	}

	public double getdW() {
		return dW;
	}

	public void setdW(double dW) {
		this.dW = dW;
	}
	
}
