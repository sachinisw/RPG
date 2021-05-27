package run;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class State {
	public ArrayList<String> statePredicates;
	
	public State(){
		statePredicates = new ArrayList<String>();
	}
	
	public ArrayList<String> getState() {
		return statePredicates;
	}

	public void setState(ArrayList<String> st) {
		this.statePredicates = st;
	}
	
	public String toString(){
		return Arrays.toString(statePredicates.toArray());
	}
	
	public boolean equals(State s){
		Collections.sort(statePredicates);
		Collections.sort(s.statePredicates);
		return statePredicates.equals(s.statePredicates);
	}
}
