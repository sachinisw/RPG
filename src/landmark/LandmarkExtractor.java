package landmark;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeSet;

import con.ConnectivityGraph;

public class LandmarkExtractor {
	private RelaxedPlanningGraph RPG;
	private ConnectivityGraph con;
	private HashMap<String, String> achievers;

	public LandmarkExtractor(RelaxedPlanningGraph rpg, ConnectivityGraph c){
		this.RPG = rpg;
		this.con = c;
		achievers = new HashMap<>();
	}

	public LGG extractLandmarks(ArrayList<String> goalpredicates){
		ArrayList<String> lmCandidates = new ArrayList<String>();//C
		lmCandidates.addAll(goalpredicates);
		LGG lgg = new LGG();
		lgg.addLGGNode(goalpredicates);
		while(!lmCandidates.isEmpty()){
			TreeSet<String> cprime = new TreeSet<String>();
			for (String lprime : lmCandidates) {
				int level = RPG.getLevelofEffect(lprime);
				if(level>0){ //this version picks the actions that adds the predicate from level before . method used in landmark heuristic meneguzzi paper.
					int earliestLevel = level-1; //level-1 doesn't produce proved landmarks. if this condition is ignored then the resulting LGG contains only (greedy) necessary landmarks.
//					for (int i=level-1; i>=0; i--) {//to find greedy necessary uncomment this loop. It finds the action that adds the predicate earliest in the graph. not only from the level before.
////						GraphLevel glevel = RPG.getLevel(i);
////						ArrayList<String> acLevelBefore = glevel.getActionLayer();
//						for (String ac : acLevelBefore) {
//							ArrayList<String> adds = con.findStatesAddedByAction(ac);
//							if(listContainsPredicate(adds, lprime)){
//								if(i<earliestLevel) {
//									earliestLevel = i;
//								}
//							}
//						}
//					}
					GraphLevel glevel = RPG.getLevel(earliestLevel); 
					ArrayList<String> acLevelBefore = glevel.getActionLayer();
					//										System.out.println("actions level before ====  "+ Arrays.toString(acLevelBefore.toArray()));
					TreeSet<String> A = new TreeSet<String>();
					for (String ac : acLevelBefore) {
						ArrayList<String> adds = con.findStatesAddedByAction(ac);
															//												System.out.println("ac="+ac + " adds="+Arrays.toString(adds.toArray()));
						if(listContainsPredicate(adds, lprime)){ //A{} contains actions from level before that adds current landmark
							A.add(ac);
						}
					}
															//										System.out.println("actions adding landmarks=====  "+ Arrays.toString(A.toArray()));
					//extract predicates that are preconditions to all actions in A{}
					//these are fact landmarks
					TreeSet<String> factlm = extractCommonPreconditions(A);
					ArrayList<String> lprimedata = new ArrayList<String>();
					lprimedata.add(lprime);
															//										System.out.println("common preconds =====  "+ Arrays.toString(factlm.toArray()));
					achievers.put(Arrays.toString(A.toArray()), Arrays.toString(factlm.toArray()));
					for (String fact : factlm) {
															//												System.out.println("factlm = "+ fact);
						ArrayList<String> data = new ArrayList<String>();
						data.add(fact);
						if(lgg.findLGGNode(data) == null){
							cprime.add(fact);
							lgg.addLGGNode(data);
															//														System.out.println("before\n"+lgg.toString());
						}
						LGGNode from = new LGGNode(data);
						LGGNode to = new LGGNode(lprimedata);
						if(lgg.findEdge(from, to)==null){
							lgg.addEdge(data, lprimedata);//fact -> lprime edge to lgg
						}
						
															//														System.out.println("after\n"+lgg.toString());
					}
				}
			}
			lmCandidates.clear();
			lmCandidates.addAll(cprime);
		}
//																			System.out.println("final======   \n"+lgg);
//																			for (LGGEdge e : lgg.getEdges()) {
//																				System.out.println(e.toString());
//																			}
		return lgg;
	}

	//from initial state, see if using actions that does not add a landmark can get you to reach the goal
	public ArrayList<LGGNode> verifyLandmarks(LGG lgg, ArrayList<String> goalpredicates, ArrayList<String> init, String lmoutputfilepath){
		ArrayList<LGGNode> vlm = new ArrayList<LGGNode>();
		ArrayList<LGGNode> lmCandidates = new ArrayList<LGGNode>();
		Iterator<LGGNode> itr = lgg.getAdjacencyList().keySet().iterator();
		while(itr.hasNext()){
			lmCandidates.add(itr.next());
		}
		for (LGGNode node : lmCandidates) {
			boolean isGoal = false, isInit = false, isVerifiedCandidate = false;
			if(node.containsState(init)){ //initial state is a trivial landmark
				isInit = true;
			}else if(node.containsState(goalpredicates)){ //goals are trivial landmarks
				isGoal = true;
			}else if(!goalReachableWithoutLandmark(node, goalpredicates, init)){ //goal can't be reached without this landmark
				isVerifiedCandidate = true;
			}
			if(isGoal || isInit || isVerifiedCandidate){
				vlm.add(node);
			}
		}
//																System.out.println("VERIFIED  ***************\n");
//																for (LGGNode lggNode : vlm) {
//																	System.out.println(lggNode);
//																}
		HashMap<LGGNode, TreeSet<LGGNode>> orders = removeUnverifiedLandmarksFromLGG(lgg, vlm); 
		writeLandmarksToFile(lgg, vlm, orders, lmoutputfilepath);
		return vlm; 
	}
	
	//Remove unverified lm from adjacency list of LGG and produce the greedy necessary orders
	public  HashMap<LGGNode, TreeSet<LGGNode>> removeUnverifiedLandmarksFromLGG(LGG lgg, ArrayList<LGGNode> verified){
		ArrayList<LGGNode> toRemove = new ArrayList<>();
		Iterator<Entry<LGGNode, TreeSet<LGGNode>>> itr = lgg.getAdjacencyList().entrySet().iterator();
		while(itr.hasNext()){
			LGGNode n = itr.next().getKey();
			boolean found = false;
			for (LGGNode lggNode : verified) { //find stuff to remove
				if(n.equals(lggNode)){
					found = true;
				}
			}
			if(!found){
				toRemove.add(n);
			}
		}
		for (LGGNode n : toRemove) {//remove
			lgg.removeLGGNode(n);
		}
		return buildOrders(lgg);
//														System.out.println("CLEANED ADJ--------------");
//														System.out.println(lgg);
//														System.out.println(lgg.getEdges());
	}

	//merge edges to form a DAG indicating what LMs should be achieved together
	public HashMap<LGGNode, TreeSet<LGGNode>> buildOrders(LGG lgg) {
		HashMap<LGGNode, TreeSet<LGGNode>> groups = new HashMap<LGGNode, TreeSet<LGGNode>>();
		Iterator<Entry<LGGNode, TreeSet<LGGNode>>> itr = lgg.getAdjacencyList().entrySet().iterator();
		while(itr.hasNext()){
			groups.put(itr.next().getKey(), new TreeSet<LGGNode>());
		}
		Iterator<Entry<LGGNode,TreeSet<LGGNode>>> itrg = groups.entrySet().iterator();
		while(itrg.hasNext()) {
			Entry<LGGNode, TreeSet<LGGNode>> e = itrg.next();
			LGGNode cur = e.getKey();
			TreeSet<LGGNode> curval = e.getValue();
			for (LGGEdge edge : lgg.getEdges()) {
				if(edge.getTo().isEqual(cur)) {
					curval.add(edge.getFrom());
				}
			}
		}
		return groups;
	}
	
	//build the relaxed plan graph layer by layer in the loop. if next state == cur state then stop. not solvable.
	//algorithm in p.6 https://arxiv.org/pdf/1106.5271.pdf
	private boolean goalReachableWithoutLandmark(LGGNode lm, ArrayList<String> goals, ArrayList<String> inits){
		RelaxedPlanningGraph temp_rpg = new RelaxedPlanningGraph();
		int graphlevel = 0;
		TreeSet<String> curStates = new TreeSet<>();
		curStates.addAll(inits);
		while(!temp_rpg.containsGoal(goals)){ //build rpg
			TreeSet<String> nextStates  = new TreeSet<>();
			ArrayList<String> temp = new ArrayList<String>();
			temp.addAll(curStates);
			ArrayList<String> available = filterActionsNotAddingLandmark(lm, con.findApplicableActionsInState(temp));
			nextStates.addAll(curStates);
			for (String ac : available) {
				nextStates.addAll(con.findStatesAddedByAction(ac));
			}
			ArrayList<String> nxt = new ArrayList<String>();//temporary holding place for equalLists()
			ArrayList<String> cur = new ArrayList<String>();
			nxt.addAll(nextStates); cur.addAll(curStates);
			GraphLevel l =  new GraphLevel(); //add new layer to graph
			l.setPropositionLayer(cur);
			l.setActionLayer(available);
			l.setLevelType(String.valueOf(graphlevel));
			temp_rpg.setLevel(l);
			graphlevel++;
			curStates.clear();
			curStates.addAll(nextStates);
			if(equalLists(nxt, cur)){
				break; //state doesn't change from now to next. stop building here.
			}
		}//		System.out.println(temp_rpg.toString());
		return temp_rpg.containsGoal(goals); //if false, then goal cant be achieved without lm. lm is a verified landmark
	}

	private ArrayList<String> filterActionsNotAddingLandmark(LGGNode lm, ArrayList<String> actions){
		ArrayList<String> filter = new ArrayList<>();
		for (String ac : actions){
			ArrayList<String> adds = con.findStatesAddedByAction(ac);
			boolean found = false;
			for (String ad : adds) {
				ArrayList<String> lmVal = lm.getValue();
				for (String v : lmVal) {
					if(ad.equalsIgnoreCase(v)){
						found = true;
					}
				}
			}
			if(!found){
				filter.add(ac);
			}
		}
		return filter;
	}

	private boolean listContainsPredicate(ArrayList<String> predicates, String pred){
		for (String add : predicates) {
			if(add.equalsIgnoreCase(pred)){
				return true;
			}
		}
		return false;
	}

	private boolean equalLists(ArrayList<String> a, ArrayList<String> b){
		if(a==null && b==null){
			return true;
		}
		if((a!=null && b==null) || (a==null && b!=null) || (a.size()!=b.size())){
			return false;
		}
		Collections.sort(a);
		Collections.sort(b);
		return a.equals(b);
	}

	private TreeSet<String> extractCommonPreconditions(TreeSet<String> A){
		TreeSet<String> preconds = new TreeSet<>();
		TreeSet<String> commons = new TreeSet<>();
		for (String ac : A) {
			preconds.addAll(con.findPreconditionsofAction(ac));
		}
		for (String cond : preconds) {
			boolean found = true;
			for (String ac : A) {
				ArrayList<String> pre = con.findPreconditionsofAction(ac);
				if(!pre.contains(cond)){
					found = false;
				}
			}
			if(found){
				commons.add(cond);
			}
		}
		return commons;
	}

	private void writeLandmarksToFile(LGG lgg, ArrayList<LGGNode>vlm, HashMap<LGGNode, TreeSet<LGGNode>> order, String lmoutputfilepath){
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(lmoutputfilepath, "UTF-8");
			writer.write(":LGG VERIFIED"+"\n");
			writer.write(lgg.toString()+"\n");
			writer.write(":VERIFIED LM\n");
			String s = "";
			for (LGGNode lggNode : vlm) {
				s+=lggNode.toString()+"\n";
			}
			writer.write(s+"\n");
			writer.write(":LGG GREEDY NECESSARY ORDERS"+"\n");
			String gn = "";
			Iterator<Entry<LGGNode, TreeSet<LGGNode>>> itr=  order.entrySet().iterator();
			while(itr.hasNext()){
				Entry<LGGNode, TreeSet<LGGNode>> e = itr.next();
				gn+=e.getKey()+": "+e.getValue()+"\n";
			}
			writer.write(gn+"\n");
			writer.write(":LGG UNVERIFIED ACHIEVERS"+"\n");
			Iterator<Entry<String, String>> its=  achievers.entrySet().iterator();
			while(its.hasNext()){
				Entry<String, String> e = its.next();
				writer.write(e.getKey()+"\t->"+e.getValue()+"\n");
			}
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally{
			writer.close();
		}
	}
	
	public RelaxedPlanningGraph getRPG() {
		return RPG;
	}

	public void setRPG(RelaxedPlanningGraph rPG) {
		RPG = rPG;
	}

	public ConnectivityGraph getCon() {
		return con;
	}

	public void setCon(ConnectivityGraph con) {
		this.con = con;
	}

	public HashMap<String, String> getAchievers() {
		return achievers;
	}

	public void setAchievers(HashMap<String, String> achievers) {
		this.achievers = achievers;
	}
}
