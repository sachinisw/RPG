package test;

public class Harness {
	public static void main(String[] args) {
		TestInstanceGenerator.generateProblemsFromTemplate(); 		//From templates, generate 3 test instances with 'testProblemCount' problems
		InstanceProblemGenerator.generateTracesForInstances();		//Generate x (20 for blocks, 2/3/4 for grid) problem scenarios for each test instance
		ReducedTraceGenerator.generateReducedTrace();		//generate reduced trace considering landmarks 
	}
}
