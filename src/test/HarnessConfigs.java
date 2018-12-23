package test;

public class HarnessConfigs {
	//TEMPLATES
	public final static String domain = "BLOCKS";//BLOCKS,EASYIPC,NAVIGATOR,FERRY
	public final static String testscenario = "0"; //this is which trained attack scenario it is. e.g. BLOCKS has 4 testable attack scenarios.
	public final static String prefix = "/home/sachini/domains/"+domain+"/scenarios/TEST"+testscenario+"/inst";
	public final static String criticalstates = "/cs.txt";
	public final static String desirablestates = "/ds.txt";
	public final static String domainFile = "/domain.pddl";
	public final static String criticalStateFile = "/cs.txt";
	public final static String initFile = "/inits.txt";
	public final static String template_problem = "/problem_template.pddl";
	public final static String template_domain = "/domain_template.pddl";
	public final static String template_problemgen = "/probgen_template.pddl";
	public final static String problems = "/problems/";
	public final static String problemgen_output = "/scenarios/";
	
	//FILES FOR PROBLEM IN EACH TEST INSTANCE
	public final static String domfilename = "domain.pddl";
	public final static String aprobfilename = "problem_a.pddl";
	public final static String uprobfilename = "problem_u.pddl";
	public final static String ainit = "inits_a.txt";
	public final static String uinit = "inits_u.txt";
	public final static String critical = "critical.txt";
	public final static String desirable = "desirable.txt";
	public final static String arpg = "rpg-problem_a";
	public final static String acon = "connectivity-problem_a";
	public final static String a_dotFileSuffix = "graph_ad_noreverse_";
	public final static String u_dotFileSuffix = "graph_ag_noreverse_";
	public final static String lmFile = "verifiedlm.txt";
	public final static String outdir = "outs";
	public final static String obsdir = "obs"; //full observations
	public final static String datadir = "data";
	public final static String dotdir = "dot";
	public final static String aout = "attacker";
	public final static String obslm = "obslm"; //landmark restricted observations
	public final static String tempplan = "temp";
	public final static int testProblemCount = 20; //blocks(20), easygrid-3x3(20), navigator (20), ferry(20)
	public final static int testInstanceCount = 3; //blocks(3), easygrid(3), navigator(3), ferry(3)
	public final static int testInstanceStart = 1; //usually 0. change to inst number if running for one instance at a time.
}
