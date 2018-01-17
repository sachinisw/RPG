package run;

import graph.StateGraph;

public class Run {

	public static void main(String[] args) {
		StateGenerator gen = new StateGenerator();
		StateGraph graph = gen.enumerateStates();
		gen.graphToDOT(graph);
		graph.printMetrics();
	}
}
