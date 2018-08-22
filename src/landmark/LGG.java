package landmark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
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

	//remove unverified landmarks
	public void applyVerication(ArrayList<LGGNode> vlm){
		Iterator<LGGNode> itr = getAdjacencyList().keySet().iterator();
		while(itr.hasNext()){	//remove node itself
			if (!itr.next().find(vlm)) {
				itr.remove();
			}
		}
		Iterator<Entry<LGGNode, TreeSet<LGGNode>>> it = getAdjacencyList().entrySet().iterator(); 
		while (it.hasNext()) { //remove edges in remaining nodes connected to the unverified node
			Map.Entry<LGGNode, TreeSet<LGGNode>> pair = (Map.Entry<LGGNode, TreeSet<LGGNode>>)it.next();
			Iterator<LGGNode> nbr = pair.getValue().iterator();
			while(nbr.hasNext()){
				if(nbr.next().find(vlm)){
					nbr.remove();
				}
			}
		}
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
