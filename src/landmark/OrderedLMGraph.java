package landmark;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Queue;
import java.util.TreeSet;

public class OrderedLMGraph {
	
	private HashMap<OrderedLMNode, TreeSet<OrderedLMNode>> adj;
	
	public OrderedLMGraph() {
		adj = new HashMap<OrderedLMNode, TreeSet<OrderedLMNode>>();
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
		Iterator<OrderedLMNode> itr = adj.keySet().iterator();
		while(itr.hasNext()){
			OrderedLMNode key = itr.next();
			if(adj.get(key).isEmpty()) {
				last.add(key);
			}
		}
		return last;
	}

	public ArrayList<OrderedLMNode> findAllSiblingsofNode(OrderedLMNode node){
		ArrayList<OrderedLMNode> siblings = new ArrayList<OrderedLMNode>();
		Queue<OrderedLMNode> queue = new ArrayDeque<OrderedLMNode>();
		queue.add(node);
		recursivelyFindSiblings(queue, siblings);
		return siblings;
	}

	private void recursivelyFindSiblings(Queue<OrderedLMNode> queue, ArrayList<OrderedLMNode> siblings) {
		if(queue.isEmpty()) {
			return;
		}
		OrderedLMNode current = queue.poll();
		for (OrderedLMNode child : adj.get(current)) {
			if(!siblings.contains(child)) {
				siblings.add(child);
				queue.add(child);
			}
	        recursivelyFindSiblings(queue, siblings);
		}
	}
	
	public HashMap<OrderedLMNode, TreeSet<OrderedLMNode>> getAdj() {
		return adj;
	}

	public void setAdj(HashMap<OrderedLMNode, TreeSet<OrderedLMNode>> adj) {
		this.adj = adj;
	}
}
