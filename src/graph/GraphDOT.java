package graph;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

public class GraphDOT {
	private String domain;
	private StateGraph graph;
	private ArrayList<String> dotLines;

	public GraphDOT(StateGraph g, String dom){
		graph = g;
		dotLines = new ArrayList<>();
		domain = dom;
	}

	public void generateDOT(String filename){
		dotLines.add(getDOTHeader());
		dotLines.add(getDOTEdges());
		dotLines.add(markLeafNodes());
		dotLines.add(markNonLeafNodes());
		dotLines.add(getDOTFooter());
		writeDOTFile(filename);
	}

	public void generateDOTNoUndo(String filename){
		dotLines.add(getDOTHeader());
		dotLines.add(getDOTEdgesWithoutUndo());
		dotLines.add(markLeafNodes());
		dotLines.add(markNonLeafNodes());
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
		if(domain.equalsIgnoreCase("BLOCKS")) { //partial states ok
			for (StateVertex stateVertex : leaves) {
				if(stateVertex.isaPartialDesirableState()){
					s += stateVertex.convertToDOTString() + " [shape=doublecircle,color=green, peripheries=3];" + "\n";
				}else if(stateVertex.isaPartialCriticalState()){
					s+=stateVertex.convertToDOTString() + " [shape=doublecircle, color=crimson, peripheries=3];"+ "\n";
				}else{
					s += stateVertex.convertToDOTString() + " [shape=doublecircle, penwidth=3];" + "\n";
				}
			}
		} else {
			for (StateVertex stateVertex : leaves) {
				if(stateVertex.isContainsDesirableState()){
					s += stateVertex.convertToDOTString() + " [shape=doublecircle,color=green, peripheries=5];" + "\n";
				}else if(stateVertex.isContainsCriticalState()){
					s+=stateVertex.convertToDOTString() + " [shape=doublecircle, color=crimson, peripheries=3];"+ "\n";
				}else{
					s += stateVertex.convertToDOTString() + " [shape=doublecircle, penwidth=3];" + "\n";
				}
			}
		}
		return s;
	}

	private String markNonLeafNodes(){ //any desirable/undesirable vertices that are not leaves
		String s = "";
		Iterator<Entry<String, StateVertex>> itr = graph.getVertices().entrySet().iterator();
		if(domain.equalsIgnoreCase("BLOCKS")) { //partial states ok
			while(itr.hasNext()) {
				StateVertex v = itr.next().getValue();
				if(v.isaPartialDesirableState()){
					s += v.convertToDOTString() + " [shape=doublecircle,color=green, peripheries=3];" + "\n";
				}else if(v.isaPartialCriticalState()){
					s+=v.convertToDOTString() + " [shape=doublecircle, color=crimson, peripheries=3];"+ "\n";
				}
			}
		}else {
			while(itr.hasNext()) {
				StateVertex v = itr.next().getValue();
				if(v.isContainsDesirableState()){
					s += v.convertToDOTString() + " [shape=doublecircle,color=green, peripheries=3];" + "\n";
				}else if(v.isContainsCriticalState()){
					s+=v.convertToDOTString() + " [shape=doublecircle, color=crimson, peripheries=3];"+ "\n";
				}
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

	public ArrayList<String> getDotLines() {
		return dotLines;
	}

	public void setDotLines(ArrayList<String> dotLines) {
		this.dotLines = dotLines;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}
}
