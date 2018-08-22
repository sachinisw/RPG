package graph;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

public class StateVertex implements Comparable<StateVertex>{
	private ArrayList<String> states;
	private String name;
	private double stateProbability;
	private boolean aCriticalState;
	private boolean aDesirableState;

	public StateVertex(){
		states = new ArrayList<>();
		name = "";
		stateProbability = 0.0;
		this.aCriticalState = false;
		this.aDesirableState = false;
	}

	public boolean isEqual(StateVertex anotherVertex){
		if(states.size()== anotherVertex.getStates().size()){
			if(states.containsAll(anotherVertex.getStates()) && anotherVertex.getStates().containsAll(states)){
				return true;
			}
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
		if(this.isEqual(anotherVertex)){
			return 0;
		}else{
			return 1;
		}
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

	/*return true if node's state contains the goal state
	 * */
	public boolean containsGoalState(ArrayList<String> goalstate){
		ArrayList<String> state = getStates();
		if(goalstate.isEmpty()){
			return false;
		}
		for (String st : goalstate) {
			if(!state.contains(st)){
				return false;
			}
		}
		return true;
	}
	
	/*return true if node's state contains the critical state
	 * */
	public boolean containsCriticalState(ArrayList<String> criticalstate){
		ArrayList<String> state = getStates();
		if(criticalstate.isEmpty()){
			return false;
		}
		for (String st : criticalstate) {
			if(!state.contains(st)){
				return false;
			}
		}
		return true;
	}
	
	/*return true if node's state contains the desirable state
	 * */
	public boolean containsDesirableState(ArrayList<String> desirablestate){
		ArrayList<String> state = getStates();
		if(desirablestate.isEmpty()){
			return false;
		}
		for (String st : desirablestate) {
			if(!state.contains(st)){
				return false;
			}
		}
		return true;
	}
//	NOT USING XXXX, XX anymore for the blocks.
//	public boolean containsCriticalState(ArrayList<String> criticalstate){
//		ArrayList<String> state = getStates();
//		int [] check = new int[criticalstate.size()];
//		if(criticalstate.isEmpty()){
//			return false;
//		}
//		for (int a=0; a<criticalstate.size(); a++) {
//			for(int i=0; i<state.size(); i++){
//				if(!criticalstate.get(a).contains("XX") && criticalstate.get(a).equalsIgnoreCase(state.get(i))){
//					check[a]=1;
//				}else if(criticalstate.get(a).contains("XX")){
//					String ctparts [] = criticalstate.get(a).substring(1,criticalstate.get(a).length()-1).split(" ");
//					String thisstate [] = state.get(i).substring(1,state.get(i).length()-1).split(" ");
//					if(ctparts.length==thisstate.length && ctparts[0].equalsIgnoreCase(thisstate[0])){
//						int lim = 1;
//						for(int x=1; x<ctparts.length; x++){
//							if(thisstate[x].equals("XXXX") && ctparts[x].equals("XX")){
//								lim++;
//							}
//							else if(ctparts[x].equals(thisstate[x])){
//								lim++;
//							}
//						}
//						if(lim==ctparts.length){
//							check[a]=1;
//						}
//					}
//				}
//			}
//		}
//		int sum = 0;
//		for(int i=0; i<check.length; i++){
//			sum += check[i];
//		}
//		return (sum==check.length);
//	}
		
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
}
