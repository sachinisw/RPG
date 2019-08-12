package causality;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Queue;
import java.util.TreeSet;

import metrics.CausalLink;

/**
 * Directed Acyclic Graph of causal links for the undesirable reference plan
 * @author sachini
 *
 */
public class CausalGraph {
	private HashMap<CGNode, TreeSet<CGNode>> adjacencyList;
	private ArrayList<CGNode> vertices; 
	private ArrayList<CGEdge> edges;
	private ArrayList<String> currentState;
	private int numVertices;
	private int numEdges;

	public CausalGraph(ArrayList<String> current){
		adjacencyList = new HashMap<>();
		vertices = new ArrayList<>();
		edges = new ArrayList<>();
		currentState = current;
		numVertices = 0;
		numEdges = 0;
	}

	public void generateCausalGraph(ArrayList<CausalLink> cl) {
		System.out.println("Causal LInks ====" + cl);
		for (CausalLink c : cl) {
			ArrayList<String> add = new ArrayList<String>();
			ArrayList<String> need = new ArrayList<String>();
			add.add(c.getActionAdding());
			need.add(c.getActionNeeding());
			CGNode nodeF = new CGNode(add);
			CGNode nodeT = new CGNode(need);
			CGEdge edge = new CGEdge(nodeF, nodeT);
			if(!vertices.contains(nodeF)) {
				vertices.add(nodeF);
			}else if(!vertices.contains(nodeT)) {
				vertices.add(nodeT);
			}
			if(!edges.contains(edge)) {
				edges.add(edge);
			}
		}
		for (CGEdge e : edges) {
			if(!adjacencyList.containsKey(e.getFrom())) {
				TreeSet<CGNode> nbr = new TreeSet<>();
				nbr.add(e.getTo());
				adjacencyList.put(e.getFrom(), nbr);
			}else {
				TreeSet<CGNode> nbr = adjacencyList.get(e.getFrom());
				nbr.add(e.getTo());
				adjacencyList.put(e.getFrom(),nbr);
			}
		}
	}

	public CGNode getRoot() {
		Iterator<CGNode> itr = adjacencyList.keySet().iterator();
		while(itr.hasNext()){
			CGNode key = itr.next();
			ArrayList<String> data = new ArrayList<String>();
			data.add("A_I");
			if(key.containsState(data)) {
				return key;
			}
		}
		return null;
	}

	//from this observation, what actions get enabled? (i.e. preconditions gets added)
	//search through causal graph using BFS to find node corresponding to observation. Edge obs-neighbor will have enablers
	public void findEnablers(String observation) {
		ArrayList<String> data = new ArrayList<String>();
		data.add(observation);
		CGNode find = new CGNode(data);
		Queue<CGNode> queue = new ArrayDeque<CGNode>();
		ArrayList<CGNode> explored = new ArrayList<CGNode>();
		ArrayList<CGEdge> enablers = new ArrayList<CGEdge>();
		queue.add(getRoot());
		while(!queue.isEmpty()) {
			CGNode cur = queue.poll();
			TreeSet<CGNode> children = adjacencyList.get(cur);
			if(cur.isEqual(find)) {
				if(children!=null) {
					for (CGNode cgNode : children) {
						ArrayList<CGEdge> edges = findEdges(find, cgNode);
						for (CGEdge e : edges) {
							enablers.add(e);
						}
					}
				}
				break;
			}
			if(children!=null) {
				for (CGNode child : children) {
					if(!explored.contains(child)) {
						queue.add(child);
					}
				}
			}
			explored.add(cur);
		}
		System.out.println("------------------------");
		System.out.println("ob="+observation);
		System.out.println(adjacencyList);
		System.out.println("enablers= "+enablers);
	}

	//from my current observation find how it leads to goal state
	//do dfs from current observation.
	public void findLongTermEnablers(String observation) {
		ArrayList<String> data = new ArrayList<String>();
		data.add(observation);
		ArrayList<String> g = new ArrayList<>();
		g.add("A_G");
		CGNode find = new CGNode(data);
		CGNode goal = new CGNode(g);
		ArrayList<ArrayList<CGNode>> paths = new ArrayList<>();
		ArrayList<CGNode> currentpath = new ArrayList<>();
		ArrayList<CGNode> visited = new ArrayList<CGNode>();
		allPathsUtil(find, goal, visited, currentpath, paths);
		System.out.println("long term >>>>= "+ paths);
	}

	public void allPathsUtil(CGNode s, CGNode d, ArrayList<CGNode> visited, 
			ArrayList<CGNode> currentpath, ArrayList<ArrayList<CGNode>> allpaths) {
		visited.add(s);
		currentpath.add(s);
		if(s.equals(d)) {
			ArrayList<CGNode> copy = new ArrayList<>();
			for (CGNode cgNode : currentpath) {
				try {
					copy.add((CGNode) cgNode.clone());
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
			}
			allpaths.add(copy);
		}else {
			TreeSet<CGNode> nbrs = adjacencyList.get(s);
			if(nbrs!=null) {
				for (CGNode c : adjacencyList.get(s)) {
					if(!visited.contains(c)) {
						allPathsUtil(c,d,visited,currentpath,allpaths);
					}
				}
			}
		}
		currentpath.remove(s);
		visited.remove(s);
	}

	public void findActiveSatisfiers() {

	}

	public ArrayList<CGEdge> findSatisfiersOfUndesirableState() {
		ArrayList<CGEdge> satisfiers = new ArrayList<>();
		ArrayList<String> searchdata = new ArrayList<>();
		searchdata.add("A_G");
		for (CGEdge e : edges) {
			if(e.getTo().containsState(searchdata)) {
				satisfiers.add(e);
			}
		}
		System.out.println("satisifiers of undesirable= "+satisfiers);
		return satisfiers;
	}

	public ArrayList<CGEdge> findEdges(CGNode nodefrom, CGNode nodeTo){
		ArrayList<CGEdge> edgeset = new ArrayList<CGEdge>();
		CGEdge find = new CGEdge(nodefrom, nodeTo);
		for (CGEdge cgEdge : edges) {
			if(cgEdge.equals(find)) {
				edgeset.add(cgEdge);
			}
		}
		return edgeset;
	}

	public HashMap<CGNode, TreeSet<CGNode>> getAdjacencyList() {
		return adjacencyList;
	}

	public void setAdjacencyList(HashMap<CGNode, TreeSet<CGNode>> adjacencyList) {
		this.adjacencyList = adjacencyList;
	}

	public ArrayList<CGNode> getVertices() {
		return vertices;
	}

	public void setVertices(ArrayList<CGNode> vertices) {
		this.vertices = vertices;
	}

	public int getNumVertices() {
		return numVertices;
	}

	public void setNumVertices(int numVertices) {
		this.numVertices = numVertices;
	}

	public ArrayList<CGEdge> getEdges() {
		return edges;
	}

	public void setEdges(ArrayList<CGEdge> edges) {
		this.edges = edges;
	}

	public int getNumEdges() {
		return numEdges;
	}

	public void setNumEdges(int numEdges) {
		this.numEdges = numEdges;
	}

	public ArrayList<String> getCurrentState() {
		return currentState;
	}

	public void setCurrentState(ArrayList<String> currentState) {
		this.currentState = currentState;
	}
}
