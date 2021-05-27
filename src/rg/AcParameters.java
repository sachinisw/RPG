package rg;

import java.util.ArrayList;

public class AcParameters implements Cloneable{
	private String header = ":parameters ";
	private ArrayList<String> paramlist;

	public AcParameters() {
		paramlist = new ArrayList<>();
	}

	public void addParameter(String param) {
		paramlist.add(param);
	}

	public String toString() {
		String params = "";
		for (String s : paramlist) {
			params += s+" ";
		}
		return header + "("+ params +")\n";
	}

	public ArrayList<String> getParamlist() {
		return paramlist;
	}

	public void setParamlist(ArrayList<String> paramlist) {
		this.paramlist = paramlist;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		AcParameters clone = null;
		try{
			clone = (AcParameters) super.clone();//do a deep copy here. because array list is a shallow copy by default
			ArrayList<String> clonedparams = new ArrayList<>();
			for (String string : paramlist) {
				clonedparams.add(string);
			}
			clone.setParamlist(clonedparams);
		}catch (CloneNotSupportedException e){
			throw new RuntimeException(e);
		}
		return clone;
	}
}
