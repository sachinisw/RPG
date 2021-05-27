package run;

public class TrainConfigsML {
	public static final int K = 50; //50 //100
	public static final int scenario = 1;
	public static final int runmodeTrain = 0;
	public static final int cases = 20; //rushhour cases 10 (20 takes too long)
	public static final int fileLimit = 110; //RUSHHOUR 110, rest 100
	public static final boolean writeDOT = false;
	public static final String domain = "BLOCKS"; //NAVIGATOR, EASYIPC, BLOCKS, FERRY, RUSHHOUR
	public static final String root = "/home/sachini/oldhp/sachini/domains/"+domain+"/scenarios/"+scenario+"/train/cases/";
	public static final String traindir = "train/cases/";
	public static final String obsdir = "/obs/";
	public static final String outsdir = "/outs/";
	public static final String dotdir = "/dot/";
	public static final String datadir = "/data/";
	public static final String topkdir = "/topk/";
	public static final String domainFile = "/domain.pddl";
	public static final String dstates = "/desirable.txt"; //attacker and user do not need separate files. the state graphs are generated from the observer's point of view
	public static final String cstates = "/critical.txt";
	public static final String a_output = "attacker/"; //clean this directory before running. if not graphs will be wrong
	public static final String a_initFile = "/init_a.txt";
	public static final String a_problemFile = "/problem_a.pddl";
	public static final String a_dotFileSuffix = "graph_ad_noreverse_";
	public static final String u_problemFile = "/problem_u.pddl";
	public static final String u_output = "user/"; 
	public static final String u_dotFileSuffix = "graph_ag_noreverse_";
	public static final String u_initFile = "/init_u.txt";
	public static final String weightedCSV = "weighted/";
	public static final String decisionCSV = "decision/";
	public static final String owFile = "/home/sachini/oldhp/sachini/domains/"+domain +"/configs/ow_short.config";
	public static final String lmoutputFile = "verifiedlm.txt";
}
