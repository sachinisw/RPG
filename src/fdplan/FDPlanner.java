package fdplan;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

//Generates **one** plan from Fast Downward using lmcut() heuristic given problem and domain.
public class FDPlanner {
	private final static String fdPath = "python /home/sachini/domains/Planners/LAMA/FD/fast-downward.py ";
	private final static String fdConfig = " --search astar(lmcut())";
	private final static String fdoutput = "/home/sachini/eclipse-workspace/IJCAI16/RPG/sas_plan";
	private final static String fdoutputsub = "/home/sachini/eclipse-workspace/IJCAI16/RPG/output.sas";

	private String domainfile;
	private String problemfile;

	public FDPlanner(String d, String p) {
		domainfile = d;
		problemfile = p;
	}


	public void runFDPlanner() {
		String command =  fdPath + " " + domainfile + " " + problemfile + fdConfig;
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
			System.err.println(e.getMessage());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		return lines;
	}

	public void removeOutputFiles() {
		String command = "rm "+ fdoutput + " " + fdoutputsub;
		try {
			Process proc = Runtime.getRuntime().exec(command);
			proc.waitFor();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
		}
	}
	
	//run planner, read output, delete the plan output file.
	public FDPlan getFDPlan(){
		runFDPlanner();
		ArrayList<String> lines = readFile(fdoutput);
		FDPlan fp = new FDPlan();
		fp.setActions(lines);
		removeOutputFiles();
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