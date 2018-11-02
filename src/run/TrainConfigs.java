package run;

public class TrainConfigs {
	public static final int scenario = 5;
	public static final int runmode = 0;
	public static final boolean writeDOT = false;
	public static final String domain = "BLOCKS"; //EASYIPC, BLOCKS
	public static String observationFiles = "/home/sachini/domains/"+domain +"/scenarios/"+scenario+"/obs/";
	public static String domainFile = "/home/sachini/domains/"+domain +"/scenarios/"+scenario+"/domain.pddl";
	public static String desirableStateFile = "/home/sachini/domains/"+domain +"/scenarios/"+scenario+"/desirable.txt"; //attacker and user do not need separate files. the state graphs are generated from the observer's point of view
	public static String criticalStateFile = "/home/sachini/domains/"+domain +"/scenarios/"+scenario+"/critical.txt";
	public static String a_initFile = "/home/sachini/domains/"+domain +"/scenarios/"+scenario+"/inits_a.txt";
	public static String a_problemFile = "/home/sachini/domains/"+domain +"/scenarios/"+scenario+"/problem_a.pddl";
	public static String a_dotFilePrefix = "/home/sachini/domains/"+domain +"/scenarios/"+scenario+"/dot/";
	public static String a_dotFileSuffix = "graph_ad_noreverse_";
	public static String u_problemFile = "/home/sachini/domains/"+domain +"/scenarios/"+scenario+"/problem_u.pddl";
	public static String u_outputPath = "/home/sachini/domains/"+domain +"/scenarios/"+scenario+"/outs/user/"; 
	public static String a_outputPath = "/home/sachini/domains/"+domain +"/scenarios/"+scenario+"/outs/attacker/"; //clean this directory before running. if not graphs will be wrong
	public static String u_dotFilePrefix = "/home/sachini/domains/"+domain +"/scenarios/"+scenario+"/dot/";
	public static String u_dotFileSuffix = "graph_ag_noreverse_";
	public static String u_initFile = "/home/sachini/domains/"+domain +"/scenarios/"+scenario+"/inits_u.txt";
	public static String weightedCSV = "/home/sachini/domains/"+domain +"/scenarios/"+scenario+"/data/weighted/";
	public static String decisionCSV = "/home/sachini/domains/"+domain +"/scenarios/"+scenario+"/data/decision/";
	public static String owFile = "/home/sachini/domains/"+domain +"/configs/ow_short.config";
	public static String lmoutputFile = "/home/sachini/domains/"+domain +"/scenarios/"+scenario+"/outs/verifiedlm.txt";
}
