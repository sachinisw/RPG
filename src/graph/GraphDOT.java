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
				if(stateVertex.isaPartialDesirableState() && !stateVertex.isaPartialCriticalState()){ //plain desirable
					s += stateVertex.convertToDOTString() + " [shape=circle, style=filled, fillcolor=palegreen1, peripheries=3];" + "\n";
				}else if(!stateVertex.isaPartialDesirableState() && stateVertex.isaPartialCriticalState()) { //plain critical
					s += stateVertex.convertToDOTString() + " [shape=circle, style=filled, fillcolor=lightpink, peripheries=3];" + "\n";
				}else if(stateVertex.isaPartialCriticalState() &&  stateVertex.isaPartialDesirableState()){ //critical and desirable both
					s+=stateVertex.convertToDOTString() + " [shape=circle, style=filled color=red, fillcolor=gold, peripheries=3];"+ "\n";
				}else{
					s += stateVertex.convertToDOTString() + " [shape=circle, penwidth=3];" + "\n";
				}
			}
		} else {
			for (StateVertex stateVertex : leaves) {
				if(stateVertex.isContainsDesirableState() && !stateVertex.isContainsCriticalState()){
					s += stateVertex.convertToDOTString() + " [shape=circle, style=filled, fillcolor=palegreen1, peripheries=3];" + "\n";
				}else if(!stateVertex.isContainsDesirableState() && stateVertex.isContainsCriticalState()) { //plain critical
					s += stateVertex.convertToDOTString() + " [shape=circle, style=filled, fillcolor=lightpink, peripheries=3];" + "\n";
				}else if(stateVertex.isContainsCriticalState() && stateVertex.isContainsDesirableState()){
					s+=stateVertex.convertToDOTString() + " [shape=circle, style=filled color=red, fillcolor=gold, peripheries=3];"+ "\n";
				}else{
					s += stateVertex.convertToDOTString() + " [shape=circle, penwidth=3];" + "\n";
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
				if(v.isaPartialDesirableState() && !v.isaPartialCriticalState()){ //plain desirable
					s += v.convertToDOTString() + " [shape=circle, style=filled, fillcolor=palegreen1, peripheries=3];" + "\n";
				}else if(!v.isaPartialDesirableState() && v.isaPartialCriticalState()) { //plain critical
					s += v.convertToDOTString() + " [shape=circle, style=filled, fillcolor=lightpink, peripheries=3];" + "\n";
				}else if(v.isaPartialCriticalState() &&  v.isaPartialDesirableState()){ //critical and desirable both
					s+=v.convertToDOTString() + " [shape=circle, style=filled color=red, fillcolor=gold, peripheries=3];"+ "\n";
				}else{
					s += v.convertToDOTString() + " [shape=circle, penwidth=3];" + "\n";
				}
			}
		}else {
			while(itr.hasNext()) {
				StateVertex v = itr.next().getValue();
				if(v.isContainsDesirableState() && !v.isContainsCriticalState()){
					s += v.convertToDOTString() + " [shape=circle, style=filled, fillcolor=palegreen1, peripheries=3];" + "\n";
				}else if(!v.isContainsDesirableState() && v.isContainsCriticalState()) { //plain critical
					s += v.convertToDOTString() + " [shape=circle, style=filled, fillcolor=lightpink, peripheries=3];" + "\n";
				}else if(v.isContainsCriticalState() && v.isContainsDesirableState()){
					s+=v.convertToDOTString() + " [shape=circle, style=filled color=red, fillcolor=gold, peripheries=3];"+ "\n";
				}else{
					s += v.convertToDOTString() + " [shape=circle, penwidth=3];" + "\n";
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
