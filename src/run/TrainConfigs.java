package run;

public class TrainConfigs {
	public static final int scenario = 0;
	public static final int runmode = 0;
	public static final int cases = 20;
	public static final boolean writeDOT = true;
	public static final String domain = "BLOCKS"; //NAVIGATOR, EASYIPC, BLOCKS, FERRY
	public static final String root = "/home/sachini/domains/"+domain+"/scenarios/"+scenario+"/train/cases/";
	public static String obsdir = "/obs/";
	public static String outsdir = "/outs/";
	public static String a_dotFilePrefix = "/dot/";
	public static String domainFile = "/domain.pddl";
	public static String dstates = "/desirable.txt"; //attacker and user do not need separate files. the state graphs are generated from the observer's point of view
	public static String cstates = "/critical.txt";
	public static String a_output = "/attacker/"; //clean this directory before running. if not graphs will be wrong
	public static String a_initFile = "/inits_a.txt";
	public static String a_problemFile = "/problem_a.pddl";
	public static String a_dotFileSuffix = "graph_ad_noreverse_";
	public static String u_problemFile = "/problem_u.pddl";
	public static String u_output = "/user/"; 
	public static String u_dotFileSuffix = "graph_ag_noreverse_";
	public static String u_initFile = "/inits_u.txt";
	public static String weightedCSV = "/data/weighted/";
	public static String decisionCSV = "/data/decision/";
	public static String owFile = "/home/sachini/domains/"+domain +"/configs/ow_short.config";
	public static String lmoutputFile = "/verifiedlm.txt";
}
