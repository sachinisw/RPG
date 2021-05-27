package test;

public class Harness {
	public static void main(String[] args) {
		TestInstanceGenerator.generateProblemsFromTemplate(HarnessConfigs.testInstanceStart); 		//From templates, generate 3 test instances with 'testProblemCount' problems
		InstanceProblemGenerator.generateTracesForInstances(HarnessConfigs.testInstanceStart);		//Using the 20 template problems, generate x (20 for blocks, 10 for grid) problem scenarios for each test instance
		ReducedTraceGenerator.generateReducedTrace(HarnessConfigs.testInstanceStart);		//generate reduced trace considering landmarks 
	}
}