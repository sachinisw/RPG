package graph;

public class ActionEdge {
	private String action; //edge name
	private StateVertex from;
	private StateVertex to;
	
	public ActionEdge(String name, StateVertex f, StateVertex t){
		action = name;
		from = f;
		to = t;
	}

	public String toString(){
		return from+" --"+ action +"--> "+to;
	}
	
	public String convertToDOTString(){
		String f = "", t = "";
		for(int i=0; i<from.getStates().size(); i++){
			f+=from.getStates().get(i).substring(1,from.getStates().get(i).length()-1)+"\\n";
		}
		for(int i=0; i<to.getStates().size(); i++){
			t+=to.getStates().get(i).substring(1,to.getStates().get(i).length()-1)+"\\n";
		}
		return "\"" +f+ "\"" + " -> " + "\""+t+"\"" +"[label=\""+action+"\"];";
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
}
