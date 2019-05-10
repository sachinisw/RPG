package metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

//Sohrabi 2016
public class Distance {
	public ArrayList<String> ref; //observation prefix + optimal suffix plan ||| or state sequences
	public ArrayList<String> incoming; //plan coming from sampled set of alternatives
	public HashMap<String, String> tokenmap;

	public Distance(ArrayList<String> a, ArrayList<String> b) {
		ref = a;
		incoming = b;
		tokenmap = new HashMap<String, String>();
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

	public void tokenize(){
		for (String string : incoming) {
			if(!tokenmap.containsKey(string)) {
				tokenmap.put(string,"");
			}
		}
		for (String string : ref) {
			if(!tokenmap.containsKey(string)) {
				tokenmap.put(string,"");
			}
		}
		char val = 'A';
		Iterator<Entry<String, String>> itr = tokenmap.entrySet().iterator();
		while(itr.hasNext()) {
			tokenmap.put(itr.next().getKey(), String.valueOf(val++));
		}
	}
}
