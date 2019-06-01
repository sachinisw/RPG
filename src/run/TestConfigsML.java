package run;

public class TestConfigsML {
	public static final boolean writeDOT = false;
	public static final int runmode = 1; 
	public static final int trainedscenario = 0;
	public static final int instances = 3; //from start arg. run test for this many instances
	public static final int instanceCases = 20;
	public static final int fileLimit = 10;
	public static final int [] limitRatio = {50,75};
	public static final String domain = "BLOCKS"; //EASYIPC //BLOCKS //NAVIGATOR //FERRY
	public static final String prefix = "/home/sachini/domains/"+domain+"/scenarios/TEST"+trainedscenario+"/";
	public static final String topkdir = "/topk/";
	public static final String instancedir="inst";
	public static final String instscenario="/scenarios/";
	public static final String observationFiles = "/obs/";  
	public static final String limitedObservationFiles50 = "/obslm50/";
	public static final String limitedObservationFiles75 = "/obslm75/";
	public static final String domainFile = "/domain.pddl";
	public static final String desirableStateFile = "/desirable.txt"; //attacker and user do not need separate files. the state graphs are generated from the observer's point of view
	public static final String criticalStateFile = "/critical.txt";
	public static final String a_initFile = "/inits_a.txt";
	public static final String a_problemFile = "/problem_a.pddl";
	public static final String a_dotFilePrefix = "/dot/full/";
	public static final String a_dotFileLMPrefix = "/dot/lm/";
	public static final String a_dotFileSuffix = "graph_ad_noreverse_";
	public static final String a_outputPath = "/outs/attacker/"; //clean this directory before running. if not graphs will be wrong
	public static final String u_problemFile = "/problem_u.pddl";
	public static final String u_outputPath = "/outs/user/"; 
	public static final String u_dotFilePrefix = "/dot/full/";
	public static final String u_dotFileLMPrefix = "/dot/lm/";
	public static final String u_dotFileSuffix = "graph_ag_noreverse_";
	public static final String u_initFile = "/inits_u.txt";
	public static final String weightedCSV = "/data/weighted/";
	public static final String decisionCSV = "/data/decision/";
	public static final String decisiontreeinput = "/data/inputdecisiontree/";
	public static final String owFile = "/home/sachini/domains/BLOCKS/configs/ow_short.config"; //doesnt have to change with domain
	public static final String lmoutputFull = "/outs/verifiedlm_full.txt";
	public static final String lmoutputShort50 = "/outs/verifiedlm_short50.txt";
	public static final String lmoutputShort75 = "/outs/verifiedlm_short75.txt";
}
