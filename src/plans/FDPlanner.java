package plans;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import log.EventLogger;

//Generates **one** plan from Fast Downward using lazy-greedy heuristic given problem and domain.
public class FDPlanner {
	private final static String fdPath = "python /home/sachini/domains/Planners/LAMA/FD/fast-downward.py ";
	private final static String fdConfigGreedy = " --evaluator hff=ff() --evaluator hcea=cea() --search lazy_greedy([hff,hcea],preferred=[hff,hcea])";
	private final static String fdoutput = "/home/sachini/eclipse-workspace/IJCAI16/RPG/sas_plan";
	private final static String fdoutputsub = "/home/sachini/eclipse-workspace/IJCAI16/RPG/output.sas";

	private String domainfile;
	private String problemfile;

	public FDPlanner(String d, String p) {
		domainfile = d;
		problemfile = p;
	}


	public void runFDPlanner() {
		String command =  fdPath + " " + domainfile + " " + problemfile + fdConfigGreedy;
		try {
			Process proc = Runtime.getRuntime().exec(command);
			proc.waitFor();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
		}
	}

	public ArrayList<String> readFile(String path){
		FileReader fileReader;
		ArrayList<String> lines = new ArrayList<String>();
		try {
			fileReader = new FileReader(path);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String outstr = "";
			while((outstr = bufferedReader.readLine()) != null) {
				if(!outstr.contains(";")){
					lines.add(outstr.substring(1, outstr.length()-1).toUpperCase());
				}
			}
			bufferedReader.close();
		} catch (FileNotFoundException e) {
			EventLogger.LOGGER.log(Level.SEVERE, "ERROR readFile():: " + e.getMessage());
		} catch (IOException e) {
			EventLogger.LOGGER.log(Level.SEVERE, "ERROR readFile():: " + e.getMessage());
		}
		return lines;
	}

	public void removeOutputFiles() {
		String command = "rm "+ fdoutput + " " + fdoutputsub;
		try {
			Process proc = Runtime.getRuntime().exec(command);
			proc.waitFor();
		} catch (IOException e) {
			EventLogger.LOGGER.log(Level.SEVERE, "ERROR removeOutputFiles():: " + e.getMessage());
		} catch (InterruptedException e) {
			EventLogger.LOGGER.log(Level.SEVERE, "ERROR removeOutputFiles():: " + e.getMessage());
		}
	}
	
	//run planner, read output
	public FDPlan getFDPlan(){
		runFDPlanner();
		ArrayList<String> lines = readFile(fdoutput);
		FDPlan fp = new FDPlan();
		fp.setActions(lines);
		return fp;
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
