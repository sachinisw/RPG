package landmark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.TreeSet;

import con.ConnectivityGraph;

public class LandmarkExtractor {
	private RelaxedPlanningGraph RPG;
	private ConnectivityGraph con;

	public LandmarkExtractor(RelaxedPlanningGraph rpg, ConnectivityGraph c){
		this.RPG = rpg;
		this.con = c;
	}

	public LGG extractLandmarks(ArrayList<String> goalpredicates){
		System.out.println("extracting landmarks.............");
		ArrayList<String> lmCandidates = new ArrayList<String>();//C
		lmCandidates.addAll(goalpredicates);
		LGG lgg = new LGG();
		lgg.addLGGNode(goalpredicates);
		while(!lmCandidates.isEmpty()){
			TreeSet<String> cprime = new TreeSet<String>();
			System.err.println("LMCANDIDATES=="+ Arrays.toString(lmCandidates.toArray()));
			for (String lprime : lmCandidates) {
				int level = RPG.getLevelofEffect(lprime);
				System.err.println("---------------------current candidate---->"+ lprime + " level------ "+level);
				if(level>0){
					//level-1 doesn't produce proved landmarks. if this condition is ignored then the resulting LGG contains only (greedy) necessary landmarks.
					//to find greedy necessary, find the action that adds the predicate earliest in the graph. not only from the level before.
					GraphLevel glevel = RPG.getLevel(level-1); 
					ArrayList<String> acLevelBefore = glevel.getActionLayer();
					System.out.println("actions level before ====  "+ Arrays.toString(acLevelBefore.toArray()));
					TreeSet<String> A = new TreeSet<String>();
					for (String ac : acLevelBefore) {
						ArrayList<String> adds = con.findStatesAddedByAction(ac);
						System.out.println("ac="+ac + " adds="+Arrays.toString(adds.toArray()));
						if(listContainsPredicate(adds, lprime)){ //A{} contains actions from level before that adds current landmark
							A.add(ac);
						}
					}
					//					System.out.println("actions adding landmarks=====  "+ Arrays.toString(A.toArray()));
					//extract predicates that are preconditions to all actions in A{}
					//these are fact landmarks
					TreeSet<String> factlm = extractCommonPreconditions(A);
					ArrayList<String> lprimedata = new ArrayList<String>();
					lprimedata.add(lprime);
					System.out.println("common preconds =====  "+ Arrays.toString(factlm.toArray()));
					for (String fact : factlm) {
						System.out.println("factlm = "+ fact);
						ArrayList<String> data = new ArrayList<String>();
						data.add(fact);
						if(lgg.findLGGNode(data) == null){
							cprime.add(fact);
							lgg.addLGGNode(data);
							System.out.println("before\n"+lgg.toString());
						}
						lgg.addEdge(data, lprimedata);//fact -> lprime edge to lgg
						System.out.println("after\n"+lgg.toString());
					}
				}
			}
			lmCandidates.clear();
			lmCandidates.addAll(cprime);
		}
		System.out.println("final======   \n"+lgg);
		return lgg;
	}

	//from initial state, see if using actions that does not add a landmark can get you to reach the goal
	public ArrayList<LGGNode> verifyLandmarks(LGG lgg, ArrayList<String> goalpredicates, ArrayList<String> init){
		System.out.println("UNVERIFIED   ??????????? \n"+lgg);
		ArrayList<LGGNode> vlm = new ArrayList<LGGNode>();
		ArrayList<LGGNode> lmCandidates = new ArrayList<LGGNode>();
		Iterator<LGGNode> itr = lgg.getAdjacencyList().keySet().iterator();
		while(itr.hasNext()){
			lmCandidates.add(itr.next());
		}
		for (LGGNode node : lmCandidates) {
			System.out.println("current node---"+ node); 
			boolean isGoal = false, isInit = false, isVerifiedCandidate = false;
			if(node.containsState(init)){ //initial state is a trivial landmark
				System.out.println("init state");
				isInit = true;
			}else if(node.containsState(goalpredicates)){ //goals are trivial landmarks
				System.out.println("goal state");
				isGoal = true;
			}else if(!goalReachableWithoutLandmark(node, goalpredicates, init)){ //goal can't be reached without this landmark
				isVerifiedCandidate = true;
			}
			if(isGoal || isInit || isVerifiedCandidate){
				vlm.add(node);
			}
		}
		System.out.println("VERIFIED  ***************\n");
		for (LGGNode lggNode : vlm) {
			System.out.println(lggNode);
		}
		return vlm; //when implemented make this method return verified landmark set. //TODO:: REMOVE unverified lm from adjacency list
	}
	
	//build the relaxed plan graph layer by layer in the loop. if next state == cur state then stop. not solvable.
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
//			System.out.println("available actions = "+ available);System.out.println("current state = "+ curStates);
			nextStates.addAll(curStates);
			for (String ac : available) {
				nextStates.addAll(con.findStatesAddedByAction(ac));
			}
//			System.out.println("next state  = "+ nextStates);
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
//			System.out.println(temp_rpg.toString());
			if(equalLists(nxt, cur)){
				break; //state doesn't change from now to next. stop building here.
			}
		}
//		System.out.println(lm + "=======================================================>" + temp_rpg.containsGoal(goals));
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
}
