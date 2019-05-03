package metrics;

import java.util.ArrayList;

import con.ConnectivityGraph;

public class CausalLinkDistance  extends Distance{

	public ArrayList<String> p1;
	public ArrayList<String> p2;
	public ConnectivityGraph con;
	
	public CausalLinkDistance(ArrayList<String> a, ArrayList<String> b, ConnectivityGraph c) {
		super(a,b);
		con = c;
	}
	
	//T Nguyen 2012
	public double getCausalLinkDistance() {
		return 0;
	}
}

