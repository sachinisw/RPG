package landmark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import con.ConnectivityGraph;

public class RelaxedPlanningGraphGenerator {
	public boolean atL;
	public boolean propositionLevel;
	public boolean actionLevel;
	public boolean first;
	public int currentLevel;
	public int counter;
	public RelaxedPlanningGraph rpg;
	public ConnectivityGraph con;

	public RelaxedPlanningGraphGenerator(){
		rpg = new RelaxedPlanningGraph();
		con = new ConnectivityGraph("");
		propositionLevel = false;
		actionLevel = false;
		atL = false;
		currentLevel = 0;
		counter = 0;
		first = true;
	}

	public void populate(String inputStr){
		GraphLevel l = null;
		Pattern p0 = Pattern.compile("LEVEL\\s[0-9]:{1,}");
		Matcher m0 = p0.matcher(inputStr.trim());
		if(m0.find()){
			l = new GraphLevel();
			l.setLevelType(inputStr.substring(5, inputStr.length()-1));
			currentLevel = Integer.parseInt(inputStr.substring(5, inputStr.length()-1).trim());
			if(inputStr.substring(5, inputStr.length()-1).trim().equals("0")){
				atL = true;
				first = false;
				counter++;
			}
			if(counter<2 && !first){
				rpg.setLevel(l);
			}
		}
		if(counter<2){
			if(inputStr.trim().equalsIgnoreCase("FACTS:") && atL){
				propositionLevel = true;
			}else if (inputStr.trim().equalsIgnoreCase("EFS:") && atL){
				actionLevel = true;
			}
			if(propositionLevel && (inputStr.startsWith("(") )){
				rpg.getLevel(currentLevel).getPropositionLayer().add(inputStr.trim());
			}
			if(inputStr.trim().equals("") && propositionLevel){
				propositionLevel = false;
			}
			if(actionLevel && (inputStr.startsWith("effect") )){
				String eff = inputStr.substring(inputStr.indexOf("to")+3,inputStr.length()).trim();
				rpg.getLevel(currentLevel).getActionLayer().add(eff);
			}
			if(inputStr.trim().equals("") && actionLevel){
				actionLevel = false;
			}
		}
	}

	public void readFFOutput(String filename){	
		String outStr="";
		try {
			FileReader fileReader = new FileReader(filename);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			while((outStr = bufferedReader.readLine()) != null) {
				populate(outStr);             
			}    
			bufferedReader.close();            
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<ConnectivityGraph> readConnectivityGraphs(String inputfile){
		ArrayList<ConnectivityGraph> connectivities = new ArrayList<ConnectivityGraph>();
		ConnectivityGraph graph = new ConnectivityGraph(inputfile);
		graph.readConGraphOutput(inputfile);
		connectivities.add(graph);
		return connectivities;
	}

	public void runLandmarkGenerator(String inputfilerpg, String inputfilecon, ArrayList<String> goalstate, ArrayList<String> init, String lmoutput) {
		RelaxedPlanningGraphGenerator test = new RelaxedPlanningGraphGenerator();
		test.readFFOutput(inputfilerpg);
		ArrayList<ConnectivityGraph> cons = test.readConnectivityGraphs(inputfilecon);
		LandmarkExtractor lm = new LandmarkExtractor(test.rpg, cons.get(0));
		LGG lgg = lm.extractLandmarks(goalstate);
		lm.verifyLandmarks(lgg, goalstate, init, lmoutput); //writes cleaned landmarks to lmoutput
	}

	//REMOVE AFTER DEBUG
	public HashMap<String,TreeSet<String>> readLandmarksGNOrders(String lmfile){
		Scanner sc;
		HashMap<String,TreeSet<String>> lms = new HashMap<String, TreeSet<String>>();	
		boolean start = false;
		try {
			sc = new Scanner(new File(lmfile));
			while(sc.hasNextLine()) {
				String line = sc.nextLine().trim();
				if(line.contains(":LGG GREEDY NECESSARY ORDERS")) {
					start = true;
				}
				if(start && line.contains("UNVERIFIED")) {
					start = false;
				}
				if(start && !line.contains("LGG GREEDY NECESSARY ORDERS") && !line.isEmpty()) {
					String parts [] = line.split(":");
					String key = parts[0].substring(parts[0].indexOf("[")+1,parts[0].indexOf("]"));
					if(!lms.containsKey(key)) {
						lms.put(key,new TreeSet<String>());
					}
					TreeSet<String> set = lms.get(key);
					String val = parts[1].substring(2,parts[1].length()-1);
					if(!val.isEmpty()) {
						String valparts[] = val.split(",");
						for (String s : valparts) {
							set.add(s.substring(s.indexOf("[")+1,s.indexOf("]")));
						}
					}
				}
			}
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return  lms;
	}

	public static void main(String[] args) {
		RelaxedPlanningGraphGenerator rpgen= new RelaxedPlanningGraphGenerator();
		String inputfilerpg = "/home/sachini/domains/BLOCKS/scenarios/TEST0/inst1/scenarios/0/outs/attacker/rpg-problem_a";
		String inputfilecon = "/home/sachini/domains/BLOCKS/scenarios/TEST0/inst1/scenarios/0/outs/attacker/connectivity-problem_a";
		String lmoutput = "/home/sachini/domains/BLOCKS/scenarios/TEST0/inst1/scenarios/0/outs/attacker/verifiedlm.txt";
		ArrayList<String> goal = new ArrayList<String>();
		ArrayList<String> init = new ArrayList<String>();
		ArrayList<String> currentstate = new ArrayList<String>();

		goal.add("(ON P A)");
		goal.add("(ON A C)");//attacker PAC, user PAL

		init.add("(HANDEMPTY)");
		init.add("(CLEAR P)");
		init.add("(CLEAR A)");
		init.add("(CLEAR L)");
		init.add("(CLEAR C)");
		init.add("(ONTABLE A)");
		init.add("(ONTABLE P)");
		init.add("(ONTABLE L)");
		init.add("(ONTABLE C)");

		currentstate.add("(HANDEMPTY)");
		currentstate.add("(CLEAR P)");
		currentstate.add("(CLEAR L)");
		currentstate.add("(CLEAR A)");
		currentstate.add("(ON A C)");
		currentstate.add("(ONTABLE P)");
		currentstate.add("(ONTABLE L)");
		currentstate.add("(ONTABLE C)");

		//		critical.add("(ON C A)"); //prob 30
		//		critical.add("(ON B D)");
		//		init.add("(HANDEMPTY)");
		//		init.add("(CLEAR A)");
		//		init.add("(ONTABLE A)");
		//		init.add("(CLEAR B)");
		//		init.add("(ONTABLE B)");
		//		init.add("(ONTABLE C)");
		//		init.add("(ON D C)");
		//		init.add("(CLEAR D)");

		//		critical.add("(AT D)"); //prob 31. Give these with paranthesis
		//		init.add("(AT A)");
		//		init.add("(ADJ A B)");
		//		init.add("(ADJ A E)");
		//		init.add("(ADJ B C)");
		//		init.add("(ADJ C D)");
		//		init.add("(ADJ A D)");
		rpgen.runLandmarkGenerator(inputfilerpg, inputfilecon, goal, init, lmoutput);
		rpgen.computeAchievedLandmarks(goal, lmoutput, currentstate);
	}

	public int computeAchievedLandmarks(ArrayList<String> goal, String lmout, ArrayList<String> currentstate) { //Karpaz,domshlak 2009 (cost optimal planning with landmarks)
		HashMap<String,TreeSet<String>> a_landmarks = readLandmarksGNOrders(lmout);
		HashMap<OrderedLMNode, Boolean> active = new HashMap<OrderedLMNode, Boolean>();
		OrderedLMGraph aGraph = new OrderedLMGraph();
		aGraph.produceOrders(a_landmarks, goal);
		//when counting landmarks, predicates that need to be together should be counted together. if not that landmark is not achieved
		//if i am seeing a higher-order predicate, also assume all predicates below it has been achieved. (ordered landmarks principle)
		HashMap<OrderedLMNode, TreeSet<OrderedLMNode>> orders = aGraph.getAdj();
		Iterator<OrderedLMNode> itr = orders.keySet().iterator();
		while(itr.hasNext()){
			OrderedLMNode key = itr.next();
			active.put(key, false);
		}
		//iterate over the current state. mark each predicate as active true/false
		Iterator<OrderedLMNode> acitr = active.keySet().iterator();
		while(acitr.hasNext()){
			OrderedLMNode key = acitr.next();
			for (String string : currentstate) {
				if(key.getNodecontent().contains(string)) {
					active.put(key, true);
				}
			}
		}
		System.out.println("active===="+active);
		System.out.println("gnorders====");
		Iterator<OrderedLMNode> itrac = aGraph.getAdj().keySet().iterator();
		while(itrac.hasNext()){
			OrderedLMNode lmn = itrac.next();
			System.out.println(lmn+"    :"+aGraph.getAdj().get(lmn));
		}
		return countAchievedLandmarks(aGraph, active);
	}

	private int countAchievedLandmarks(OrderedLMGraph aGraph, HashMap<OrderedLMNode, Boolean> active) {
		int achievedcount = 0;
		ArrayList<OrderedLMNode> subgoals = aGraph.findRoots();
		System.out.println("******"+subgoals);
		for (OrderedLMNode node : subgoals) {
			ArrayList<OrderedLMNode> siblings = aGraph.findAllSiblingsofNode(node);
			if(active.get(node)) {
				System.out.println("node = "+ node + "  |>> "+siblings);
			}

		}

		//this only counts the highest node in the tree. 
		//since landmarks are ordered, if higher node is true, all it's siblings must assume to be achieved
		return achievedcount;
	}

	public boolean nodeContainsPredicate(ArrayList<String> pred, OrderedLMNode node) {
		String ss = "";
		for (String s : node.getNodecontent()) {
			ss+=s;
		}
		for (String string : pred) {
			if(ss.contains(string) && !ss.contains(",")) {
				return true;
			}
		}	
		return false;
	}
}
