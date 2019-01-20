package graph;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
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

	//used in blocks domain, check if astate[] partially contains required[] (desirable or critical)
	//e.g. attacker goal (SOY) user wants (JOY) with blocks JOYS. partial state matching will make SJOY, JSOY, JOSY, JOYS all candidates. 
	//Only JSOY will need intervention, attacker succeeds before user finishes spelling JOY
	public boolean containsPartialStateBlockWords(ArrayList<String> requiredword, boolean isCritical){
		return matchPartialBlocks(this.getStates(), requiredword, isCritical);
	}

	//go to a node check what word it spells. node must contain 1 vertical stack.
	private static LinkedList<String> getSpelledWord(ArrayList<String> state){
		LinkedList<String> order = new LinkedList<>();
		LinkedList<String> ons = new LinkedList<String>();
		for (String string : state) {
			String parts[] = string.substring(1,string.length()-1).split(" ");
			if(parts[0].equalsIgnoreCase("ON")) {
				ons.add(string.substring(1,string.length()-1));
			}
		}
		if(ons.size()>0) {
			order.add(ons.getFirst().split(" ")[1]);
			order.add(ons.getFirst().split(" ")[2]);
			ons.removeFirst();
			while(!ons.isEmpty()) { //if next element is adjacent. then add to order
				String s = ons.removeFirst();
				String parts[] = s.split(" ");
				if(!order.contains(parts[1]) && !order.contains(parts[2]) && ons.size()==0) {
					order.add("-");
					order.add(parts[1]);
					order.add(parts[2]);
				}else if(!order.contains(parts[1]) && !order.contains(parts[2]) && ons.size()>0) {
					ons.addLast(s);
				}else if(order.contains(parts[1]) && !order.contains(parts[2])) { //insert after parts[1] in order
					int i = order.indexOf(parts[1]);
					order.add(i+1, parts[2]);
				}else if(!order.contains(parts[1]) && order.contains(parts[2])) { //insert after 
					int i = order.indexOf(parts[2]);
					if(i>0) {
						order.add(i-1, parts[1]);
					}else {
						order.add(0, parts[1]);
					}
				}
			}
		}
		return order;
	}

	//check if cur spells req partially. may need to remove blocks to see if the req word can still be spelled.
	//req. could be desirable or critical words
	private boolean matchPartialBlocks(ArrayList<String> curstate, ArrayList<String> requiredword, boolean isCritical) {
		LinkedList<String> cur = getSpelledWord(curstate);//words are spelled correctly. no need to debug here
		LinkedList<String> req = getSpelledWord(requiredword);//words are spelled correctly. no need to debug here
		String curword = "", reqword = "", hidden="";
		if(!cur.isEmpty()) {
			for (String s : req) {
				reqword+=s;
			}
			for (String s : cur) {
				curword+=s;
			}
			if(curword.equalsIgnoreCase(reqword)) {
				return true;
			}
			if(!isCritical) { //if tested whether cur spells desirable state, then you can find hidden block this way.
				StringBuilder sb = new StringBuilder(curword); 	//find invisible letter. remove it. see if remainder spells required word
				for(int i=0; i<curword.length(); i++) {
					String letter = String.valueOf(curword.charAt(i));
					if(!reqword.contains(letter)) {
						hidden=letter;
					}
				}
				if(hidden.length()>0) {
					sb.deleteCharAt(curword.indexOf(hidden));
				}
				return sb.toString().equalsIgnoreCase(reqword);
			}else { //if tested whether cur spells critical state, hidden block is in the critical state. 
				return curword.contains(reqword);
			}
		}	
		return false;
	}

	public boolean isWordConsecutive(ArrayList<String> requiredword) {
		LinkedList<String> cur = getSpelledWord(this.getStates());
		LinkedList<String> req = getSpelledWord(requiredword);
		String curword = "", reqword = "";
		if(!cur.isEmpty()) {
			for (String s : req) {
				reqword+=s;
			}
			for (String s : cur) {
				curword+=s;
			}
			return curword.contains(reqword);
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
