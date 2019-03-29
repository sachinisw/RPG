package fdplan;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

//Generates **one** plan from Fast Downward using lmcut() heuristic given problem and domain.
public class FDPlanner {
	private final static String fdPath = "/home/sachini/domains/Planners/LAMA/FD/fast-downward.py ";
	private final static String fdConfig = " --search \"astar(lmcut())\"";
	private final static String fdoutpath = "/home/sachini/eclipse-workspace/IJCAI16/RPG/";
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

	public void removeOutputDir() {
		String command = "rm -rf "+ fdoutpath ;
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

	//run planner, read output, delete the plan output file.
	public FDPlan getFDPlan(){
		runFDPlanner();
		ArrayList<String> lines = readFile(fdoutpath);
		FDPlan fp = new FDPlan();
		fp.setActions(lines);
		fp.setLength(lines.size());
		//removeOutputDir();
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
