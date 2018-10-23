package test;

public class Harness {
	public static void main(String[] args) {
		//From templates, generate 3 test instances with 'testProblemCount' problems
		TestInstanceGenerator.generateProblemsFromTemplate();
		//Generate x (20 for blocks, 2/3/4 for grid) problem scenarios (and files needed for producing test data) for each test instance
		InstanceProblemGenerator.generateTracesForInstances();
		ReducedTraceGenerator.generateReducedTrace();
	}
}
