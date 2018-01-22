package run;

import graph.StateGraph;

public class Run {

	public static void main(String[] args) {
		int reverseConfig = 1;
		StateGenerator gen = new StateGenerator();
		StateGraph graph = gen.enumerateStates();
		if(reverseConfig==0){
			gen.graphToDOT(graph);
		}else{
			gen.graphToDOTNoUndo(graph);
		}
		graph.printMetrics();
	}
}
