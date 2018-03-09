package graph;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import run.CriticalState;
import run.DesirableState;

public class GraphDOT {
	private StateGraph graph;
	private DesirableState desirable;
	private CriticalState critical;
	private ArrayList<String> dotLines;

	public GraphDOT(StateGraph g, DesirableState s, CriticalState c){
		graph = g;
		desirable = s;
		critical = c;
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
			if(stateVertex.containsGoalState(desirable.getDesirable())){
				s += stateVertex.convertToDOTString() + " [shape=doublecircle,color=green, peripheries=3];" + "\n";
			}else if(stateVertex.containsCriticalState(critical.getCritical())){
				s+=stateVertex.convertToDOTString() + " [shape=doublecircle, color=crimson, peripheries=3];"+ "\n";
			}else{
				s += stateVertex.convertToDOTString() + " [shape=doublecircle, penwidth=3];" + "\n";
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

	public DesirableState getState() {
		return desirable;
	}

	public void setState(DesirableState state) {
		this.desirable = state;
	}

	public ArrayList<String> getDotLines() {
		return dotLines;
	}

	public void setDotLines(ArrayList<String> dotLines) {
		this.dotLines = dotLines;
	}

	public CriticalState getCritical() {
		return critical;
	}

	public void setCritical(CriticalState critical) {
		this.critical = critical;
	}
}
