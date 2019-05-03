package metrics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ActionSetDistance extends Distance{
	public ArrayList<String> p1;
	public ArrayList<String> p2;
	
	public ActionSetDistance(ArrayList<String> a, ArrayList<String> b) {
		super(a,b);
	}
	
	//T Nguyen 2012
	public double getActionSetDistance() {
		Set<String> s1 = new HashSet<String>();
		Set<String> s2 = new HashSet<String>();
		Set<String> s3 = new HashSet<String>();
		s1.addAll(p1);
		s2.addAll(p2);
		s1.retainAll(s2);
		s3.addAll(p1);
		s3.addAll(p2);
		return 1 - ((double) s1.size()/(double) s3.size());
	}
}
