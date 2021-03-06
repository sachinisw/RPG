package train;

public class SampleConfigs {
	public final static String domain = "DEPOT";//BLOCKS,EASYIPC,NAVIGATOR,FERRY,DEPOT
	public final static String prefix = "/home/sachini/domains/"+domain+"/scenarios/"; //base path. must be followed by a number 1, 2...
	public final static String domainFile = "/domain.pddl";
	public final static String desirableStateFile = "/desirable.txt"; //attacker and user do not need separate files for desirable states. this is because the state graphs are generated from the observer's (3rd agent) point of view
	public final static String criticalStateFile = "/critical.txt";
	public final static String a_initFile = "/init_a.txt";
	public final static String a_problemFile = "/problem_a.pddl";
	public final static String a_dotFile = "/graph_ad_noreverse_";
	public final static String a_outputPath = "/outs/attacker/"; //clean this directory before running. if not graphs will be wrong
	public final static String u_initFile = "/init_u.txt";
	public final static String u_problemFile = "/problem_u.pddl";
	public final static String u_dotFile = "/graph_ag_noreverse_";
	public final static String u_outputPath = "/outs/user/"; //each scenario has it's own output directory for rpg, connectivity plan files.
	public final static String traces = "/home/sachini/domains/"+domain+"/traces/"; //base path. must be followed by a number 1, 2...
	public final static String dot = "/dot/";
	public final static String lmFile = "/outs/verifiedlm.txt";
}
