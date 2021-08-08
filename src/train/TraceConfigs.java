package train;

public class TraceConfigs {
	public final static String a_outputPath = "attacker/"; 
	public final static String a_initFile = "/init_a.txt";
	public final static String a_problemFile = "/problem_a.pddl";
	public final static String desirableStateFile = "/desirable.txt"; //attacker and user do not need separate files for desirable states. this is because the state graphs are generated from the observer's (3rd agent) point of view
	public final static String criticalStateFile = "/critical.txt";
	public final static String domainFile = "/domain.pddl";
	public final static String a_dotFile = "graph_ad_noreverse_";
	public final static String u_dotFile = "graph_ag_noreverse_";
	public final static String u_outputPath = "user/"; //each scenario has it's own output directory for rpg, connectivity plan files.
	public final static String lmFile = "verifiedlm.txt";
	
	//Critical and desirable states for template training problem generator
	public final static int trainingcases = 20;
	public final static int scenario = 1; //intervention scenario 0 (BAD/TAD), 1(CUT/CUP), 2(TEA/EAT), ... for the domain
	public final static String domain = "BLOCKS";//BLOCKS,EASYIPC,NAVIGATOR,FERRY,LOGISTICS,PAG
	public final static String prefix = "/home/sachini/oldhp/sachini/domains/"+domain+"/scenarios/"+scenario+"/train"; //base path. must be followed by a number 1, 2...
	public final static String t_CriticalStates = "/t_critical.txt";
	public final static String t_DesirableStates = "/t_desirable.txt";
	public final static String template_domain = "/domain_template.pddl";
	public final static String template_problem = "/probgen_template.pddl";
	public final static String template_init = "/inits_template.txt";
	public final static String datadir = "/data/";
	public final static String cases = "/cases/";
	public final static String dot = "/dot/";
	public final static String out = "/outs/";
	public final static String obs = "/obs/";
}
