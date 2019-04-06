package hspfplan;

import java.util.ArrayList;
import java.util.Arrays;

public class HSPFPlan {
	private ArrayList<String> actions;
	
	public HSPFPlan() {
		actions = new ArrayList<String>();
	}
	
	public int getPlanCost() {
		return actions.size(); //assumes unit cost
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
	
}
