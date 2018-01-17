package graph;

public class GraphDiameter {
	private int diameter;
	private StateVertex from;
	private StateVertex to;
	
	public GraphDiameter(int d, StateVertex f, StateVertex t){
		this.diameter = d;
		this.from = f;
		this.to = t;
	}
	
	public int getDiameter() {
		return diameter;
	}
	public void setDiameter(int diameter) {
		this.diameter = diameter;
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
	
	public String toString(){
		return "from="+this.from.getName()+" to="+this.to.getName()+" diameter="+this.diameter;
	}
}
