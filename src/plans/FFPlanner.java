package plans;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class FFPlanner {
	public final static String ffPath = "/home/sachini/domains/Metric-FF-new/ff";

	private String domainfile;
	private String problemfile;

	public FFPlanner(String d, String p) {
		domainfile = d;
		problemfile = p;
	}
	
	public String executeShellCommand(String command){
		StringBuilder sb = new StringBuilder();
		try {
			Process p = Runtime.getRuntime().exec(command);
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";           
			while ((line = reader.readLine())!= null) {
				sb.append(line + "\n");
			}
			p.waitFor();
		} catch (IOException e) {
			System.err.println("FFPlanner:runFF()"+ command+ " " + e.getMessage());
		} catch (InterruptedException e) {
			System.err.println("FFPlanner:runFF()"+ command+ " " + e.getMessage());
		}
		return sb.toString();
	}
	
	public void runFF(int config, String outputpath){
		switch (config){
		case 1: //create plan
			String command = ffPath+ " -o "+ domainfile+ " -f "+ problemfile;
			String plan = executeShellCommand(command);
			writeConsoleOutputtoFile(outputpath +"plan-"+problemfile.substring(problemfile.length()-14,problemfile.length()-5), plan);
			break;
		case 2:	//create relaxed planning graph
			String command_rpg = ffPath +" -o "+ domainfile+ " -f "+ problemfile+" -i 126";
			String rpg = executeShellCommand(command_rpg);
			writeConsoleOutputtoFile(outputpath, rpg);
			break;
		case 3:	//create connectivity graph
			String command_con = ffPath +" -o "+ domainfile+ " -f "+ problemfile+" -i 125";
			String con = executeShellCommand(command_con);
			writeConsoleOutputtoFile(outputpath, con);
			break;
		default:
			System.out.println("UNSUPPORTED COMMAND");
			break;
		}
	}

	public void writeConsoleOutputtoFile(String outfile, String text){
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(outfile, "UTF-8");
			writer.write(text);
			writer.println();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}finally{
			writer.close();
		}
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
