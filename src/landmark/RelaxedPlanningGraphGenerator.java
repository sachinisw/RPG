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
		String inputfilerpg = "/home/sachini/domains/BLOCKS/scenarios/test1/rpg";
		String inputfilecon = "/home/sachini/domains/BLOCKS/scenarios/test1/con";
		String lmoutput = "/home/sachini/domains/BLOCKS/scenarios/test1/verifiedlm.txt";
		ArrayList<String> goal = new ArrayList<String>();
		ArrayList<String> init = new ArrayList<String>();
		ArrayList<String> currentstate = new ArrayList<String>();

		goal.add("(CLEAR R)");
		goal.add("(ON E D)");//attacker PAC, user PAL
		goal.add("(ON R E)");
		goal.add("(ONTABLE D)");
		//		init.add("(HANDEMPTY)");
		//		init.add("(CLEAR P)");
		//		init.add("(CLEAR A)");
		//		init.add("(CLEAR L)");
		//		init.add("(CLEAR C)");
		//		init.add("(ONTABLE A)");
		//		init.add("(ONTABLE P)");
		//		init.add("(ONTABLE L)");
		//		init.add("(ONTABLE C)");

		init.add("(CLEAR S)");
		init.add("(CLEAR R)");
		init.add("(CLEAR D)");
		init.add("(CLEAR E)");
		init.add("(HANDEMPTY)");
		init.add("(ONTABLE S)");
		init.add("(ONTABLE R)");
		init.add("(ONTABLE B)");
		init.add("(ONTABLE A)");
		init.add("(ON D B)");
		init.add("(ON E A)");
				//		currentstate.add("(HANDEMPTY)");
				//		currentstate.add("(CLEAR P)");
				//		currentstate.add("(CLEAR L)");
				//		currentstate.add("(CLEAR A)");
				//		currentstate.add("(ON A C)");
				//		currentstate.add("(ONTABLE P)");
				//		currentstate.add("(ONTABLE L)");
				//		currentstate.add("(ONTABLE C)");

//				currentstate.add("(HOLDING A)");
//				currentstate.add("(CLEAR P)");
//				currentstate.add("(CLEAR L)");
//				currentstate.add("(CLEAR C)");
//				currentstate.add("(ONTABLE P)");
//				currentstate.add("(ONTABLE L)");
//				currentstate.add("(ONTABLE C)");

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
		currentstate.add("(CLEAR S)");
		currentstate.add("(CLEAR R)");
		currentstate.add("(CLEAR E)");
		currentstate.add("(CLEAR A)");
		currentstate.add("(HANDEMPTY)");
		currentstate.add("(ONTABLE S)");
		currentstate.add("(ONTABLE R)");
		currentstate.add("(ONTABLE B)");
		currentstate.add("(ONTABLE A)");
		currentstate.add("(ON E D)");
		currentstate.add("(ON D B)");

				rpgen.runLandmarkGenerator(inputfilerpg, inputfilecon, goal, init, lmoutput);
				double x = rpgen.computeAchievedLandmarks(goal, lmoutput, currentstate);
				System.out.println(x);
	}

	//TODO: Karpaz,domshlak 2009 (cost optimal planning with landmarks)
	//TODO: bryce 2014 (landmark distance metric). awful paper to understand :-(
	//Landmark based goal completion heuristic (Meneguzzi 2017)
	public double computeAchievedLandmarks(ArrayList<String> goal, String lmout, ArrayList<String> currentstate) { 
		HashMap<String,TreeSet<String>> a_landmarks = readLandmarksGNOrders(lmout);
		HashMap<OrderedLMNode, Boolean> active = new HashMap<OrderedLMNode, Boolean>();
		OrderedLMGraph aGraph = new OrderedLMGraph(goal);
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
		}//iterate over active nodes and find each node's all siblings. mark them as active.
		//since landmarks are ordered, if higher node is true, all it's siblings must assume to be achieved
		Iterator<OrderedLMNode> ac = active.keySet().iterator();
		ArrayList<OrderedLMNode> allTrue = new ArrayList<OrderedLMNode>();
		HashMap<OrderedLMNode, ArrayList<OrderedLMNode>> nodesiblngs = new HashMap<OrderedLMNode, ArrayList<OrderedLMNode>>();
		while(ac.hasNext()){
			OrderedLMNode key = ac.next();
			ArrayList<OrderedLMNode> sib = aGraph.findAllSiblingsofNode(key);
			if(active.get(key)) {
				allTrue.addAll(sib);
			}
			nodesiblngs.put(key,sib);
		}//mark everything in allTrue as active
		Iterator<OrderedLMNode> acTrue = active.keySet().iterator();
		while(acTrue.hasNext()){
			OrderedLMNode key = acTrue.next();
			if(allTrue.contains(key)) {
				active.put(key, true);
			}
		}//mark everything in allTrue as active
		aGraph.assignSiblingLevels(); //ok
		HashMap<OrderedLMNode, ArrayList<ArrayList<OrderedLMNode>>> subgoallevels = aGraph.getLevelsPerSubgoal();
		//		System.out.println("active======="+active);
		HashMap<OrderedLMNode, Integer> lmCompleteLevels = getMaxCompleteLevel(subgoallevels,active);
		HashMap<OrderedLMNode, Integer> subGoalLevels = getSubgoalLevels(subgoallevels); //ok
		return computeLandmarkCompletionHeuristic(lmCompleteLevels, subGoalLevels);
	}

	public double computeLandmarkCompletionHeuristic(HashMap<OrderedLMNode, Integer> lmcompletelevels, HashMap<OrderedLMNode, Integer> subgoallevels) {
		double numofsubgoals = subgoallevels.size();
		double sum = 0.0;
		System.out.println(lmcompletelevels);
		System.out.println(subgoallevels);
		Iterator<OrderedLMNode> itr = subgoallevels.keySet().iterator();
		while(itr.hasNext()) {
			OrderedLMNode subgoal = itr.next();
			System.out.println(subgoal);
			double complete = lmcompletelevels.get(subgoal);
			double levels = subgoallevels.get(subgoal);
			System.out.println(complete + "|" + levels);
			sum += (levels-complete)/levels;
		}
		return sum/numofsubgoals;
	}

	public HashMap<OrderedLMNode, Integer> getMaxCompleteLevel(HashMap<OrderedLMNode, ArrayList<ArrayList<OrderedLMNode>>> subgoallevels, 
			HashMap<OrderedLMNode, Boolean> active) {
//		System.out.println("active====="+active);
//		HashMap<OrderedLMNode, Integer> lmCompletedLevel = new HashMap<>();
//		Iterator<OrderedLMNode> itr = subgoallevels.keySet().iterator();
//		while(itr.hasNext()) {
//			OrderedLMNode key = itr.next();
//			ArrayList<ArrayList<OrderedLMNode>> levels = subgoallevels.get(key);
//			Iterator<OrderedLMNode> itrac = active.keySet().iterator();
//			OrderedLMNode minimumActive = null;
//			int l = Integer.MAX_VALUE;
//			while(itrac.hasNext()) { //find node at the highest point (level~0) in tree which is active 
//				OrderedLMNode keyac = itrac.next();
//				if(active.get(keyac)) {
//					if(keyac.getTreeLevel()<l && l>=0) {
//						l = keyac.getTreeLevel();
//						minimumActive = keyac;
//					}
//				}
//			}
//			System.out.println("MIN ACTIVE"+minimumActive);
//			if(l==0 && key.equals(minimumActive)) {
//				lmCompletedLevel.put(key, levels.size()+1);
//			}else {
//				int completeLevel = levels.size()-1;
//				for (int i=levels.size()-1; i>=0; i--) {
//					ArrayList<OrderedLMNode> level = levels.get(i);
//					int curlevelcompletenodes = 0;
//					for (OrderedLMNode node : level) {
//						if(active.get(node)) {
//							++curlevelcompletenodes;
//						}
//					}
//					if(curlevelcompletenodes==level.size()) { //if completelevel=0 that means the tree is fully done. complete level=1 means all but root is complete.
//						completeLevel = i+1;//i+1 subgoallevels structure doesn't have the 0th (goal) level.
//					}//half complete. if so, check if upper level is active. if yes, the upper level becomes the complete level
//				}
//				lmCompletedLevel.put(key, completeLevel);
//			}
//		}
//		System.out.println("***********************");
//		System.out.println(lmCompletedLevel);
//		return lmCompletedLevel;
		System.out.println(subgoallevels);
		HashMap<OrderedLMNode, Integer> lmCompletedLevel = new HashMap<>(); //contains the highest fully complete tree level 
		Iterator<OrderedLMNode> itr = subgoallevels.keySet().iterator();
		while(itr.hasNext()) {
			OrderedLMNode key = itr.next();
			ArrayList<ArrayList<OrderedLMNode>> levels = subgoallevels.get(key);
			System.out.println("current subgoal=="+key);
			if(active.get(key)) { //subgoal root is active. assume everything below is also active. why? Landmarks are partial orders
				System.out.println("goal complete");
				lmCompletedLevel.put(key, 0);
			}else { //subgoal tree is half complete. first find that half complete level. go from top to bottom in the current subtree
				System.out.println("half tree complete");
				if(levels.size()>0) { //subtree has a few levels. one of these levels is half complete
					int completeLevel =  levels.size()-1;
					for (int i=levels.size()-1; i>=0; i--) { //move from bottom up. when this loop finishes, I will have the highest level with all active nodes
						ArrayList<OrderedLMNode> level = levels.get(i);
						int curlevelcompletenodes = 0;
						for (OrderedLMNode node : level) {
							if(active.get(node)) {
								curlevelcompletenodes++;
							}
						}
						System.out.println("curlevelcompletenodes="+curlevelcompletenodes);
						if(curlevelcompletenodes==level.size()) { //if completelevel=0 that means the tree is fully done. complete level=1 means all but root is complete.
							System.out.println("current level is fully complete ");
							completeLevel = i;
						}
//						else { //half complete level. if so, check if upper level (key) is active. 
//							System.out.println("half complete level");
//							if(active.get(key)) { //if yes, the upper level becomes the complete level
//								completeLevel = key.getTreeLevel()+1;
//							}else { //half complete also parent is not active. (-1)
//								completeLevel = -1;
//							}
//						}
					}
					lmCompletedLevel.put(key, completeLevel+1); //i+1 subgoallevels structure doesn't have the 0th (goal) level.
				}else { //this subgoal doesn't have children
					System.out.println("no children subgoal");
					if(active.get(key)) {
						lmCompletedLevel.put(key, 0); 
					}else {
						lmCompletedLevel.put(key, -1); 
					}
				}
			}
			System.out.println("......."+lmCompletedLevel);
		}
		return lmCompletedLevel;
	}

	public HashMap<OrderedLMNode, Integer> getSubgoalLevels(HashMap<OrderedLMNode, ArrayList<ArrayList<OrderedLMNode>>> subgoallevels) {
		HashMap<OrderedLMNode, Integer> lmLevels = new HashMap<>();
		Iterator<OrderedLMNode> itr = subgoallevels.keySet().iterator();
		while(itr.hasNext()) {
			OrderedLMNode key = itr.next();
			ArrayList<ArrayList<OrderedLMNode>> levels = subgoallevels.get(key);
			if(!levels.isEmpty()) {
				lmLevels.put(key, levels.size()+1);
			}else {
				lmLevels.put(key, 1);

			}
		}
		return lmLevels;
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
