package metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;
import static java.util.stream.Collectors.*;
import static java.util.Map.Entry.*;

import con.ConnectivityGraph;

public class CausalLinkDistance  extends Distance{
	private ArrayList<CausalLink> ref_causal; //observation prefix + optimal suffix plan
	private ArrayList<CausalLink> in_causal; //plan coming from sampled set of alternatives
	private ConnectivityGraph con;
	private ArrayList<String> init;
	private ArrayList<String> goal;

	public CausalLinkDistance(ArrayList<String> ref, ArrayList<String> inc, ConnectivityGraph c, ArrayList<String> in, ArrayList<String> g) {
		super(ref,inc);
		con = c;
		init = in;
		goal = g;
		ref_causal = new ArrayList<>();
		in_causal = new ArrayList<>();
	}
	
	//need to extract causal links only for the reference plan
	public CausalLinkDistance(ArrayList<String> ref, ConnectivityGraph c, ArrayList<String> in, ArrayList<String> g) {
		super(ref,null);
		con = c;
		init = in;
		goal = g;
		ref_causal = new ArrayList<>();
		in_causal = new ArrayList<>();
	}
	
	public void extractCausalLinksForReferencePlanForCriticalState() {
		extractCausalLinksFromReferencePlan();
	}

	public void extractCausalLinks() {
		extractCausalLinksFromReferencePlan();
		extractCausalLinksFromIncomingPlan();
	}

	public HashMap<String, Integer> getApplicableActionPriority(ArrayList<String> plan, ArrayList<String> applicables) {
		HashMap<String,Integer> priority = new HashMap<>();
		for (String app : applicables) {
			priority.put(app, plan.indexOf(app)); //will give me the first occurrence of this action. I think it will be ok. make sure TODO:::
		}
		return priority;
	}

	public ArrayList<State> producePlanStateSeq(ArrayList<String> plan) {
		TreeSet<String> in = new TreeSet<String>(init);
		ArrayList<State> seq = new ArrayList<>();
		seq.add(new State(in));
		TreeSet<String> currentstate = new TreeSet<String>(init);
		while(!stateContainsGoal(new ArrayList<>(currentstate), goal)) {
			ArrayList<String> applicables = con.findApplicableActionsInState(new ArrayList<>(currentstate));
			for (int i=0; i<applicables.size(); i++) {
				if(plan.contains(applicables.get(i))) {
					ArrayList<String> adds = con.findStatesAddedByAction(applicables.get(i));
					ArrayList<String> dels = con.findStatesDeletedByAction(applicables.get(i));
					currentstate.removeAll(dels);
					currentstate.addAll(adds);
				}
			}
			TreeSet<String> st = new TreeSet<String>(currentstate);
			seq.add(new State(st));
		}
		return seq;
	}

	public ArrayList<String> filterNonConcurrentActions(ArrayList<String> applicables, TreeSet<String> currentstate, HashMap<String, Integer> priority,
			ArrayList<TreeSet<String>> alreadyseen) {
		ArrayList<String> filtered = new ArrayList<String>();
		TreeSet<String> curstatecopy = new TreeSet<String>();
		curstatecopy.addAll(currentstate);
		HashMap<String, Integer> sorted = priority.entrySet().stream().sorted(comparingByValue()).
				collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
		Iterator<String> itr = sorted.keySet().iterator();
		while (itr.hasNext()) {
			String ac = (String) itr.next();
			if(sorted.get(ac)>=0) {
				ArrayList<String> adds = con.findStatesAddedByAction(ac);
				ArrayList<String> dels = con.findStatesDeletedByAction(ac);
				ArrayList<String> pre = con.findPreconditionsofAction(ac);
				TreeSet<String> effect = new TreeSet<String>(curstatecopy);
				effect.removeAll(dels);
				effect.addAll(adds);
				if(canExecuteInCurrentState(pre, curstatecopy) && !alreadySeenState(alreadyseen, effect)) {
					curstatecopy.removeAll(dels);
					curstatecopy.addAll(adds);
					filtered.add(ac);//added in the acending order of priority
					alreadyseen.add(curstatecopy);
				}
			}	
		}
		return filtered;
	}

	public boolean alreadySeenState(ArrayList<TreeSet<String>> history, TreeSet<String> state) {
		TreeSet<String> prevstate = history.get(history.size()-1);
		for (String s : prevstate) {
			if(!state.contains(s)) {
				return false;
			}
		}
		return true;
	}

	public boolean canExecuteInCurrentState(ArrayList<String> pre, TreeSet<String> current) {
		for (String p : pre) {
			if(!current.contains(p)) {
				return false;
			}
		}
		return true;
	}

	class SpecialAction{
		public String name;
		public ArrayList<String> adds;
		public ArrayList<String> dels;
		public ArrayList<String> pres;

		public SpecialAction(String nm) {
			name = nm;
			adds = new ArrayList<String>();
			dels = new ArrayList<String>();
			pres = new ArrayList<String>();
		}
	}

	//source: https://www.cs.cmu.edu/afs/cs/project/jair/pub/volume15/ambite01a-html/node21.html FIGURE 17 whoopee!!
	public void extractCausalLinksFromReferencePlan() {
		SpecialAction ini = new SpecialAction("A_I"); //pre=[], add=[init]
		ini.adds.addAll(init);
		SpecialAction go = new SpecialAction("A_G");//pre=[goal], add=[]
		go.pres.addAll(goal);
		ArrayList<String> refdummy = new ArrayList<>(ref);
		refdummy.add(0,ini.name);//append special actions to the end and to the start
		refdummy.add(go.name);
		for (int i=refdummy.size()-1; i>=0; i--) {
			String ai = refdummy.get(i);
			ArrayList<String> pre = null;
//			ArrayList<String> del = null;
			if(i==refdummy.size()-1) {
				pre = go.pres;
//				del = new ArrayList<String>();
			}else {
				pre = con.findPreconditionsofAction(ai);
//				del = con.findStatesDeletedByAction(ai);
			}
			for (String p : pre) {
				for (int k=i-1; k>=0; k--) {
					String ak = refdummy.get(k);
					ArrayList<String> adds = null;
					if(k==0) {
						adds = ini.adds;
					}
					else{
						adds = con.findStatesAddedByAction(ak);
					}
					boolean pinadd = adds.contains(p); //predicate added by some action coming before
					boolean foundL = false;
					for (int l=k-1; l>=0; l--) {
						ArrayList<String> dels = con.findStatesDeletedByAction(refdummy.get(l));
						if(dels.contains(p)) {
							foundL = true;
						}
					}
					if(pinadd && !foundL) {
						ref_causal.add(new CausalLink(p, ak, ai));
					}
				}
			}
			//			for (String d : del) { //FYI: I don't have to do this part because the relaxed version of the problem (which I use) ignores the negative effects.
			//				for (int j=i-1; j>=1; j--) {
			//					String aj = refdummy.get(j);
			//					ArrayList<String> ajpre = con.findPreconditionsofAction(aj);
			//					if(ajpre.contains(d)) {
			//						ref_causal.add(new CausalLink(d, aj, ai));
			//					}
			//				}
			//			}
		}
	}

	public void extractCausalLinksFromIncomingPlan() {
		SpecialAction ini = new SpecialAction("A_I");
		ini.adds.addAll(init);
		SpecialAction go = new SpecialAction("A_G");
		go.pres.addAll(goal);
		ArrayList<String> incomingdummy = new ArrayList<>(incoming);
		incomingdummy.add(0,ini.name);
		incomingdummy.add(go.name);
		for (int i=incomingdummy.size()-1; i>=0; i--) {
			String ai = incomingdummy.get(i);
			ArrayList<String> pre = null;
//			ArrayList<String> del = null;
			if(i==incomingdummy.size()-1) {
				pre = go.pres;
//				del = new ArrayList<String>();
			}else {
				pre = con.findPreconditionsofAction(ai);
//				del = con.findStatesDeletedByAction(ai);
			}
			for (String p : pre) {
				for (int k=i-1; k>=0; k--) {
					String ak = incomingdummy.get(k);
					ArrayList<String> adds = null;
					if(k==0) {
						adds = ini.adds;
					}
					else{
						adds = con.findStatesAddedByAction(ak);
					}
					boolean pinadd = adds.contains(p); //predicate added by some action coming before
					boolean foundL = false;
					for (int l=k-1; l>=0; l--) {
						ArrayList<String> dels = con.findStatesDeletedByAction(incomingdummy.get(l));
						if(dels.contains(p)) {
							foundL = true;
						}
					}
					if(pinadd && !foundL) {
						in_causal.add(new CausalLink(p, ak, ai));
					}
				}
			}
//			for (String d : del) { //FYI: I don't have to do this part because the relaxed version of the problem (which I use) ignores the negative effects.
//				for (int j=i-1; j>=1; j--) {
//					String aj = incomingdummy.get(j);
//					ArrayList<String> ajpre = con.findPreconditionsofAction(aj);
//					if(ajpre.contains(d)) {
//						ref_causal.add(new CausalLink(d, aj, ai));
//					}
//				}
//			}
		}
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
		for (CausalLink cl : ref_causal) {
			if(in_causal.contains(cl)) {
				intersection.add(cl);
			}
		}
		double union = (double) ref_causal.size()+in_causal.size()-intersection.size();
		return 1 - ((double) intersection.size()/union);
	}

	public static ConnectivityGraph readConnectivityGraphs(){
		ConnectivityGraph graph = new ConnectivityGraph("/home/sachini/domains/Nguye/testcon");
		graph.readConGraphOutput("/home/sachini/domains/Nguye/testcon");
		return graph;
	}

	public static void main(String[] args) {
		ArrayList<String> in = new ArrayList<String>();
		in.add("(RA)");
		ArrayList<String> a = new ArrayList<String>();
		a.add("AA");
		a.add("AB");
		a.add("AC");
		ArrayList<String> b = new ArrayList<String>();
		b.add("AA");
		b.add("AB");
		b.add("AD");
		ArrayList<String> c = new ArrayList<String>();
		c.add("AE");
		c.add("AF");
		ArrayList<String> g = new ArrayList<String>();
		g.add("(RC)");
		g.add("(RD)");
		ConnectivityGraph con = readConnectivityGraphs();
		CausalLinkDistance cld = new CausalLinkDistance(c, b, con, in, g);
		System.out.println(cld.getCausalLinkDistance());
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

