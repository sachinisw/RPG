package metrics;

import java.util.Arrays;
import java.util.TreeSet;

public class State {
	private TreeSet<String> predicates;
	
	public State(TreeSet<String> p) {
		setPredicates(p);
	}

	public boolean equals(Object st) {
		if (st == null) {
			return false;
		}
		if (!State.class.isAssignableFrom(st.getClass())) {
			return false;
		}
		final State other = (State) st;
		return other.getPredicates().equals(predicates);
	}
	
	public String toString() {
		return Arrays.toString(predicates.toArray());
	}
	public TreeSet<String> getPredicates() {
		return predicates;
	}

	public void setPredicates(TreeSet<String> predicates) {
		this.predicates = predicates;
	}
}