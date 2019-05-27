package metrics;

import java.util.ArrayList;

public class ActionSetDistance extends Distance{
	
	public ActionSetDistance(ArrayList<String> ref, ArrayList<String> inc) {
		super(ref,inc);
	}
	
	//T Nguyen 2012
	public double getActionSetDistance() {
		int intersectionsize = 0, unionsize = 0;
		for (String in : incoming) {
			if(ref.contains(in)) {
				intersectionsize++;
			}
		}
		unionsize = incoming.size()+ref.size() - intersectionsize;
		return 1 - ((double) intersectionsize/(double) unionsize);
	}
		
	public static void main(String[] args) {
		ArrayList<String> in = new ArrayList<String>();
		in.add("(R1)");
		ArrayList<String> a = new ArrayList<String>();
		a.add("A1");
		a.add("A2");
		a.add("A3");
		ArrayList<String> b = new ArrayList<String>();
		b.add("A1");
		b.add("A2");
		b.add("A4");
		ArrayList<String> c = new ArrayList<String>();
		c.add("A5");
		c.add("A6");
		ArrayList<String> g = new ArrayList<String>();
		g.add("(R3)");
		g.add("(R4)");
		ActionSetDistance asd = new ActionSetDistance(a, c);
		double d = asd.getActionSetDistance();
		System.out.println(d);
	}
}
