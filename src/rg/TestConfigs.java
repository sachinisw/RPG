package rg;

public class TestConfigs {
	public static final int runmode = 1; 
	public static final int trainedscenario = 0;
	public static final int instances = 1; //from start arg. run test for this many instances
	public static final int instanceCases = 20;
	public static final int fileLimit = 10;
	public static final String domain = "EASYIPC"; //EASYIPC //BLOCKS //NAVIGATOR //FERRY
	public static final String planner = "lama/"; //hsp //lama
	public static final String prefix = "/home/sachini/domains/"+domain+"/scenarios/TEST"+trainedscenario+"/";
	public static final String instancedir="inst";
	public static final String instscenario="/scenarios/";
	public static final String observationFiles = "/obs/";  
	public static final String domainFile = "/domain.pddl";
	public static final String desirableStateFile = "/desirable.txt"; //attacker and user do not need separate files. the state graphs are generated from the observer's point of view
	public static final String criticalStateFile = "/critical.txt";
	public static final String a_initFile = "/inits_a.txt";
	public static final String a_problemFile = "/problem_a.pddl";
	public static final String u_initFile = "/inits_u.txt";
	public static final String rgout = "/rg/";
	public static final String outputfile = "out";
	public static final String testedObservationFiles = "/data/decision/";
}
