package landmark;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Queue;
import java.util.TreeSet;

public class OrderedLMGraph {

	private HashMap<OrderedLMNode, TreeSet<OrderedLMNode>> adj; //child --> {parent1, parent2...}
	private ArrayList<String> goal;
	
	public OrderedLMGraph(ArrayList<String> g) {
		adj = new HashMap<OrderedLMNode, TreeSet<OrderedLMNode>>();
		goal = g;
	}

	public void produceOrders(HashMap<String,TreeSet<String>> lms, ArrayList<String> goal){
		ArrayList<OrderedLMEdge> orders = new ArrayList<>();
		ArrayList<OrderedLMNode> nodes = new ArrayList<>();
		Iterator<String> itr = lms.keySet().iterator();
		while(itr.hasNext()){ //add all nodes (key) in lms
			nodes.add(new OrderedLMNode(itr.next()));
		}
		Iterator<String> itr2 = lms.keySet().iterator();
		while(itr2.hasNext()){
			String key = itr2.next();
			TreeSet<String> val = lms.get(key);
			OrderedLMNode before = null, after = null;
			after = nodes.get(nodes.indexOf(new OrderedLMNode(key)));
			for (String s : val) {
				before = nodes.get(nodes.indexOf(new OrderedLMNode(s)));
				orders.add(new OrderedLMEdge(before, after));
			}
		}
		produceOrderdLMGraph(nodes, orders);
	}

	public HashMap<OrderedLMNode, TreeSet<OrderedLMNode>> produceOrderdLMGraph(ArrayList<OrderedLMNode> nodes, ArrayList<OrderedLMEdge> orders) {
		for (OrderedLMNode n : nodes) {
			if(!adj.containsKey(n)) {
				adj.put(n, new TreeSet<>());
			}
		}
		for (OrderedLMEdge e : orders) {
			if(adj.containsKey(e.getBefore())) {
				TreeSet<OrderedLMNode> set = adj.get(e.getBefore());
				set.add(e.getAfter());
			}
		}
		return adj;
	}

	public ArrayList<OrderedLMNode> findRoots(){
		ArrayList<OrderedLMNode> last = new ArrayList<OrderedLMNode>();
		for (String g : goal) {
			OrderedLMNode lmn = new OrderedLMNode(g);
			Iterator<OrderedLMNode> itr = adj.keySet().iterator();
			while(itr.hasNext()){
				OrderedLMNode key = itr.next();
				if(key.equals(lmn)) {
					last.add(lmn);
				}
			}
		}
		return last;
	}

	public ArrayList<OrderedLMNode> findAllSiblingsofNode(OrderedLMNode node){
		ArrayList<OrderedLMNode> siblings = new ArrayList<OrderedLMNode>();
		Queue<OrderedLMNode> queue = new ArrayDeque<OrderedLMNode>();
		queue.add(node);
		recursivelyFindAllSiblings(queue, siblings);
		return siblings;
	}

	private void recursivelyFindAllSiblings(Queue<OrderedLMNode> queue, ArrayList<OrderedLMNode> siblings) {
		if(queue.isEmpty()) {
			return;
		}
		OrderedLMNode current = queue.poll();
		Iterator<OrderedLMNode> itr = adj.keySet().iterator();
		while(itr.hasNext()) {
			OrderedLMNode key = itr.next();
			TreeSet<OrderedLMNode> parents = adj.get(key);
			if(parents.contains(current)) {
				siblings.add(key);
				queue.add(key);
			}
		}
		recursivelyFindAllSiblings(queue, siblings);
	}

	public void assignSiblingLevels() {
		ArrayList<OrderedLMNode> roots = findRoots();
		for (OrderedLMNode r : roots) { //put roots at level 0;
			r.setTreeLevel(0);
		}
		//iterative BFS to assign level numbers for each root.
		Queue<OrderedLMNode> queue = new ArrayDeque<OrderedLMNode>();
		ArrayList<OrderedLMNode> explored = new ArrayList<OrderedLMNode>();
		queue.addAll(roots);
		while(!queue.isEmpty()) {
			OrderedLMNode cur = queue.poll();
			ArrayList<OrderedLMNode> children = findImmediateChildrenofNode(cur);
			for (OrderedLMNode child : children) {
				if(!explored.contains(child)) {
					child.setTreeLevel(cur.getTreeLevel()+1);
				}
				queue.add(child);
			}
			explored.add(cur);
		}
	}

	public HashMap<OrderedLMNode, ArrayList<ArrayList<OrderedLMNode>>> getLevelsPerSubgoal() {
		ArrayList<OrderedLMNode> roots = findRoots();
		HashMap<OrderedLMNode, ArrayList<ArrayList<OrderedLMNode>>> subgoallevels = new HashMap<>();
		for (OrderedLMNode r : roots) {
			Queue<OrderedLMNode> queue = new ArrayDeque<OrderedLMNode>();
			queue.add(r);
			ArrayList<OrderedLMNode> explored = new ArrayList<OrderedLMNode>();
			ArrayList<ArrayList<OrderedLMNode>> levels = new ArrayList<>();
			while(!queue.isEmpty()) {
				OrderedLMNode cur = queue.poll();
				ArrayList<OrderedLMNode> children = findImmediateChildrenofNode(cur);
				if(!children.isEmpty()) {
					ArrayList<OrderedLMNode> l = new ArrayList<>(children);
					levels.add(l);
				}
				for (OrderedLMNode child : children) {
					if(!explored.contains(child)) {
						queue.add(child);
					}
				}
				explored.add(cur);
			}
			subgoallevels.put(r,levels); //goal1 == [level 1, level2...]
		}
//		Iterator<OrderedLMNode> itr = subgoallevels.keySet().iterator();
//		while(itr.hasNext()) {
//			OrderedLMNode key = itr.next();
//			ArrayList<ArrayList<OrderedLMNode>> levels = subgoallevels.get(key);
//			System.out.println(key);
//			for (ArrayList<OrderedLMNode> level : levels) {
//				System.out.println(level);
//				System.out.println();
//			}
//			System.out.println("-------------------------------------------");
//		}
		return subgoallevels;
	}

	public ArrayList<OrderedLMNode> findImmediateChildrenofNode(OrderedLMNode node){
		ArrayList<OrderedLMNode> immediate = new ArrayList<OrderedLMNode>();
		Iterator<OrderedLMNode> itr = adj.keySet().iterator();
		while(itr.hasNext()) {
			OrderedLMNode key = itr.next();
			TreeSet<OrderedLMNode> parents = adj.get(key);
			if(parents.contains(node)) {
				immediate.add(key);
			}
		}
		return immediate;
	}

	public HashMap<OrderedLMNode, TreeSet<OrderedLMNode>> getAdj() {
		return adj;
	}

	public void setAdj(HashMap<OrderedLMNode, TreeSet<OrderedLMNode>> adj) {
		this.adj = adj;
	}

	public ArrayList<String> getGoal() {
		return goal;
	}

	public void setGoal(ArrayList<String> goal) {
		this.goal = goal;
	}
}
