package graph;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

public class StateVertex implements Comparable<StateVertex>{
	private ArrayList<String> states;
	private String name;
	private double stateProbability;

	public StateVertex(){
		states = new ArrayList<>();
		name = "";
		stateProbability = 0.0;
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
		for (String st : goalstate) {
			if(!state.contains(st)){
				return false;
			}
		}
		return true;
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

}
