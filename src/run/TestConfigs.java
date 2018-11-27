package run;

public class TestConfigs {
	public static final int runmode = 1; 
	public static final int trainedscenario = 1;
	public static final int instances = 2; //which test instance is it?
	public static final int instanceCases = 20;
	public static final boolean writeDOT = false;
	public static final String domain = "BLOCKS"; //EASYIPC //BLOCKS //NAVIGATOR //FERRY
	public final static String prefix = "/home/sachini/domains/"+domain+"/scenarios/TEST"+trainedscenario+"/";
	public final static String instancedir="inst";
	public final static String instscenario="/scenarios/";
	public static String observationFiles = "/obs/";  
	public static String observationLMFiles = "/obslm/";
	public static String domainFile = "/domain.pddl";
	public static String desirableStateFile = "/desirable.txt"; //attacker and user do not need separate files. the state graphs are generated from the observer's point of view
	public static String criticalStateFile = "/critical.txt";
	public static String a_initFile = "/inits_a.txt";
	public static String a_problemFile = "/problem_a.pddl";
	public static String a_dotFilePrefix = "/dot/full/";
	public static String a_dotFileLMPrefix = "/dot/lm/";
	public static String a_dotFileSuffix = "graph_ad_noreverse_";
	public static String a_outputPath = "/outs/attacker/"; //clean this directory before running. if not graphs will be wrong
	public static String u_problemFile = "/problem_u.pddl";
	public static String u_outputPath = "/outs/user/"; 
	public static String u_dotFilePrefix = "/dot/full/";
	public static String u_dotFileLMPrefix = "/dot/lm/";
	public static String u_dotFileSuffix = "graph_ag_noreverse_";
	public static String u_initFile = "/inits_u.txt";
	public static String weightedCSV = "/data/weighted/";
	public static String decisionCSV = "/data/decision/";
	public static String owFile = "/home/sachini/domains/BLOCKS/configs/ow_short.config"; //doesnt have to change with domain
	public static String lmoutputFull = "/outs/verifiedlm_full.txt";
	public static String lmoutputShort = "/outs/verifiedlm_short.txt";
}
