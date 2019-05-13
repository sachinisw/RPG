package metrics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import con.ConnectivityGraph;

public class StateSequenceDistance extends Distance{

	public ConnectivityGraph con;
	public ArrayList<State> refseq;
	public ArrayList<State> inseq;
	public ArrayList<String> init;
	public ArrayList<String> goal;

	public StateSequenceDistance(ArrayList<String> a, ArrayList<String> b, ConnectivityGraph c, ArrayList<String> inits,
			ArrayList<String> goals) {
		super(a,b);
		refseq = new ArrayList<>();
		inseq = new ArrayList<>();
		con = c;
		init = inits;
		goal = goals;
	}

	public void producePlanStateSeq() {
		TreeSet<String> in = new TreeSet<String>(init);
		refseq.add(new State(in));
		inseq.add(new State(in));
		TreeSet<String> currentstate = new TreeSet<String>(init);
		while(!stateContainsGoal(new ArrayList<>(currentstate), goal)) {
			ArrayList<String> applicables = con.findApplicableActionsInState(new ArrayList<>(currentstate));
			for (int i=0; i<applicables.size(); i++) {
				if(ref.contains(applicables.get(i))) {
					ArrayList<String> adds = con.findStatesAddedByAction(applicables.get(i));
					ArrayList<String> dels = con.findStatesDeletedByAction(applicables.get(i));
					currentstate.removeAll(dels);
					currentstate.addAll(adds);
				}
			}
			TreeSet<String> st = new TreeSet<String>(currentstate);
			refseq.add(new State(st));
		}
		currentstate.clear();
		currentstate.addAll(init);
		while(!stateContainsGoal(new ArrayList<>(currentstate), goal)) {
			ArrayList<String> applicables = con.findApplicableActionsInState(new ArrayList<>(currentstate));
			for (int i=0; i<applicables.size(); i++) {
				if(incoming.contains(applicables.get(i))) {
					ArrayList<String> adds = con.findStatesAddedByAction(applicables.get(i));
					ArrayList<String> dels = con.findStatesDeletedByAction(applicables.get(i));
					currentstate.removeAll(dels);
					currentstate.addAll(adds);
				}
			}
			TreeSet<String> st = new TreeSet<String>(currentstate);
			inseq.add(new State(st));
		}
	}

	//Nguyen 2012
	public double getStateSequenceDistance() {
		producePlanStateSeq();
		int k = 0, kprime = 0;
		double deltasum = 0.0;
		if(inseq.size()<refseq.size()) {
			k = refseq.size() - 1;
			kprime = inseq.size() - 1;
			for(int i=0; i<=kprime; i++) {
				Set<String> s = new HashSet<String>(refseq.get(i).getPredicates());
				Set<String> sprime = new HashSet<String>(inseq.get(i).getPredicates());
				Set<String> union = new HashSet<String>();
				Set<String> intersection = new HashSet<String>();
				for (String st : s) {
					if(sprime.contains(st)) {
						intersection.add(st);
					}
				}
				union.addAll(s);
				union.addAll(sprime);
				deltasum += 1.0 - ((double) intersection.size()/(double) union.size());
			}
		}else {
			k = inseq.size() - 1;
			kprime = refseq.size() - 1;
			for(int i=0; i<=kprime; i++) {
				Set<String> s = new HashSet<String>(inseq.get(i).getPredicates());
				Set<String> sprime = new HashSet<String>(refseq.get(i).getPredicates());
				Set<String> union = new HashSet<String>();
				s.retainAll(sprime);
				union.addAll(s);
				union.addAll(sprime);
				deltasum += ((double) s.size()/(double) union.size());
			}
		}
		return (double)( (1/(double)(k) )*(deltasum + (double)(k-kprime)));
	}

	public int minDsistanceToGoal() {
		producePlanStateSeq();
		int loc=0;
		for (State st : inseq) {
			loc++;
			if(st.containsGoal(goal)) {
				return loc;
			}
		}
		return loc;
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
		ArrayList<String> c = new ArrayList<String>();
		c.add("A5");
		c.add("A6");
		ArrayList<String> g = new ArrayList<String>();
		g.add("(R3)");
		g.add("(R4)");
		ConnectivityGraph con = readConnectivityGraphs();
		StateSequenceDistance ssd = new StateSequenceDistance(a, c, con, in, g);
		System.out.println(ssd.getStateSequenceDistance());
	}
}
