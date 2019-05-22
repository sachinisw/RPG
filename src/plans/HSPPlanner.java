package plans;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;

import log.EventLogger;

//Generates the optimal plan from HSP-F from problem and domain.
public class HSPPlanner {
	private final static String hsppath = "/home/sachini/domains/Planners/hsps/hsps/hsp_f -cost ";

	private String domainfile;
	private String problemfile;

	public HSPPlanner(String d, String p) {
		domainfile = d;
		problemfile = p;
	}

	public ArrayList<String> runHSPPlanner() {
		String command =  hsppath + " " + domainfile + " " + problemfile;
		ArrayList<String> lines = new ArrayList<String>();
		String line = "";
		boolean start = false;
		try {
			Process proc = Runtime.getRuntime().exec(command);
			proc.waitFor();	
			BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			while((line=input.readLine()) != null){
				if(line.startsWith("plan #")) {
					start = true;
				}
				if(start && line.startsWith(" [")) {
					lines.add(line.substring(line.indexOf("("),line.indexOf(")")+1));
				}
			}
			input.close();
		} catch (IOException | InterruptedException e) {
			EventLogger.LOGGER.log(Level.SEVERE, "ERROR runHSPPlanner():: " + e.getMessage());
		} 
		return lines;
	}

	//run planner, read output
	public HSPFPlan getHSPPlan(){
		ArrayList<String> lines = runHSPPlanner();
		ArrayList<String> cleaned = new ArrayList<>();
		for (String s : lines) {
			cleaned.add(s.substring(s.indexOf("(")+1, s.indexOf(")")).toUpperCase()); //lose the paranthesis around actions
		}
		HSPFPlan hsp = new HSPFPlan();
		hsp.setActions(cleaned);
		return hsp;
	}

	public String getDomainfile() {
		return domainfile;
	}

	public void setDomainfile(String domainfile) {
		this.domainfile = domainfile;
	}

	public String getProblemfile() {
		return problemfile;
	}

	public void setProblemfile(String problemfile) {
		this.problemfile = problemfile;
	}
}
