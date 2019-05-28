package metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

public class Distance {
	public ArrayList<String> ref; //observation prefix + optimal suffix plan ||| or state sequences
	public ArrayList<String> incoming; //plan/state sequences coming from sampled set of alternatives
	public HashMap<String, String> tokenmap;

	public Distance(ArrayList<String> a, ArrayList<String> b) {
		ref = a;
		incoming = b;
		tokenmap = new HashMap<String, String>();
	}

	public boolean stateContainsGoal(ArrayList<String> state, ArrayList<String> goal) {
		for (String g : goal) {
			boolean found = false;
			for (String s : state) {
				if(s.equalsIgnoreCase(g)) {
					found = true;
				}
			}
			if(!found) {
				return false;
			}
		}
		return true;
	}

	public void tokenizeActions(){ //use hash code of action step to tokenize
		for (String string : incoming) {
			if(!tokenmap.containsKey(string)) {
				tokenmap.put(string,"");
			}
		}
		for (String string : ref) {
			if(!tokenmap.containsKey(string)) {
				tokenmap.put(string,"");
			}
		}
		Iterator<Entry<String, String>> itr = tokenmap.entrySet().iterator();
		while(itr.hasNext()) {
			String key = itr.next().getKey();
			tokenmap.put(key, String.valueOf(key.hashCode()));
		}
	}

	public void tokenizeStates() {
		HashMap<HashSet<String>, ArrayList<Integer>> uniquer = findUniqueStrings(ref);//state-position index pairs
		HashMap<HashSet<String>, ArrayList<Integer>> uniquei = findUniqueStrings(incoming);
		HashMap<String, ArrayList<String>> tokens = new HashMap<>(); //hashcode - array index pairs
		Iterator<HashSet<String>> initr = uniquer.keySet().iterator();
		Iterator<HashSet<String>> initi = uniquei.keySet().iterator();
		while(initr.hasNext()) {
			HashSet<String> key = initr.next();
			ArrayList<Integer> ids = uniquer.get(key);
			ArrayList<String> indexes = null;
			String stk = "";
			for (String string : key) {
				stk += string;
			}
			if(!tokens.containsKey(String.valueOf(stk.hashCode()))) {
				indexes = new ArrayList<String>();
			}else {
				indexes = tokens.get(String.valueOf(stk.hashCode()));
			}
			for (Integer i : ids) {
				indexes.add(String.valueOf(i)+"R");
			}
			tokens.put(String.valueOf(stk.hashCode()),indexes);
		}
		while(initi.hasNext()) {
			HashSet<String> key = initi.next();
			ArrayList<Integer> ids = uniquei.get(key);
			ArrayList<String> indexes = null;
			String stk = "";
			for (String string : key) {
				stk += string;
			}
			if(!tokens.containsKey(String.valueOf(stk.hashCode()))) {
				indexes = new ArrayList<String>();
			}else {
				indexes = tokens.get(String.valueOf(stk.hashCode()));
			}
			for (Integer i : ids) {
				indexes.add(String.valueOf(i)+"I");
			}
			tokens.put(String.valueOf(stk.hashCode()),indexes);
		}
		Iterator<String> itrt = tokens.keySet().iterator();
		while(itrt.hasNext()) { //put tokens in tokenmap (hashcode, corresponding list index) pairs
			String code = itrt.next();
			ArrayList<String> ids = tokens.get(code);
			String s = "";
			for (String string : ids) {
				s += string + ",";
			}
			tokenmap.put(code,s.substring(0,s.length()-1));
		}
	}

	private HashMap<HashSet<String>, ArrayList<Integer>> findUniqueStrings(ArrayList<String> in){
		HashMap<HashSet<String>, ArrayList<Integer>> lists = new HashMap<>();
		int index = 0;
		for (String s : in) {
			HashSet<String> items = new HashSet<>();
			String [] l = s.split("\\)");
			for (String st : l) {
				items.add(st+")");
			}
			if(lists.containsKey(items)) {
				ArrayList<Integer> ids = lists.get(items);
				ids.add(index);
			}else {
				ArrayList<Integer> ids = new ArrayList<>();
				ids.add(index);
				lists.put(items,ids);
			}
			index++;
		}
		return lists;
	}

	public static void main(String[] args) {
		String a = "(CLEAR A)(CLEAR D)(CLEAR R)(CLEAR W)(HANDEMPTY)(ONTABLE A)(ONTABLE D)(ONTABLE R)(ONTABLE W)";
		String b =  "(CLEAR A)(CLEAR D)(CLEAR R)(HOLDING W)(ONTABLE A)(ONTABLE D)(ONTABLE R)";
		String c = "(CLEAR D)(CLEAR A)(CLEAR R)(CLEAR W)(HANDEMPTY)(ONTABLE A)(ONTABLE D)(ONTABLE R)(ONTABLE W)";
		String d = "(CLEAR D)(CLEAR R)(CLEAR W)(HOLDING A)(ONTABLE D)(ONTABLE R)(ONTABLE W)";
		String e = "(CLEAR A)(CLEAR D)(CLEAR R)(HANDEMPTY)(ON A W)(ONTABLE D)(ONTABLE R)(ONTABLE W)";
		String f = "(CLEAR A)(CLEAR D)(HOLDING R)(ON A W)(ONTABLE D)(ONTABLE W)";
		String g = "(CLEAR D)(CLEAR R)(HANDEMPTY)(ON A W)(ON R A)(ONTABLE D)(ONTABLE W)";
		System.out.println(a.hashCode());
		System.out.println(b.hashCode());
		System.out.println(c.hashCode());
		System.out.println(d.hashCode());
		System.out.println(e.hashCode());
		System.out.println(f.hashCode());
		System.out.println(g.hashCode());
		ArrayList<String> aa = new ArrayList<String>();
		aa.add(a);
		aa.add(b);
		aa.add(c);
		aa.add(d);
		aa.add(e);
		aa.add(f);
		aa.add(g);
	}
}
