package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

public class PartialState {
	//go to each leaf node
	//check what word it spells
	//remove hidden block and see if the word can still be spelled
	//if yes. then partial state match.
	public static LinkedList<String> getSpelledWord(ArrayList<String> state) {
		LinkedList<String> order = new LinkedList<>();
		for (String string : state) {
			String parts[] = string.split(" ");
			if(parts[0].equalsIgnoreCase("ON")) {
				System.out.println(Arrays.toString(parts));
				if(!order.contains(parts[1]) && !order.contains(parts[2])) {
					order.add(parts[1]);
					order.add(parts[2]);
				}else if(order.contains(parts[1]) && !order.contains(parts[2])) {
					int i = order.indexOf(parts[1]);
					order.add(i+1,parts[2]);
				}else if(!order.contains(parts[1]) && order.contains(parts[2])) {
					int i = order.indexOf(parts[2]);
					if(i>0) {
						order.add(i-1, parts[1]);
					}else {
						order.add(0, parts[1]);
					}
				}else if(order.contains(parts[1])&& order.contains(parts[2])) {
					int i = order.indexOf(parts[1]);
					order.add(i+1,parts[2]);
					int old = 0;
					for (int j=0; j<order.size(); j++) {
						if(order.get(j).equalsIgnoreCase(parts[2])) {
							old = j;
							break;
						}
					}
					String next = order.get(old+1);
					order.remove(old);
					order.add(i+1, next);
					order.remove(next);
				}
			}
		}
		System.out.println("ON LETTERS==== "+Arrays.toString(order.toArray()));
		return order;
	}
	public static boolean matchpartial(ArrayList<String> state, ArrayList<String> attackerword) {
		LinkedList<String> curorder = getSpelledWord(state);
		LinkedList<String> attackers = getSpelledWord(attackerword);
		Iterator<String> iter = curorder.iterator();
		while(iter.hasNext()){
			String l = iter.next();
			if(!attackers.contains(l)) {
				iter.remove();
			}
		}
		for (int i=0; i<curorder.size(); i++) {
			if(!curorder.get(i).equalsIgnoreCase(attackers.get(i))) {
				return false;
			}
		}
		return true;
	}

	public static void main(String[] args) {
		ArrayList<String> state = new ArrayList<String>();
//		state.add("CLEAR T");
//		state.add("CLEAR B");
//		state.add("ONTABLE T");
//		state.add("ONTABLE D");
//		state.add("ON B A");
//		state.add("ON A D");
//		state.add("HANDEMPTY");
		state.add("CLEAR D");
		state.add("CLEAR B");
		state.add("ONTABLE T");
		state.add("ONTABLE A");
		state.add("ON D A");
		state.add("ON B T");
		state.add("HANDEMPTY");
		//(CLEAR T), (CLEAR B), (ONTABLE T), (ONTABLE D), (ON B A), (ON A D), (HANDEMPTY)
		ArrayList<String> user= new ArrayList<String>();
		user.add("ON T A");
		user.add("ON A D");
		System.out.println(matchpartial(state, user));
	}
}
