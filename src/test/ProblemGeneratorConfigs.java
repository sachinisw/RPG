package test;

public class ProblemGeneratorConfigs {
	//TEMPLATES
	public final static String domain = "BLOCKS";//BLOCKS,EASYIPC,LOGISTICS
	public final static String testscenario = "1";
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
	
	//PROBLEM SPECIFIC FILES
	public final static String domfilename = "domain.pddl";
	public final static String aprobfilename = "problem_a.pddl";
	public final static String uprobfilename = "problem_u.pddl";
	public final static String ainit = "inits_a.txt";
	public final static String uinit = "inits_u.txt";
	public final static String critical = "critical.txt";
	public final static String desirable = "desirable.txt";
	public final static String outdir = "outs";
	public final static String obsdir = "obs";
	public final static String datadir = "data";
	public final static String dotdir = "dot";
	public final static int problemCount = 20;
//	public final static String lmFile = "/outs/verifiedlm.txt";
}
