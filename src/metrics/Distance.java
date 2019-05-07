package metrics;

import java.util.ArrayList;

public class Distance {
	public ArrayList<String> ref; //observation prefix + optimal suffix plan
	public ArrayList<String> incoming; //plan coming from sampled set of alternatives
	
	public Distance(ArrayList<String> a, ArrayList<String> b) {
		ref = a;
		incoming = b;
	}
	
	public boolean stateContainsGoal(ArrayList<String> state, ArrayList<String> goal) {
		for (String g : goal) {
			boolean found = false;
			for (String s : state) {
				if(s.equalsIgnoreCase(g)) {
					found = true;
				}
			}
			if(!found) {
				return false;
			}
		}
		return true;
	}
}
