package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public class PartialState {
	//go to each leaf node
	//check what word it spells
	//remove hidden block and see if the word can still be spelled
	//if yes. then partial state match.
	public static LinkedList<String> getSpelledWord(ArrayList<String> state) {
		LinkedList<String> order = new LinkedList<>();
		LinkedList<String> ons = new LinkedList<String>();
		for (String string : state) {
			String parts[] = string.split(" ");
			if(parts[0].equalsIgnoreCase("ON")) {
				ons.add(string);
			}
		}
		order.add(ons.getFirst().split(" ")[1]);
		order.add(ons.getFirst().split(" ")[2]);
		ons.removeFirst();
		while(!ons.isEmpty()) { //if next element is adjacent. then add to order
			String s = ons.removeFirst();
			String parts[] = s.split(" ");
			if(!order.contains(parts[1]) && !order.contains(parts[2]) && ons.size()==0) {
				order.add(parts[1]);
				order.add(parts[2]);
			}else if(!order.contains(parts[1]) && !order.contains(parts[2]) && ons.size()>0) {
				ons.addLast(s);
			}else if(order.contains(parts[1]) && !order.contains(parts[2])) { //insert after parts[1] in order
				int i = order.indexOf(parts[1]);
				order.add(i+1, parts[2]);
			}else if(!order.contains(parts[1]) && order.contains(parts[2])) { //insert after 
				int i = order.indexOf(parts[2]);
				if(i>0) {
					order.add(i-1, parts[1]);
				}else {
					order.add(0, parts[1]);
				}
			}
		}
		return order;
	}
	public static boolean matchpartial(ArrayList<String> state, ArrayList<String> attackerword) {
		LinkedList<String> cur = getSpelledWord(state);
		LinkedList<String> req = getSpelledWord(attackerword);
		String curword = "", reqword = "", hidden="";
		if(!cur.isEmpty()) {
			for (String s : req) {
				reqword+=s;
			}
			for (String s : cur) {
				curword+=s;
			}
			if(curword.equalsIgnoreCase(reqword)) {
				return true;
			}
			StringBuilder sb = new StringBuilder(curword); 	//find invisible letter. remove it. see if remainder spells required word
			for(int i=0; i<curword.length(); i++) {
				String letter = String.valueOf(curword.charAt(i));
				if(!reqword.contains(letter)) {
					hidden=letter;
				}
			}
			if(hidden.length()>0) {
				sb.deleteCharAt(curword.indexOf(hidden));
			}
			return sb.toString().equalsIgnoreCase(reqword);
		}	
		return false;
	}

	public static void main(String[] args) {
		ArrayList<String> state = new ArrayList<String>();
		//checking-----------------[(CLEAR D), (ONTABLE R), (ON W R), (ON D A), (HANDEMPTY), (ON A W)]
//				state.add("CLEAR D");
//				state.add("ONTABLE R");
//				state.add("ON W R");
//				state.add("ON D A");
//				state.add("HANDEMPTY");
//				state.add("ON A W");
		//picked>>>>>>>>>>>>>>>[(ON R D), (CLEAR R), (ON D A), (HANDEMPTY), (ON A W), (ONTABLE W)]
//				state.add("ON R D");
//				state.add("CLEAR D");
//				state.add("ON D A");
//				state.add("HANDEMPTY");
//				state.add("ON A W");
//				state.add("ONTABLE W");
		//picked>>>>>>>>>>>>>>>[(ON R A), (CLEAR D), (HANDEMPTY), (ON D R), (ON A W), (ONTABLE W)]
//		state.add("ON R A");
//		state.add("CLEAR D");
//		state.add("HANDEMPTY");
//		state.add("ON A W");
//		state.add("ON D R");
//		state.add("ONTABLE W");
		//picked>>>>>>>>>>>>>>>[(CLEAR D), (ON R W), (ON A R), (ON D A), (HANDEMPTY), (ONTABLE W)]
//				state.add("CLEAR D");
//				state.add("ON R W");
//				state.add("ON A R");
//				state.add("ON D A");
//				state.add("HANDEMPTY");
//				state.add("ONTABLE W");

		//(ON R A), (CLEAR R), (ONTABLE A), (ON W D), (ONTABLE D), (HANDEMPTY), (CLEAR W) - false
//				state.add("ON R A");
//				state.add("CLEAR R");
//				state.add("ONTABLE A");
//				state.add("ON W D");
//				state.add("ONTABLE D");
//				state.add("HANDEMPTY");
//				state.add("CLEAR W");

		//(CLEAR A), (ON A O), (ONTABLE O), (ON W E), (ONTABLE E), (HANDEMPTY), (CLEAR W)
		//(ON O E), (CLEAR A), (ONTABLE E), (HANDEMPTY), (ON W O), (ON A W)
		//(CLEAR A), (ON A O), (ONTABLE O), (ON W E), (ONTABLE E), (HANDEMPTY), (CLEAR W)
		//(ON T A), (CLEAR B), (ONTABLE D), (ON B T), (ON A D), (HANDEMPTY)
		state.add("ON T A");
		state.add("CLEAR B");
		state.add("ONTABLE D");
		state.add("ON B T");
		state.add("ON A D");
		state.add("HANDEMPTY");

		Collections.sort(state);
		ArrayList<String> user= new ArrayList<String>();
		user.add("ON B A");
		user.add("ON A D");
		System.out.println(matchpartial(state, user));
	}
}
