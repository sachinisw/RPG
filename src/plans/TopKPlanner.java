package plans;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import log.EventLogger;

//http://www.cs.toronto.edu/~shirin/Sohrabi-MRC-ECAI-16-Camera-ready.pdf
//Generates a sampling set of K plans for a given problem and domain.
public class TopKPlanner{
	private final static String tkPath = "python /home/sachini/domains/Planners/top-k/";
	private final static String tkoutpath = "/home/sachini/eclipse-workspace/IJCAI16/RPG/found_plans/";
	private String domainfile;
	private String problemfile;
	private int k;

	public TopKPlanner(String d, String p, int numPlans) {
		domainfile = d;
		problemfile = p;
		k = numPlans;
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
		catch (IOException e) {
			EventLogger.LOGGER.log(Level.SEVERE, "ERROR TopK getSASfiles():: " + e.getMessage());
		}
		return sasfiles;
	}

	public void runTopKPlanner() {
		String command = tkPath+ "fast-downward.py " + domainfile + " " + problemfile + " --search kstar(lmcut(),"+k+")";
		try {
			Process proc = Runtime.getRuntime().exec(command);
			proc.waitFor();
		} catch (IOException | InterruptedException e) {
			EventLogger.LOGGER.log(Level.SEVERE, "ERROR TopK runTopKPlanner():: " + e.getMessage());
		}
	}

	public void removeOutputDir() {
		String command = "rm -rf "+ tkoutpath ;
		try {
			Process proc = Runtime.getRuntime().exec(command);
			proc.waitFor();
		} catch (IOException | InterruptedException e) {
			EventLogger.LOGGER.log(Level.SEVERE, "ERROR TopK removeOutputDir():: " + e.getMessage());
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
		} catch (IOException e) {
			EventLogger.LOGGER.log(Level.SEVERE, "ERROR TopK readFile():: " + e.getMessage());
		} 
		return lines;
	}

	//run planner, read output, delete the output folder.
	public ArrayList<SASPlan> getPlans(){
		ArrayList<SASPlan> plans = new ArrayList<>();
		runTopKPlanner();
		ArrayList<String> paths = getSASfiles(tkoutpath);
		for (String p : paths) {
			ArrayList<String> lines = readFile(p);
			SASPlan sp = new SASPlan();
			sp.setActions(lines);
			plans.add(sp);
		}
		removeOutputDir();
		return plans;
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

	public int getK() {
		return k;
	}

	public void setK(int k) {
		this.k = k;
	}
}
