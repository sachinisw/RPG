package graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeSet;

public class StateGraph {
	private HashMap<StateVertex, TreeSet<StateVertex>> adjacencyList; //[vertice --> vertice adj. list]
	private HashMap<String, StateVertex> vertices; //[verticename --> vertice]
	private ArrayList<ActionEdge> edges;
	private int numVertices;
	private int numEdges;

	/**
	 * Construct empty Graph
	 */
	public StateGraph() {
		adjacencyList = new HashMap<StateVertex, TreeSet<StateVertex>>();
		vertices = new HashMap<String, StateVertex>();
		edges = new ArrayList<ActionEdge>();
		numVertices = numEdges = 0;
	}

	/**
	 * Add a new vertex name with no neighbors (if vertex does not yet exist)
	 */
	public StateVertex addVertex(ArrayList<String> state) {
		StateVertex v;
		v = findVertex(state);
		if (v == null) {
			v = new StateVertex();
			v.addStates(state);
			v.setName();
			vertices.put(v.getName(), v);
			adjacencyList.put(v, new TreeSet<StateVertex>());
			numVertices += 1;
		}
		return v;
	}

	/**
	 *Is from-to, an edge in this Graph. The graph is directional so the order of from and to matters.
	 */
	public boolean hasEdge(ArrayList<String> stateFrom, ArrayList<String> stateTo) {
		if (!hasVertex(stateFrom) || !hasVertex(stateTo))
			return false;
		StateVertex vFrom = new StateVertex();
		vFrom.addStates(stateFrom);
		vFrom.setName();

		StateVertex vTo = new StateVertex();
		vTo.addStates(stateTo);
		vTo.setName();
		return adjacencyList.get(vertices.get(vFrom.getName())).contains(vertices.get(vTo.getName()));
	}

	/**
	 * Returns true iff v with state is in this Graph, false otherwise
	 */
	public boolean hasVertex(ArrayList<String> state) {
		if(findVertex(state)!= null){
			return true;
		}
		return false;
	}

	/**
	 * Add toState to from's set of neighbors, do not add fromState to toState's set of neighbors. directional. Does not add an edge if another edge already exists
	 */
	public void addEdge(ArrayList<String> fromState, ArrayList<String> toState, String action) {
		if (hasEdge(fromState, toState))
			return;
		numEdges += 1;
		if (findVertex(fromState) == null)
			addVertex(fromState);
		if (findVertex(toState) == null)
			addVertex(toState);

		for(Map.Entry<StateVertex, TreeSet<StateVertex>> entry : adjacencyList.entrySet()){
			StateVertex from = findVertex(fromState);
			StateVertex to = findVertex(toState);
			if(from != null && entry.getKey().isEqual(from)){
				entry.getValue().add(to);
				edges.add(new ActionEdge(action, from, to));
			}
		}
	}

	public StateVertex findVertex(ArrayList<String> state){
		StateVertex in = new StateVertex();
		in.addStates(state);
		for(Map.Entry<String, StateVertex> entry : vertices.entrySet()){
			if(entry.getValue().isEqual(in)){
				return entry.getValue();
			}
		}
		return null;
	}

	/* Returns adjacency-list representation of graph
	 */
	public String toString() {
		String s = "";
		for (StateVertex v : vertices.values()) {
			s += v.toString() + ": ";
			for (StateVertex w : adjacencyList.get(v)) {
				s += w.toString() + " ";
			}
			s += "\n";
		}
		return s;
	}

	public String printEdges(){
		String s = "";
		for (ActionEdge e : edges) {
			s += e.toString() + " ";
			s += "\n";
		}
		return s;
	}

	public boolean isEdgeBidirectional(String action, ArrayList<String> currentState){
		//find edges with name=action
		ArrayList<ActionEdge> contains  = new ArrayList<ActionEdge>();
		StateVertex temp =  new StateVertex();
		temp.addStates(currentState);

		for(int i=0; i<edges.size(); i++){
			if(edges.get(i).getAction().equals(action)){
				contains.add(edges.get(i));
			}
		}
		for (ActionEdge actionEdge : contains) {
			StateVertex stateFrom = actionEdge.getFrom();
			StateVertex stateTo = actionEdge.getTo();
			if(stateFrom.isEqual(temp)){
				TreeSet<StateVertex> adjFrom = adjacencyList.get(stateFrom);
				Iterator<StateVertex> itrF = adjFrom.iterator();
				while(itrF.hasNext()){
					StateVertex fromsAdj = itrF.next();
					if(fromsAdj.isEqual(stateTo)){
						TreeSet<StateVertex> adjTo = adjacencyList.get(fromsAdj);
						Iterator<StateVertex> itrT = adjTo.iterator();
						while(itrT.hasNext()){
							if(itrT.next().isEqual(stateFrom)){
								return true;//To's adj contains From. and From's adj contains To
							}
						}
					}
				}
			}
		}
		return false;
	}

	/*return nodes that do not have elements in their adj. list
	 * */
	public ArrayList<StateVertex> getLeafNodes(){
		ArrayList<StateVertex> leaves = new ArrayList<StateVertex>();
		for (StateVertex v : vertices.values()) {
			if(adjacencyList.get(v).size()==0){
				leaves.add(v);
			}
		}
		return leaves;
	}

	/*print graph metrics
	 * */
	public void printMetrics(){
		System.out.println("----------Graph Metrics------------");
		System.out.println("No. of Vertices= "+getNumVertices());
		System.out.println("No. of Edges= "+ getNumEdges());
		System.out.println("Vertex Out Degrees");
		System.out.println(prettyPrintMap(getVertexOutDegree()));
		System.out.println("Vertex In Degrees");
		System.out.println(prettyPrintMap(getVertexInDegree()));
		System.out.println("Graph Distance");
		prettyPrintGraphDitances(getGraphDistance());
		System.out.println("\nGraph Diameter= "+getGraphDiameter());
		System.out.println("\nVertex Eccentricity");
		prettyPrintVertexEccentricity();
	}

	//greatest distance between any pair of vertices in the graph g.
	public GraphDiameter getGraphDiameter(){
		int[][] distMatrix = getGraphDistance();
		HashMap<StateVertex, Integer> map = assignIntIDToVertex();
		int max = 0, f=-1, t=-1;
		StateVertex from=null, to=null;
		for(int i=0; i<distMatrix.length; i++){
			for(int j=0; j<distMatrix[i].length; j++){
				if(distMatrix[i][j]>max){
					max=distMatrix[i][j];
					f = i;
					t = j;
				}
			}
		}
		for (StateVertex v : vertices.values()) {
			if(map.get(v).intValue()==f){
				from=v;
			}
			if(map.get(v).intValue()==t){
				to=v;
			}
		}
		return new GraphDiameter(max, from, to);
	}

	//gives the length of the longest shortest path from the source s to every other vertex in the graph g.
	public VertexEccentricity getVertexEccentricity(StateVertex v){
		HashMap<StateVertex, Integer> map = assignIntIDToVertex();
		int[][] distMatrix = getGraphDistance();
		int vId = map.get(v);
		int [] paths = distMatrix[vId];
		int max=0, t=-1;
		StateVertex to = null;
		for(int i=0; i<paths.length; i++){
			if(max<paths[i]){
				max=paths[i];
				t=i;
			}
		}
		for (StateVertex x : vertices.values()) {
			if(map.get(x).intValue()==t){
				to=x;
			}
		}
		return new VertexEccentricity(max, v, to);
	}

	private void prettyPrintVertexEccentricity(){
		for (StateVertex v : vertices.values()) {
			System.out.println(getVertexEccentricity(v));
		}
	}

	//vertex connectivity of a graph g is the smallest number of vertices whose deletion from g disconnects g.
	public void vertexConnctivity(){
		
	}
	
	//The edge connectivity of a graph g is the smallest number of edges whose deletion from g disconnects g.
	public int getEdgeConnectivity(){
		return 0;
	}
	
	//the number of in-edges for each vertex
	public HashMap<StateVertex, Integer> getVertexInDegree(){
		HashMap<StateVertex, Integer> dIn = new HashMap<>();
		for (ActionEdge actionEdge : edges) {
			if(!dIn.containsKey(actionEdge.getTo())){
				dIn.put(actionEdge.getTo(), 1);
			}else{
				dIn.put(actionEdge.getTo(), dIn.get(actionEdge.getTo()).intValue()+1);
			}		
		}
		return dIn;
	}

	//the number of out-edges for each vertex
	public HashMap<StateVertex, Integer> getVertexOutDegree(){
		HashMap<StateVertex, Integer> dOut = new HashMap<>();
		for (StateVertex v : vertices.values()) {
			dOut.put(v, adjacencyList.get(v).size());
		}
		return dOut;
	}

	private String prettyPrintMap(HashMap<StateVertex, Integer> map){
		String s = "";
		for (Map.Entry<StateVertex, Integer> entry : map.entrySet()) {
			StateVertex key = entry.getKey();
			Integer value = entry.getValue();
			s += key.toString() + ": " + value.intValue()+"\n";
		}
		return s;
	}

	//single source shortest path. Integer overflow for nodes with no neighbors.
	public int[] singleSourceDijkstra(StateVertex source){
		int [] dist = new int [getNumVertices()];
		PriorityQueue<PriorityVertex> queue = new PriorityQueue<PriorityVertex>(10, new PriorityVertexComparator()); //priority by distance
		ArrayList<PriorityVertex> visited = new ArrayList<>();
		//initialize dist: source = 0, others = infinity
		HashMap<StateVertex, Integer> vertexIds = assignIntIDToVertex();
		int srcID = vertexIds.get(source);
		dist[srcID] = 0;
		for(int i=0; i<dist.length; i++){
			if(i!=srcID){
				dist[i] = Integer.MAX_VALUE;
			}
		}
		//add all vertices as PriorityVertex to queue
		for (Map.Entry<StateVertex, Integer> entry : vertexIds.entrySet()) {
			StateVertex key = entry.getKey();
			Integer value = entry.getValue();
			queue.add(new PriorityVertex(key, value.intValue(), dist[value.intValue()]));
		}		
		//dijkstra
		while(!queue.isEmpty()){
			PriorityVertex current = queue.poll();
			if(!visited.contains(current)){
				visited.add(current);
				TreeSet<StateVertex> currentAdj = adjacencyList.get(current.getVertex());
				for (StateVertex v : currentAdj) {
					//System.out.println(v.getName());
					int vId = vertexIds.get(v);
					int currentId = vertexIds.get(current.getVertex());
					if(dist[currentId]+1<dist[vId]){ //edge weight=1
						dist[vId] = dist[currentId]+1;
					}
				}
			}
			//update PrirotyVertex distance values in queue. updating in queue itself won't work. 
			//so use a temp to copy everything, then update, clear old queue, then copy back from temp to old queue
			PriorityQueue<PriorityVertex> temp = new PriorityQueue<>(10, new PriorityVertexComparator());
			temp.addAll(queue);
			for (PriorityVertex pv : temp) {
				int id = vertexIds.get(pv.getVertex());
				pv.setDistance(dist[id]);
			}
			queue.clear();
			queue.addAll(temp);
		}
		return dist;
	}

	//gives the distance from s to all vertices of the graph g.
	public int [][] getGraphDistance(){
		int [][] distances = new int [getNumVertices()][getNumVertices()];
		HashMap<StateVertex, Integer> map = assignIntIDToVertex();
		for (StateVertex v : vertices.values()) {
			int vId = map.get(v);
			int [] distForV = singleSourceDijkstra(v);
			distances[vId] = distForV;
		}
		return distances;
	}

	private HashMap<StateVertex, Integer> assignIntIDToVertex(){
		HashMap<StateVertex, Integer> map = new HashMap<>();
		int id = 0;
		for (StateVertex v : vertices.values()) {
			map.put(v, id);
			id++;
		}
		return map;
	}

	private void prettyPrintGraphDitances(int [][] distances){
		HashMap<StateVertex, Integer> map = assignIntIDToVertex();
		for (StateVertex v : vertices.values()) {
			int vId = map.get(v);
			System.out.println(v.getName()+ " id="+ vId +" : "+ Arrays.toString(distances[vId]));
		}
	}

	public int getNumVertices() {
		return numVertices;
	}

	public void setNumVertices(int numVertices) {
		this.numVertices = numVertices;
	}

	public int getNumEdges() {
		return numEdges;
	}

	public void setNumEdges(int numEdges) {
		this.numEdges = numEdges;
	}

	public HashMap<StateVertex, TreeSet<StateVertex>> getAdjacencyList() {
		return adjacencyList;
	}

	public void setAdjacencyList(HashMap<StateVertex, TreeSet<StateVertex>> adjacencyList) {
		this.adjacencyList = adjacencyList;
	}

	public HashMap<String, StateVertex> getVertices() {
		return vertices;
	}

	public void setVertices(HashMap<String, StateVertex> vertices) {
		this.vertices = vertices;
	}

	public ArrayList<ActionEdge> getEdges() {
		return edges;
	}

	public void setEdges(ArrayList<ActionEdge> edges) {
		this.edges = edges;
	}
}
