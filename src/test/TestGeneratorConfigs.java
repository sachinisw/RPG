package test;

public class TestGeneratorConfigs {
	//TEMPLATES
	public final static String domain = "BLOCKS";//BLOCKS,EASYIPC,LOGISTICS
	public final static String testscenario = "1"; //this is which trained attack scenario it is. e.g. BLOCKS has 4 testable attack scenarios.
	public final static String prefix = "/home/sachini/domains/"+domain+"/scenarios/TEST"+testscenario+"/inst";
	public final static String criticalstates = "/cs.txt";
	public final static String desirablestates = "/ds.txt";
	public final static String domainFile = "/domain.pddl";
	public final static String criticalStateFile = "/cs.txt";
	public final static String initFile = "/inits.txt";
	public final static String template_problem = "/problem_template.pddl";
	public final static String template_domain = "/domain_template.pddl";
	public final static String template_problemgen = "/probgen_template.pddl";
	public final static String traces = "/traces/"; //base path. must be followed by a number 1, 2...
	public final static String problems = "/problems/";
	public final static String plans = "/plans/";
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
	public final static String lmFile = "verifiedlm.txt";
	public final static String outdir = "outs";
	public final static String obsdir = "obs"; //full observations
	public final static String datadir = "data";
	public final static String dotdir = "dot";
	public final static String obslm = "obslm"; //landmark restricted observations
	public final static int testProblemCount = 20;
	public final static int testInstanceCount = 3;
}