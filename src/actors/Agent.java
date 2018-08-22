package actors;


public abstract class Agent {
	public String domainFile;
	public String desirableStateFile;
	public String problemFile;
	public String outputPath;
	public String criticalStateFile;
	public String dotFilePrefix;
	public String dotFileSuffix;
	
	public Agent(String dom, String des, String pro, String out, String cri, String dotp, String dots){
		this.domainFile = dom;
		this.desirableStateFile = des;
		this.problemFile = pro;
		this.outputPath = out;
		this.criticalStateFile = cri;
		this.dotFilePrefix = dotp;
		this.dotFileSuffix = dots;
	}
	
	public abstract double[] computeMetric();
}
