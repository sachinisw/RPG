package train;

public class TrainDataConfigs {
	public final static String observationFile = "/obs_blocks.txt";
	public final static String a_dotFile = "/graph_ad_noreverse_";
	public final static String u_dotFile = "/graph_ag_noreverse_";
	public final static String u_outputPath = "/outs/user/"; //each scenario has it's own output directory for rpg, connectivity plan files.
	public final static String lmFile = "/outs/verifiedlm.txt";
	//Crticial and desirable states for template training problem generator
	public final static int trainingcases = 20;
	public final static int scenario = 0; //intervention scenario 0 (BAD/TAD), 1(TEA/EAT), ... for the domain
	public final static String domain = "BLOCKS";//BLOCKS,EASYIPC,NAVIGATOR,FERRY
	public final static String prefix = "/home/sachini/domains/"+domain+"/scenarios/"+scenario+"/train"; //base path. must be followed by a number 1, 2...
	public final static String t_CriticalStates = "/t_critical.txt";
	public final static String t_DesirableStates = "/t_desirable.txt";
	public final static String template_domain = "/domain_template.pddl";
	public final static String template_problem = "/probgen_template.pddl";
	public final static String template_init = "/inits_template.txt";
	public final static String datadir = "/data/";
	public final static String cases = "/cases/";
	public final static String dot = "/dot/";
	public final static String out = "/outs/";
	
}
