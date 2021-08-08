package actors;


public abstract class Agent {
	public String domainFile;
	public String domain;
	public String desirableStateFile;
	public String problemFile;
	public String outputPath;
	public String criticalStateFile;
	public String dotFilePrefix;
	public String dotFileSuffix;
	
	public Agent(String domain, String domfile, String des, String pro, String out, String cri, String dotp, String dots){
		this.domainFile = domfile;
		this.domain = domain;
		this.desirableStateFile = des;
		this.problemFile = pro;
		this.outputPath = out;
		this.criticalStateFile = cri;
		this.dotFilePrefix = dotp;
		this.dotFileSuffix = dots;
	}
	
	public Agent(String domain, String domfile, String des, String pro, String out, String cri){
		this.domainFile = domfile;
		this.domain = domain;
		this.desirableStateFile = des;
		this.problemFile = pro;
		this.outputPath = out;
		this.criticalStateFile = cri;
		this.dotFilePrefix = "";
		this.dotFileSuffix = "";
	}	
}
