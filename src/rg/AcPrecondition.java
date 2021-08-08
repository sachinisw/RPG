package rg;

import java.util.ArrayList;

public class AcPrecondition implements Cloneable{
	private String header = ":precondition ";
	private ArrayList<String> predicates;

	@Override
	public Object clone() throws CloneNotSupportedException {
		AcPrecondition clone = null;
		try{
			clone = (AcPrecondition) super.clone();//do a deep copy here. because array list is a shallow copy by default
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

	public AcPrecondition() {
		predicates = new ArrayList<>();
	}

	public void addPrecondition(String pred) {
		predicates.add(pred);
	}

	public String toString() {
		String preds = "";
		for (String s : predicates) {
			preds += s;
		}
		return header + preds +")\n";
	}
}
