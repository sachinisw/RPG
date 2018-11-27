package graph;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.TreeSet;

public class StateVertex implements Comparable<StateVertex>{
	private ArrayList<String> states;
	private String name;
	private double stateProbability;
	private boolean aCriticalState;
	private boolean aDesirableState;
	private boolean aPartialCriticalState; //for blocks
	private boolean aPartialDesirableState; //for blocks

	public StateVertex(){
		states = new ArrayList<>();
		name = "";
		stateProbability = 0.0;
		this.aCriticalState = false;
		this.aDesirableState = false;
		aPartialCriticalState = false;
		aPartialDesirableState = false;
	}

	public boolean isEqual(StateVertex anotherVertex){
		if(states.containsAll(anotherVertex.getStates()) && anotherVertex.getStates().containsAll(states)){
			return true;
		}
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!StateVertex.class.isAssignableFrom(obj.getClass())) {
			return false;
		}
		final StateVertex other = (StateVertex) obj;
		return this.isEqual(other);
	}

	@Override
	public int compareTo(StateVertex anotherVertex) {
		return this.hashCode() - anotherVertex.hashCode();
	}

	public int hashCode(){
		return Objects.hash(this.getStates());
	}

	public String getName() {
		return name;
	}

	public void setName() {	//name=sorted states in string.
		String nm = "";
		TreeSet<String>set = new TreeSet<>();
		for(int i=0; i<states.size(); i++){
			set.add(states.get(i));
		}
		for (String el : set) {
			nm+=el+",";
		}
		this.name = nm.substring(0, nm.length()-1);
	}

	public String convertToDOTString(){
		String s = "\"";
		String prob = new DecimalFormat(".###").format(getStateProbability())+"\\n";
		for(int i=0; i<states.size(); i++){
			s+=states.get(i).substring(1,states.get(i).length()-1)+"\\n";
		}
		return  s + prob + "\"";
	}

	/*return true if node's state contains the given state, can be used to check if state contains both critical and desirable goal states
	 * */
	public boolean containsState(ArrayList<String> astate){
		ArrayList<String> state = getStates();
		if(astate.isEmpty()){
			return false;
		}
		for (String st : astate) {
			if(!state.contains(st)){
				return false;
			}
		}
		return true;
	}

	//used in blocks domain, check if astate[] partially contains required[] 
	//e.g. attacker goal (SOY) user wants (JOY) with blocks JOYS. partial state matching will make SJOY, JSOY, SOJY  all valid
	public boolean containsPartialStateBlockWords(ArrayList<String> requiredword){
		return matchPartialBlocks(this.getStates(), requiredword);
	}

	//go to a node check what word it spells. node must contain 1 vertical stack.
	//remove hidden block and see if the word can still be spelled. if yes, then partial state match.
	private static LinkedList<String> getSpelledWord(ArrayList<String> state) {
		LinkedList<String> order = new LinkedList<>();
		int ontablecount = 0;
		for (String st : state) {		//check if 1 column vertical stack. count on table blocks must be 1.
			if(st.contains("ONTABLE")) {
				ontablecount++;
			}
		}
		if(ontablecount==2) {
			return null;
		}else {
			for (String string : state) {
				String parts[] = string.substring(1,string.length()-1).split(" ");
				if(parts[0].equalsIgnoreCase("ON")) {
					if(!order.contains(parts[1]) && !order.contains(parts[2])) {
						order.add(parts[1]);
						order.add(parts[2]);
					}else if(order.contains(parts[1]) && !order.contains(parts[2])) {
						int i = order.indexOf(parts[1]);
						order.add(i+1, parts[2]);
					}else if(!order.contains(parts[1]) && order.contains(parts[2])) {
						int i = order.indexOf(parts[2]);
						if(i>0) {
							order.add(i-1, parts[1]);
						}else {
							order.add(0, parts[1]);
						}
					}else if(order.contains(parts[1])&& order.contains(parts[2])) {
						int i = order.indexOf(parts[1]);
						order.add(i+1,parts[2]);
						int old = 0;
						for (int j=0; j<order.size(); j++) {
							if(order.get(j).equalsIgnoreCase(parts[2])) {
								old = j;
								break;
							}
						}
						String next = order.get(old+1);
						order.remove(old);
						order.add(i+1, next);
						order.remove(next);
					}
				}
			}
			return order;
		}
	}

	private boolean matchPartialBlocks(ArrayList<String> state, ArrayList<String> requiredword) {
		LinkedList<String> curorder = getSpelledWord(state);
		LinkedList<String> attackers = getSpelledWord(requiredword);
		if(curorder != null && attackers != null) {
			Iterator<String> iter = curorder.iterator();
			while(iter.hasNext()){
				String l = iter.next();
				if(!attackers.contains(l)) {
					iter.remove();
				}
			}
			if(curorder.size()>attackers.size()) {
				for (int i=0; i<curorder.size(); i++) {
					if(!curorder.get(i).equalsIgnoreCase(attackers.get(i))) {
						return false;
					}
				}
				return true;
			}else if(curorder.size()==attackers.size()) {
				for(int i=0; i<curorder.size(); i++) {
					if(!curorder.get(i).equalsIgnoreCase(attackers.get(i))) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	public void addStates(ArrayList<String> st){
		states.addAll(st);
	}

	public ArrayList<String> getStates(){
		return this.states;
	}

	public String toString(){
		return Arrays.toString(states.toArray());
	}

	public double getStateProbability() {
		return stateProbability;
	}

	public void setStateProbability(double stateProbability) {
		this.stateProbability = stateProbability;
	}

	public boolean isContainsCriticalState() {
		return aCriticalState;
	}

	public void setContainsCriticalState(boolean containsCriticalState) {
		this.aCriticalState = containsCriticalState;
	}

	public boolean isContainsDesirableState() {
		return aDesirableState;
	}

	public void setContainsDesirableState(boolean containsDesirableState) {
		this.aDesirableState = containsDesirableState;
	}

	public boolean isaPartialCriticalState() {
		return aPartialCriticalState;
	}

	public void setaPartialCriticalState(boolean aPartialCriticalState) {
		this.aPartialCriticalState = aPartialCriticalState;
	}

	public boolean isaPartialDesirableState() {
		return aPartialDesirableState;
	}

	public void setaPartialDesirableState(boolean aPartialDesirableState) {
		this.aPartialDesirableState = aPartialDesirableState;
	}
}
