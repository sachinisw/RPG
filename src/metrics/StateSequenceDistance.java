package metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

import con.ConnectivityGraph;

public class StateSequenceDistance extends Distance{
	
	public ConnectivityGraph con;
	public ArrayList<TreeSet<String>> refseq;
	public ArrayList<TreeSet<String>> inseq;
	public ArrayList<String> init;
	public ArrayList<String> goal;

	public StateSequenceDistance(ArrayList<String> a, ArrayList<String> b, ConnectivityGraph c, ArrayList<String> in,
			ArrayList<String> g) {
		super(a,b);
		refseq = new ArrayList<>();
		inseq = new ArrayList<>();
		con = c;
		init = in;
		goal = g;
	}
	
	public void producePlanStateSeq() {
		TreeSet<String> in = new TreeSet<String>();
		in.addAll(init);
		refseq.add(in);
		inseq.add(in);
		ArrayList<String> currentstate = new ArrayList<String>();
		currentstate.addAll(init);
		while(!stateContainsGoal(currentstate, goal)) {
			ArrayList<String> applicables = con.findApplicableActionsInState(currentstate);
			for (int i=0; i<applicables.size(); i++) {
				if(ref.contains(applicables.get(i))) {
					ArrayList<String> adds = con.findStatesAddedByAction(applicables.get(i));
					ArrayList<String> dels = con.findStatesDeletedByAction(applicables.get(i));
					currentstate.removeAll(dels);
					currentstate.addAll(adds);
				}
			}
			TreeSet<String> st = new TreeSet<String>();
			st.addAll(currentstate);
			refseq.add(st);
		}
		currentstate.clear();
		currentstate.addAll(init);
		while(!stateContainsGoal(currentstate, goal)) {
			ArrayList<String> applicables = con.findApplicableActionsInState(currentstate);
			for (int i=0; i<applicables.size(); i++) {
				if(incoming.contains(applicables.get(i))) {
					ArrayList<String> adds = con.findStatesAddedByAction(applicables.get(i));
					ArrayList<String> dels = con.findStatesDeletedByAction(applicables.get(i));
					currentstate.removeAll(dels);
					currentstate.addAll(adds);
				}
			}
			TreeSet<String> st = new TreeSet<String>();
			st.addAll(currentstate);
			inseq.add(st);
		}
		
		for (TreeSet<String> st : refseq) {
			System.out.println(Arrays.toString(st.toArray()));
		}
		System.out.println("======================");
		for (TreeSet<String> st : inseq) {
			System.out.println(Arrays.toString(st.toArray()));
		}
	}
	
	public static ConnectivityGraph readConnectivityGraphs(){
		ConnectivityGraph graph = new ConnectivityGraph("/home/sachini/domains/TEST/testcon");
		graph.readConGraphOutput("/home/sachini/domains/TEST/testcon");
		return graph;
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
		ArrayList<String> g = new ArrayList<String>();
		g.add("(R3)");
		g.add("(R4)");
		ConnectivityGraph c = readConnectivityGraphs();
		StateSequenceDistance ssd = new StateSequenceDistance(a, b, c, in, g);
		ssd.producePlanStateSeq();
	}
}
