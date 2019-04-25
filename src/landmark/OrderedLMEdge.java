package landmark;

import java.util.Objects;

public class OrderedLMEdge {
	private OrderedLMNode before;
	private OrderedLMNode after;
	
	public OrderedLMEdge(OrderedLMNode b, OrderedLMNode a) {
		this.before = b;
		this.after = a;
	}
	
	public void setfrom(OrderedLMNode n) {
		before = n;
	}
	public void setto(OrderedLMNode n) {
		after = n;
	}
	
	public OrderedLMNode getBefore() {
		return before;
	}
	
	public OrderedLMNode getAfter() {
		return after;
	}
	
	public boolean equals(Object o){
		if (o == this) { // If the object is compared with itself then return true
			return true;
		}
		if (!(o instanceof OrderedLMEdge)) { //Check if o is an instance of Complex or not "null instanceof [type]" also returns false */
			return false;
		}
		OrderedLMEdge c = (OrderedLMEdge) o; // typecast o to Complex so that we can compare data members 
		return (before.equals(c.before) && after.equals(c.after));       // Compare the data members and return accordingly 
	}
		
	public int hashCode(){
		return Objects.hash(this.getBefore(),this.getAfter());
	}
	
	public String toString() {
		return before.toString()+ " < " + after.toString();
	}
}
