package graph;

import java.text.DecimalFormat;

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
		return from+" --"+ action +"--> "+to;
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
