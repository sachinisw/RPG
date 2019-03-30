package fdplan;

import java.util.ArrayList;
import java.util.Arrays;

public class FDPlan {
	private ArrayList<String> actions;
	private static final int UNIT_COST = 1;
	
	public FDPlan() {
		actions = new ArrayList<String>();
	}
	
	public int getPlanCost() {
		return actions.size()*UNIT_COST; //assumes unit cost
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
