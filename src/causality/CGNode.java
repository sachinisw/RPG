package causality;

import java.util.ArrayList;
import java.util.Objects;

public class CGNode implements Comparable<CGNode>, Cloneable{
	private ArrayList<String> data;

	public CGNode(ArrayList<String> v){
		this.data = v;
	}
	
	public boolean equals(Object o){
		if (o == this) { // If the object is compared with itself then return true
			return true;
		}
		if (!(o instanceof CGNode)) { //Check if o is an instance of Complex or not "null instanceof [type]" also returns false */
			return false;
		}
		CGNode c = (CGNode) o; // typecast o to LGGNode so that we can compare data members 
		return isEqual(c);       // Compare the data members and return accordingly 
	}

	public int hashCode(){
		return Objects.hash(this.data);
	}
	
	public boolean isEqual(CGNode anotherVertex){
		if(data.size()== anotherVertex.data.size()){
			if(data.containsAll(anotherVertex.data) && anotherVertex.data.containsAll(data)){
				return true;
			}
		}
		return false;
	}
	
	public boolean containsState(ArrayList<String> state){ //returns true if <state> contains <value>
		int [] check = new int[data.size()];
		int id = 0;
		for (String s : data) {
			for (String i : state) {
				if(i.equalsIgnoreCase(s)){
					check[id++] = 1;
				}
			}
		}
		int sum = 0;
		for (int i : check) {
			sum+=i;
		}
		return sum==data.size();
	}
	
	public String toString(){
		String s = "";
		for (String string : data) {
			s+= string +" ";
		}
		return "{"+ s.substring(0,s.length()-1) + "}";
	}

	@Override
	public int compareTo(CGNode o) {
		return this.hashCode() - o.hashCode();
	}

	public ArrayList<String> getData() {
		return data;
	}

	public void setData(ArrayList<String> data) {
		this.data = data;
	}
	
	public Object clone() throws CloneNotSupportedException {
		CGNode cloned = (CGNode) super.clone();
		ArrayList<String> clonedData = new ArrayList<>(data);
		cloned.setData(clonedData);
        return cloned;
    }
	
}
