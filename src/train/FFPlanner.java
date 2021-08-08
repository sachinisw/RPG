package train;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class FFPlanner {
	private String domain;
	private String problem;
	private String outputpath;
	public final static String ffPath = "/home/sachini/oldhp/sachini/domains/Planners/FF/ff ";

	public FFPlanner(String d, String p, String o) {
		this.setDomain(d);
		this.setProblem(p);
		this.setOutputpath(o);
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
			System.err.println("TraceGenerator:runFF()"+ command+ " " + e.getMessage());
		} catch (InterruptedException e) {
			System.err.println("TraceGenerator:runFF()"+ command+ " " + e.getMessage());
		}
		return sb.toString();
	}

	public void runPlanner(String domain, String problem){
		runFF(1, domain, problem); //Plan
		runFF(2, domain, domain); //RPG
		runFF(3, domain, domain); //connectivity
	}
	
	public void runFF(int config, String domainpath, String probfilename){
		switch (config){
		case 1: //create plan
			String command = ffPath+ " -o "+ domainpath+ " -f "+ probfilename;
			String plan = executeShellCommand(command);
			writeConsoleOutputtoFile(outputpath +"plan-"+probfilename.substring(probfilename.length()-14,probfilename.length()-5), plan);
			break;
		case 2:	//create relaxed planning graph
			String command_rpg = ffPath +" -o "+ domainpath+ " -f "+ probfilename+" -i 126";
			String rpg = executeShellCommand(command_rpg);
			writeConsoleOutputtoFile(outputpath +"rpg-"+probfilename.substring(probfilename.length()-14,probfilename.length()-5), rpg);
			break;
		case 3:	//create connectivity graph
			String command_con = ffPath +" -o "+ domainpath+ " -f "+ probfilename+" -i 125";
			String con = executeShellCommand(command_con);
			writeConsoleOutputtoFile(outputpath+"connectivity-"+probfilename.substring(probfilename.length()-14,probfilename.length()-5), con);
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
	
	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getProblem() {
		return problem;
	}

	public void setProblem(String problem) {
		this.problem = problem;
	}

	public String getOutputpath() {
		return outputpath;
	}

	public void setOutputpath(String outputpath) {
		this.outputpath = outputpath;
	}
}
