package metrics;

import java.util.ArrayList;
import java.util.TreeSet;

import con.ConnectivityGraph;

public class CausalLinkDistance  extends Distance{
	private ArrayList<CausalLink> ref_causal; //observation prefix + optimal suffix plan
	private ArrayList<CausalLink> in_causal; //plan coming from sampled set of alternatives
	private ConnectivityGraph con;
	private ArrayList<String> init;
	private ArrayList<String> goal;

	public CausalLinkDistance(ArrayList<String> a, ArrayList<String> b, ConnectivityGraph c, ArrayList<String> in, ArrayList<String> g) {
		super(a,b);
		con = c;
		init = in;
		goal = g;
		ref_causal = new ArrayList<>();
		in_causal = new ArrayList<>();
	}

	public void extractCausalLinks() {
		extractCausalLinksFromReferencePlan();
		extractCausalLinksFromIncomingPlan();
	}

	public void extractCausalLinksFromReferencePlan() {
		TreeSet<String> currentstate =  new TreeSet<String>();
		ArrayList<CausalLink> goalLinks = new ArrayList<>();
		currentstate.addAll(init);
		ArrayList<String> initApplicables = con.findApplicableActionsInState(new ArrayList<String>(currentstate));
		for (String ia : initApplicables) { //adding init connections
			if(ref.contains(ia)) {
				ArrayList<String> pre = con.findPreconditionsofAction(ia);
				String propositions = "";
				for (String b : pre) {
					propositions += b;
				}
				CausalLink l = new CausalLink(propositions,"A_I", ia); 
				ref_causal.add(l);
			}
		}
		while(!stateContainsGoal(new ArrayList<String>(currentstate))) {
			ArrayList<String> applicables = con.findApplicableActionsInState(new ArrayList<String>(currentstate));
			for (String ap : applicables) {
				if(ref.contains(ap)) {
					ArrayList<String> adds = con.findStatesAddedByAction(ap);
					ArrayList<String> dels = con.findStatesDeletedByAction(ap);
					currentstate.removeAll(dels);
					currentstate.addAll(adds);
					for (String ad : adds) {
						ArrayList<String> consumers = findConsumerofPredicate(ad, ap, ref);
						for (String c : consumers) {
							CausalLink cl = new CausalLink (ad,ap,c);
							if(!ref_causal.contains(cl)) {
								ref_causal.add(cl);
							}
						}
						if(goal.contains(ad)) {
							CausalLink gl = new CausalLink(ad,ap, "A_G");
							if(!goalLinks.contains(gl)) {
								goalLinks.add(gl);
							}
						}
					}
				}
			}
		}
		ref_causal.addAll(goalLinks);
	}

	public void extractCausalLinksFromIncomingPlan() {
		TreeSet<String> currentstate =  new TreeSet<String>();
		ArrayList<CausalLink> goalLinks = new ArrayList<>();
		currentstate.addAll(init);
		ArrayList<String> initApplicables = con.findApplicableActionsInState(new ArrayList<String>(currentstate));
		for (String ia : initApplicables) { //adding init connections
			if(incoming.contains(ia)) {
				ArrayList<String> pre = con.findPreconditionsofAction(ia);
				String propositions = "";
				for (String b : pre) {
					propositions += b;
				}
				CausalLink l = new CausalLink(propositions,"A_I", ia); 
				in_causal.add(l);
			}
		}
		while(!stateContainsGoal(new ArrayList<String>(currentstate))) {
			ArrayList<String> applicables = con.findApplicableActionsInState(new ArrayList<String>(currentstate));
			for (String ap : applicables) {
				if(incoming.contains(ap)) {
					ArrayList<String> adds = con.findStatesAddedByAction(ap);
					ArrayList<String> dels = con.findStatesDeletedByAction(ap);
					currentstate.removeAll(dels);
					currentstate.addAll(adds);
					for (String ad : adds) {
						ArrayList<String> consumers = findConsumerofPredicate(ad, ap, incoming);
						for (String c : consumers) {
							CausalLink cl = new CausalLink (ad,ap,c);
							if(!in_causal.contains(cl)) {
								in_causal.add(cl);
							}
						}
						if(goal.contains(ad)) {
							CausalLink gl = new CausalLink(ad,ap, "A_G");
							if(!goalLinks.contains(gl)) {
								goalLinks.add(gl);
							}
						}
					}
				}
			}
		}
		in_causal.addAll(goalLinks);
	}
	
	public ArrayList<String> findConsumerofPredicate(String addPred, String producerac, ArrayList<String> plan) {
		int index = plan.indexOf(producerac);
		ArrayList<String> consumers = new ArrayList<String>();
		for (String step : plan) {
			ArrayList<String> pre = con.findPreconditionsofAction(step);
			if(plan.indexOf(step)>index && pre.contains(addPred)) {
				consumers.add(step);
			}
		}
		return consumers;
	}

	public boolean stateContainsGoal(ArrayList<String> state) {
		for (String g : goal) {
			if(!state.contains(g)) {
				return false;
			}
		}
		return true;
	}

	//T Nguyen 2012
	public double getCausalLinkDistance() {
		extractCausalLinks();
		ArrayList<CausalLink> intersection = new ArrayList<CausalLink>();
		TreeSet<CausalLink> union = new TreeSet<CausalLink>();
		for (CausalLink cl : ref_causal) {
			if(in_causal.contains(cl)) {
				intersection.add(cl);
			}
		}
		union.addAll(ref_causal);
		union.addAll(in_causal);
		return 1 - ((double) intersection.size()/(double) union.size());
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
		CausalLinkDistance cld = new CausalLinkDistance(a, b, con, in, g);
		double d = cld.getCausalLinkDistance();
		System.out.println(d);
	}

	public ArrayList<CausalLink> getIn_causal() {
		return in_causal;
	}

	public void setIn_causal(ArrayList<CausalLink> in_causal) {
		this.in_causal = in_causal;
	}

	public ArrayList<CausalLink> getRef_causal() {
		return ref_causal;
	}

	public void setRef_causal(ArrayList<CausalLink> ref_causal) {
		this.ref_causal = ref_causal;
	}

	public ConnectivityGraph getCon() {
		return con;
	}

	public void setCon(ConnectivityGraph con) {
		this.con = con;
	}

	public ArrayList<String> getInit() {
		return init;
	}

	public void setInit(ArrayList<String> init) {
		this.init = init;
	}

	public ArrayList<String> getGoal() {
		return goal;
	}

	public void setGoal(ArrayList<String> goal) {
		this.goal = goal;
	}
}

