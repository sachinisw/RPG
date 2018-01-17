package graph;

public class VertexEccentricity {
	private int eccentricity;
	private StateVertex from;
	private StateVertex to;
	
	public VertexEccentricity(int d, StateVertex f, StateVertex t){
		this.eccentricity = d;
		this.from = f;
		this.to = t;
	}
	
	public int getDiameter() {
		return eccentricity;
	}
	public void setDiameter(int diameter) {
		this.eccentricity = diameter;
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
		if(this.to!=null)
			return "from="+this.from.getName()+" to="+this.to.getName()+" eccentricity="+this.eccentricity;
		else
			return "from="+this.from.getName()+" to="+null+" eccentricity="+this.eccentricity;

	}
}
