package plans;

import java.util.ArrayList;
import java.util.Arrays;

public class Plan {
	private ArrayList<String> actions;
	
	public Plan() {
		actions = new ArrayList<>();
	}

	public ArrayList<String> getActions() {
		return actions;
	}

	public void setActions(ArrayList<String> actions) {
		this.actions = actions;
	}

	public String toString() {
		return Arrays.toString(getActions().toArray());
	}
}
