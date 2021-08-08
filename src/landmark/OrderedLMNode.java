package landmark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class OrderedLMNode implements Comparable<OrderedLMNode>{	
	private ArrayList<String> nodecontent;
	private int treeLevel;
	
	public OrderedLMNode(String s) {
		nodecontent = new ArrayList<String>();
		nodecontent.add(s);
		treeLevel = -1;
	}

	public boolean equals(Object o){
		if (o == this) { // If the object is compared with itself then return true
			return true;
		}
		if (!(o instanceof OrderedLMNode)) { //Check if o is an instance of Complex or not "null instanceof [type]" also returns false */
			return false;
		}
		OrderedLMNode c = (OrderedLMNode) o; // typecast o to LGGNode so that we can compare data members 
		// Compare the data members and return accordingly 
		if(nodecontent.size()== c.nodecontent.size()){
			if(nodecontent.containsAll(c.nodecontent) && c.nodecontent.containsAll(nodecontent)){
				return true;
			}
		}
		return false;
	}
	
	public int hashCode(){
		return Objects.hash(this.nodecontent);
	}
	
	@Override
	public int compareTo(OrderedLMNode o) {
		return this.hashCode() - o.hashCode();
	}
	
	public ArrayList<String> getNodecontent() {
		return nodecontent;
	}

	public void setNodecontent(ArrayList<String> nodecontent) {
		this.nodecontent = nodecontent;
	}
	
	public String toString() {
		return Arrays.toString(nodecontent.toArray()) + "("+ treeLevel +")" ;
	}

	public int getTreeLevel() {
		return treeLevel;
	}

	public void setTreeLevel(int treeLevel) {
		this.treeLevel = treeLevel;
	}
}
