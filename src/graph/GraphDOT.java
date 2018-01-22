package graph;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import run.State;

public class GraphDOT {
	private StateGraph graph;
	private State state;
	private ArrayList<String> dotLines;

	public GraphDOT(StateGraph g, State s){
		graph = g;
		state = s;
		dotLines = new ArrayList<>();
	}

	public void generateDOT(String filename){
		dotLines.add(getDOTHeader());
		dotLines.add(getDOTEdges());
		dotLines.add(markLeafNodes());
		dotLines.add(getDOTFooter());
		writeDOTFile(filename);
	}
	
	public void generateDOTNoUndo(String filename){
		dotLines.add(getDOTHeader());
		dotLines.add(getDOTEdgesWithoutUndo());
		dotLines.add(markLeafNodes());
		dotLines.add(getDOTFooter());
		writeDOTFile(filename);
	}

	private String getDOTHeader(){
		return "digraph {\n";
	}
	
	private String getDOTFooter(){
		return "}\n";
	}
	
	private String getDOTEdges(){
		String s = "";
		for (ActionEdge e : graph.getEdges()) {
			s += e.convertToDOTString();
			s += "\n";
		}
		return s;
	}
	
	private String getDOTEdgesWithoutUndo(){
		String s = "";
		for (ActionEdge e : graph.getEdges()) {
			if(!e.isReverse()){
				s += e.convertToDOTString();
				s += "\n";
			}
		}
		return s;
	}
	
	private String markLeafNodes(){ 
		String s = "";
		ArrayList<StateVertex> leaves = graph.getLeafNodes();
		for (StateVertex stateVertex : leaves) {
			if(stateVertex.containsGoalState(state.getUndesirable())){
				s += stateVertex.convertToDOTString() + " [shape=doublecircle,color=rosybrown1,style=filled,penwidth=3];" + "\n";
			}else if(stateVertex.containsGoalState(state.getDesirable())){
				s += stateVertex.convertToDOTString() + " [shape=doublecircle,color=palegreen,style=filled,penwidth=3];" + "\n";
			}else{
				s += stateVertex.convertToDOTString() + " [shape=doublecircle,style=filled,penwidth=3];" + "\n";
			}
		}
		return s;
	}
		
	private void writeDOTFile(String filename){
		FileWriter writer = null;
		try {
			File file = new File(filename);
			writer = new FileWriter(file);
			for(int i=0; i<dotLines.size(); i++){
				writer.write(dotLines.get(i));
			}	
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	public StateGraph getGraph() {
		return graph;
	}

	public void setGraph(StateGraph graph) {
		this.graph = graph;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public ArrayList<String> getDotLines() {
		return dotLines;
	}

	public void setDotLines(ArrayList<String> dotLines) {
		this.dotLines = dotLines;
	}
}
