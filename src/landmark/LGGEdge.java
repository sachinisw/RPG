package landmark;

import java.util.Objects;

public class LGGEdge {
	private LGGNode from;
	private LGGNode to;
	
	public LGGEdge(LGGNode f, LGGNode t){
		from = f;
		to = t;
	}
	
	public boolean equals(Object o){
		if (o == this) { // If the object is compared with itself then return true
			return true;
		}
		if (!(o instanceof LGGEdge)) { //Check if o is an instance of Complex or not "null instanceof [type]" also returns false */
			return false;
		}
		LGGEdge c = (LGGEdge) o; // typecast o to Complex so that we can compare data members 
		return (from.isEqual(c.from) && to.isEqual(c.to));       // Compare the data members and return accordingly 
	}
	
	public int hashCode(){
		return Objects.hash(this.getFrom(),this.getTo());
	}
	
	public LGGNode getFrom() {
		return from;
	}
	public void setFrom(LGGNode from) {
		this.from = from;
	}
	public LGGNode getTo() {
		return to;
	}
	public void setTo(LGGNode to) {
		this.to = to;
	}
}
