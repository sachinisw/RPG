package fdplan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

//Generates a plan from Fast Downward using lmcut() heuristic given problem and domain.
public class FDPlanner {
	private final static String fdPath = "./fast-downward.py ";
	private final static String fdConfig = " --search \"astar(lmcut())\"";
	private final static String fdoutpath = "/home/sachini/eclipse-workspace/IJCAI16/RPG/";
	private String domainfile;
	private String problemfile;

	public FDPlanner(String d, String p) {
		domainfile = d;
		problemfile = p;
	}

	public ArrayList<String> getSASfiles(String path) {
		ArrayList<String> sasfiles = new ArrayList<String>();
		try {
			File dir = new File(path);
			List<File> files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
			for (File fileItem : files) {
				if(fileItem.getName().contains("sas_")){
					sasfiles.add(fileItem.getCanonicalPath());
				}
			}
		}
		catch (FileNotFoundException e) {
			System.err.println(e.getMessage());		
		} catch (IOException e) {
			System.err.println(e.getMessage());	
		}
		return sasfiles;
	}

	public void runTopKPlanner() {
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

	//run planner, read output, delete the output folder.
	ArrayList<FDPlan> getPlans(){
		ArrayList<FDPlan> plans = new ArrayList<>();
		runTopKPlanner();
		ArrayList<String> paths = getSASfiles(fdoutpath);
		for (String p : paths) {
			ArrayList<String> lines = readFile(p);
			FDPlan fp = new FDPlan();
			fp.setActions(lines);
			fp.setLength(lines.size());
			plans.add(fp);
		}
		removeOutputDir();
		return plans;
	}

	public static void main(String[] args) {
		String d = "/home/sachini/domains/BLOCKS/scenarios/0/domain.pddl";
		String p = "/home/sachini/domains/BLOCKS/scenarios/0/problem_a.pddl";
		FDPlanner fd = new FDPlanner(d, p);
		ArrayList<FDPlan> sps = fd.getPlans();
		System.out.println(sps);
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
