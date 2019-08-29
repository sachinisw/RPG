package rush;

public class LabelConfigs {
	public static final int trainedscenario = 0;
	public static final int instances = 3; //from start arg. run test for this many instances
	public static final int instanceCases = 20;
	public static final String domain = "RUSHHOUR"; //EASYIPC //BLOCKS //NAVIGATOR //FERRY //RUSHHOUR
	public static final String prefix = "/home/sachini/domains/"+domain+"/scenarios/"+trainedscenario+"/train/cases/";
	public static final String topkdir = "/topk/";
	public static final String instancedir="inst";
	public static final String instscenario="/scenarios/";
	public static final String observationFiles = "/obs/";
	public static final String domainFile = "/domain.pddl";
	public static final String desirableStateFile = "/desirable.txt"; //attacker and user do not need separate files. the state graphs are generated from the observer's point of view
	public static final String criticalStateFile = "/critical.txt";
	public static final String a_initFile = "/inits_a.txt";
	public static final String a_problemFile = "/problem_a.pddl";
	public static final String a_outputPath = "/outs/attacker/"; //clean this directory before running. if not graphs will be wrong
	public static final String u_problemFile = "/problem_u.pddl";
	public static final String u_outputPath = "/outs/user/"; 
	public static final String u_initFile = "/inits_u.txt";
}
