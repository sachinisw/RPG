package vered;

public class TestConfigsVd {
	public static final int obFull = 1;
	public static final int ob50lm = 2;
	public static final int ob75lm = 3;
	public static final int trainedscenario = 0; //if domain is blocks, 0 for tad/bad, 1 for cut/cup
	public static final int instances = 3; //from start arg. run test for this many instances
	public static final int instanceCases = 20;
	public static final int fileLimit = 10;
	public static final String domain = "FERRY"; //EASYIPC //BLOCKS //NAVIGATOR //FERRY
	public static final String planner = "jff/"; //javaff
	public static final String prefix = "/home/sachini/oldhp/sachini/domains/"+domain+"/scenarios/TEST"+trainedscenario+"/";
	public static final String instancedir="inst";
	public static final String a_out = "/outs/attacker/";
	public static final String a_connectivityGraphFile = "connectivity-problem_a";
	public static final String a_rpgFile = "rpg-problem_a";
	public static final String u_connectivityGraphFile = "connectivity-problem_u";//con and rpg for desirable goal in attack domain
	public static final String u_rpgFile = "rpg-problem_u";
	public static final String instscenario="/scenarios/";
	public static final String observationFiles = "/obs/";
	public static final String observation50Files = "/obslm50/";
	public static final String observation75Files = "/obslm75/";
	public static final String domainFile = "/domain.pddl";
	public static final String desirableStateFile = "/desirable.txt"; //attacker and user do not need separate files. the state graphs are generated from the observer's point of view
	public static final String criticalStateFile = "/critical.txt";
	public static final String a_initFile = "/inits_a.txt";
	public static final String a_problemFile = "/problem_a.pddl";
	public static final String u_initFile = "/inits_u.txt";
	public static final String verout = "/ver/";
	public static final String outputfile = "out";
	public static final String testedObservationFiles = "/data/decision/";
	public static final String resultOutpath = "/data/";
	public static final String logfilename = "/inst_"+planner.substring(0, planner.length()-1)+".log";
	public static final String atLmfile = "/outs/a_verifiedlm_full.txt";
	public static final String uLmfile = "/outs/u_verifiedlm_full.txt";
}
