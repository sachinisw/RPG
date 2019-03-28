package rg;

import java.util.ArrayList;

public class AcEffect implements Cloneable{
	private String header = ":effect ";
	private ArrayList<String> predicates;
	
	public AcEffect() {
		predicates = new ArrayList<>();
	}
	
	public void addEffect(String pred) {
		predicates.add(pred);
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		AcEffect clone = null;
		try{
			clone = (AcEffect) super.clone();//do a deep copy here. because array list is a shallow copy by default
			ArrayList<String> cloneparams = new ArrayList<>();
			for (String string : predicates) {
				cloneparams.add(string);
			}
			clone.setPredicates(cloneparams);
		}catch (CloneNotSupportedException e){
			throw new RuntimeException(e);
		}
		return clone;
	}
	
	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public ArrayList<String> getPredicates() {
		return predicates;
	}

	public void setPredicates(ArrayList<String> predicates) {
		this.predicates = predicates;
	}

	public String toString() {
		String preds = "";
		for (String s : predicates) {
			preds += s;
		}
		return header + preds +")\n";
	}
}
