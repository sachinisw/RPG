package causality;

import java.util.Objects;

public class CGEdge {

	private CGNode from;
	private CGNode to;
	private String edgeLabel;

	public CGEdge(CGNode f, CGNode t, String l){
		from = f;
		to = t;
		edgeLabel = l;
	}

	public boolean equals(Object o){
		if (o == this) { // If the object is compared with itself then return true
			return true;
		}
		if (!(o instanceof CGEdge)) { //Check if o is an instance of Complex or not "null instanceof [type]" also returns false */
			return false;
		}
		CGEdge c = (CGEdge) o; // typecast o to Complex so that we can compare data members 
		return (from.isEqual(c.from) && to.isEqual(c.to) && c.getEdgeLabel().equalsIgnoreCase(edgeLabel) );       // Compare the data members and return accordingly 
	}

	public int hashCode(){
		return Objects.hash(this.getFrom(),this.getTo());
	}

	public CGNode getFrom() {
		return from;
	}
	public void setFrom(CGNode from) {
		this.from = from;
	}
	public CGNode getTo() {
		return to;
	}
	public void setTo(CGNode to) {
		this.to = to;
	}

	public String toString() {
		return this.from + "-" + edgeLabel +" -> " + this.to;
	}

	public String getEdgeLabel() {
		return edgeLabel;
	}

	public void setEdgeLabel(String edgeLabel) {
		this.edgeLabel = edgeLabel;
	}
}
