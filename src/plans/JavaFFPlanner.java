package plans;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;

import log.EventLogger;

//Generates the JavaFF plan from problem and domain.
public class JavaFFPlanner {
	private final static String jff = "java -jar /home/sachini/domains/Planners/JavaFF2/out/artifacts/JavaFF2_jar/JavaFF2.jar ";

	private String domainfile;
	private String problemfile;

	public JavaFFPlanner(String d, String p) {
		domainfile = d;
		problemfile = p;
	}

	public ArrayList<String> runJavaFFPlanner() {
		String command =  jff + " " + domainfile + " " + problemfile;
		ArrayList<String> lines = new ArrayList<String>();
		String line = "";
		try {
			Process proc = Runtime.getRuntime().exec(command);
			proc.waitFor();	
			BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			boolean start = false;
			while((line=input.readLine()) != null){
				if(line.contains("Final plan")) {
					start = true;
				}
				if(start && !line.contains("Final plan")) {
					lines.add(line.substring(line.indexOf("(")));
				}
				if(start && line.startsWith("Final plan length is")) {
					start = false;
				}
			}
			input.close();
		} catch (IOException | InterruptedException e) {
			EventLogger.LOGGER.log(Level.SEVERE, "ERROR runJavaFFPlanner():: " + e.getMessage());
		} 
		return lines;
	}

	//run planner, read output
	public JavaFFPlan getJavaFFPlan(){
		ArrayList<String> lines = runJavaFFPlanner();
		ArrayList<String> cleaned = new ArrayList<>();
		for (String s : lines) {
			cleaned.add(s.substring(s.indexOf("(")+1, s.indexOf(")")).toUpperCase());
		}
		JavaFFPlan jfp = new JavaFFPlan();
		jfp.setActions(cleaned);
		return jfp;
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
