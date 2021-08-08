package landmark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

//landmark generation graph -> reference paper "ordered landmarks in planning" (Hoffmann)
//nodes are found landmarks, edges are the orders found between them. (Hoffmann p217)
public class LGG {
	private HashMap<LGGNode, TreeSet<LGGNode>> adjacencyList;
	private ArrayList<LGGNode> vertices; 
	private ArrayList<LGGEdge> edges;
	private int numVertices;
	private int numEdges;

	public LGG(){
		adjacencyList = new HashMap<>();
		vertices = new ArrayList<>();
		edges = new ArrayList<>();
		numVertices = 0;
		numEdges = 0;
	}

	public LGGNode addLGGNode(ArrayList<String> data) {
		LGGNode v;
		v = findLGGNode(data);
		if (v == null) {
			v = new LGGNode(data);
			vertices.add(v);
			adjacencyList.put(v, new TreeSet<LGGNode>());
			numVertices += 1;
		}
		return v;
	}
	
	public LGGNode removeLGGNode(LGGNode node) { //removes the row for node in adjacency list.
//		System.out.println("removing---------"+node);
		LGGNode v = findLGGNode(node.getValue());
		if (v != null) {
			if(vertices.remove(v)){
				numVertices -= 1;
			}
			TreeSet<LGGNode> neighbors = adjacencyList.get(v); //get node's neighbor set. remove edges node-neighbor
			for (LGGNode neighbornode : neighbors) {
				LGGEdge edge = findEdge(v, neighbornode);
				if( edges.remove(edge)){
					numEdges--;
				}
			}
			adjacencyList.remove(v); //remove node's row from adjacency list
			Iterator<Map.Entry<LGGNode, TreeSet<LGGNode>>> itr = adjacencyList.entrySet().iterator();
			while(itr.hasNext()){ //from remaining vertices, find occurrences of node in neighbor list. remove any vertex-node edges
//				TreeSet<LGGNode> remainingNodeNeighbors = itr.next().getValue();
//				LGGNode current = itr.next().getKey();
				Map.Entry<LGGNode, TreeSet<LGGNode>> pair = itr.next();
//				System.out.println("current= "+pair.getKey());
//				System.out.println("neighbors="+Arrays.toString(pair.getValue().toArray()));
				Iterator<LGGNode> treeItr = pair.getValue().iterator();
				while(treeItr.hasNext()) {
					LGGNode curNeighbor = treeItr.next();
					if(curNeighbor.isEqual(node)){
						LGGEdge edge = findEdge(pair.getKey(), curNeighbor);
						if( edges.remove(edge)){
							treeItr.remove();
							numEdges--;
						}
					}
				}
			}
		}
		return v;
	}
	
	public LGGNode findLGGNode(ArrayList<String> data){
		LGGNode in = new LGGNode(data);
		for (LGGNode node : vertices) {
			if(node.isEqual(in)){
				return node;
			}
		}
		return null;
	}

	public void addEdge(ArrayList<String> fromState, ArrayList<String> toState) {
		if (hasEdge(fromState, toState))
			return;
		numEdges += 1;
		if (findLGGNode(fromState) == null)
			addLGGNode(fromState);
		if (findLGGNode(toState) == null)
			addLGGNode(toState);
		for(Map.Entry<LGGNode, TreeSet<LGGNode>> entry : adjacencyList.entrySet()){
			LGGNode from = findLGGNode(fromState);
			LGGNode to = findLGGNode(toState);
			if(from != null && entry.getKey().isEqual(from)){
				entry.getValue().add(to);
				edges.add(new LGGEdge(from, to));
			}
		}
	}
	
	public boolean hasEdge(ArrayList<String> stateFrom, ArrayList<String> stateTo) {
		if (!hasVertex(stateFrom) || !hasVertex(stateTo))
			return false;
		LGGNode from = findLGGNode(stateFrom);
		LGGNode to = findLGGNode(stateTo);
		return adjacencyList.get(from).contains(to);
	}

	public LGGEdge findEdge(LGGNode from, LGGNode to){
		LGGEdge edge = new LGGEdge(from, to);
		for (LGGEdge lggEdge : edges) {
			if(lggEdge.equals(edge)){
				return lggEdge;
			}
		}
		return null;
	}
	
	public boolean hasVertex(ArrayList<String> state) {
		if(findLGGNode(state)!= null){
			return true;
		}
		return false;
	}
	/* Returns adjacency-list representation of graph
	 */
	public String toString() {
		String s = "";
		for (LGGNode v : vertices) {
			s += v.toString() + ": ";
			for (LGGNode w : adjacencyList.get(v)) {
				s += w.toString() + " " ;
			}
			s += "\n";
		}
		return s;
	}

	public HashMap<LGGNode, TreeSet<LGGNode>> getAdjacencyList() {
		return adjacencyList;
	}

	public void setAdjacencyList(HashMap<LGGNode, TreeSet<LGGNode>> adjacencyList) {
		this.adjacencyList = adjacencyList;
	}

	public ArrayList<LGGNode> getVertices() {
		return vertices;
	}

	public void setVertices(ArrayList<LGGNode> vertices) {
		this.vertices = vertices;
	}

	public int getNumVertices() {
		return numVertices;
	}

	public void setNumVertices(int numVertices) {
		this.numVertices = numVertices;
	}

	public ArrayList<LGGEdge> getEdges() {
		return edges;
	}

	public void setEdges(ArrayList<LGGEdge> edges) {
		this.edges = edges;
	}

	public int getNumEdges() {
		return numEdges;
	}

	public void setNumEdges(int numEdges) {
		this.numEdges = numEdges;
	}

}
