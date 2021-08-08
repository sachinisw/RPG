package run.stat;

public class DTreeInputCount {
	public int countY;
	public int countTotal;
	
	public DTreeInputCount(int y, int t) {
		countY = y;
		countTotal = t;
	}
	
	public String toString() {
		return countY + "(" +countTotal+")";
	}
}
