package fdplan;

import java.util.ArrayList;
import java.util.Arrays;

public class FDPlan {
	private ArrayList<String> actions;
	private int length;
	
	public FDPlan() {
		actions = new ArrayList<String>();
		length = 0;
	}
	
	public String toString() {
		return Arrays.toString(actions.toArray());
	}
	
	public ArrayList<String> getActions() {
		return actions;
	}
	public void setActions(ArrayList<String> actions) {
		this.actions = actions;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
}
