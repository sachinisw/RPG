package landmark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class LGGNode implements Comparable<LGGNode>{
	private ArrayList<String> value;

	public LGGNode(ArrayList<String> v){
		this.value = v;
	}

	public boolean isEqual(LGGNode anotherVertex){
		if(value.size()== anotherVertex.value.size()){
			if(value.containsAll(anotherVertex.value) && anotherVertex.value.containsAll(value)){
				return true;
			}
		}
		return false;
	}

	public boolean equals(Object o){
		if (o == this) { // If the object is compared with itself then return true
			return true;
		}
		if (!(o instanceof LGGNode)) { //Check if o is an instance of Complex or not "null instanceof [type]" also returns false */
			return false;
		}
		LGGNode c = (LGGNode) o; // typecast o to LGGNode so that we can compare data members 
		return isEqual(c);       // Compare the data members and return accordingly 
	}

	public int hashCode(){
		return Objects.hash(this.value);
	}
	
	public boolean containsState(ArrayList<String> state){ //returns true if <state> contains <value>
		int [] check = new int[value.size()];
		int id = 0;
		for (String s : value) {
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
		return sum==value.size();
	}
	
	public boolean find(ArrayList<LGGNode> list){
		for (LGGNode lggNode : list) {
			if(isEqual(lggNode)){
				return true;
			}
		}
		return false;
	}

	public String toString(){
		return "{"+ Arrays.toString(value.toArray()) + "}";
	}

	@Override
	public int compareTo(LGGNode o) {
		return this.hashCode() - o.hashCode();
	}

	public ArrayList<String> getValue() {
		return value;
	}

	public void setValue(ArrayList<String> value) {
		this.value = value;
	}
}
