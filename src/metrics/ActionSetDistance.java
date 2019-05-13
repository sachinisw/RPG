package metrics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ActionSetDistance extends Distance{
	
	public ActionSetDistance(ArrayList<String> ref, ArrayList<String> inc) {
		super(ref,inc);
	}
	
	//T Nguyen 2012
	public double getActionSetDistance() {
		Set<String> s1 = new HashSet<String>();
		Set<String> s2 = new HashSet<String>();
		Set<String> s3 = new HashSet<String>();
		s1.addAll(ref);
		s2.addAll(incoming);
		s1.retainAll(s2);
		s3.addAll(ref);
		s3.addAll(incoming);
		return 1 - ((double) s1.size()/(double) s3.size());
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
