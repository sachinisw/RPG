package graph;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

import con.ConnectivityGraph;
import plan.Plan;
import run.InitialState;

public class ActionEdge {
	private String action; //edge name
	private StateVertex from;
	private StateVertex to;
	private boolean reverse;
	private double actionProbability;
	
	public ActionEdge(String name, StateVertex f, StateVertex t, boolean rev){
		action = name;
		from = f;
		to = t;
		reverse = rev;
		actionProbability = 0.0;
	}

	public String toString(){
		return from+" --"+ action +"--> "+to +"["+ reverse+"]";
	}
	
	public String convertToDOTString(){
		String f = "", t = "";
		String probF = new DecimalFormat(".###").format(from.getStateProbability())+"\\n";
		String probT = new DecimalFormat(".###").format(to.getStateProbability())+"\\n";
		String probE = new DecimalFormat(".###").format(getActionProbability())+"\\n";
		for(int i=0; i<from.getStates().size(); i++){
			f+=from.getStates().get(i).substring(1,from.getStates().get(i).length()-1)+"\\n";
		}
		for(int i=0; i<to.getStates().size(); i++){
			t+=to.getStates().get(i).substring(1,to.getStates().get(i).length()-1)+"\\n";
		}
		return "\"" +f+ probF +"\"" + " -> " + "\""+t+ probT +"\"" +"[label=\""+action+"\\n"+ probE +"\"];";
	}
	
	public boolean isReverseOf(ActionEdge anotherEdge){
		if(from.isEqual(anotherEdge.to) && to.isEqual(anotherEdge.from)){
			return true;
		}
		return false;
	}
	
	public boolean isEqual(ActionEdge anotherEdge){
		if(from.isEqual(anotherEdge.from) && to.isEqual(anotherEdge.to) && action.equalsIgnoreCase(anotherEdge.action)){
			return true;
		}
		return false;
	}
	
	public int hashCode(){
		return Objects.hash(this.getFrom(),this.getTo());
	}
	
	public boolean isEdgeInPlan(Plan p, ConnectivityGraph c, InitialState init){
		ArrayList<ArrayList<String>> seq = p.getStateSequence(init.getState(), c);
//		for(int i=0; i<seq.size(); i++){
//			System.out.println(seq.get(i));
//		}
		for(int i=0; i<p.getPlanSteps().size(); i++){
			String ac = p.getPlanSteps().get(i).substring(p.getPlanSteps().get(i).indexOf(":")+1,p.getPlanSteps().get(i).length()).trim();
			StateVertex tempFrom = new StateVertex();
			StateVertex tempTo = new StateVertex();
			tempFrom.addStates(seq.get(i));
			tempTo.addStates(seq.get(i+1));
			ActionEdge tempEdge = new ActionEdge(ac, tempFrom, tempTo, false);
			if(isEqual(tempEdge)){
				System.out.println("match.................."+ac);
				return true;
			}
		}
		return false;
	}
	
	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public StateVertex getFrom() {
		return from;
	}

	public void setFrom(StateVertex from) {
		this.from = from;
	}

	public StateVertex getTo() {
		return to;
	}

	public void setTo(StateVertex to) {
		this.to = to;
	}

	public boolean isReverse() {
		return reverse;
	}

	public void setReverse(boolean reverse) {
		this.reverse = reverse;
	}

	public double getActionProbability() {
		return actionProbability;
	}

	public void setActionProbability(double actionProbability) {
		this.actionProbability = actionProbability;
	}
}
