package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import igplan.Plan;
import igplan.PlanExtractor;

public class Planner {
	public final static String ffPath = "/home/sachini/domains/Metric-FF-new/ff";

	public static String executeShellCommand(String command){
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
			System.err.println("Planner:runFF()"+ command+ " " + e.getMessage());
		} catch (InterruptedException e) {
			System.err.println("Planner:runFF()"+ command+ " " + e.getMessage());
		}
		return sb.toString();
	}

	public static void runFF(int config, String domainpath, String probfilename, String outputPath){
		switch (config){
		case 1: //create plan
			String command = ffPath+ " -o "+ domainpath+ " -f "+ probfilename;
			String plan = executeShellCommand(command);
			writeConsoleOutputtoFile(outputPath +"plan-"+probfilename.substring(probfilename.length()-14,probfilename.length()-5), plan);
			break;
		case 2:	//create relaxed planning graph
			String command_rpg = ffPath +" -o "+ domainpath+ " -f "+ probfilename+" -i 126";
			String rpg = executeShellCommand(command_rpg);
			writeConsoleOutputtoFile(outputPath +"rpg-"+probfilename.substring(probfilename.length()-14,probfilename.length()-5), rpg);
			break;
		case 3:	//create connectivity graph
			String command_con = ffPath +" -o "+ domainpath+ " -f "+ probfilename+" -i 125";
			String con = executeShellCommand(command_con);
			writeConsoleOutputtoFile(outputPath+"connectivity-"+probfilename.substring(probfilename.length()-14,probfilename.length()-5), con);
			break;
		default:
			System.err.println("UNSUPPORTED COMMAND");
			break;
		}
	}
	
	public static void writeConsoleOutputtoFile(String outfile, String text){
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
	
	public static ArrayList<Plan> readPlans(String planpath){
		ArrayList<String> planFiles = getPlanFiles(planpath);
		PlanExtractor px = new PlanExtractor();
		for(int i=0; i<planFiles.size(); i++){
			px.readFFPlanOutput(planFiles.get(i));
		}
		return px.getPlanSet();
	}
	
	public static ArrayList<String> getPlanFiles(String planpath){		
		ArrayList<String> planFilePaths = new ArrayList<String>();
		try {
			File dir = new File(planpath);
			List<File> files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
			for (File fileItem : files) {
				if(fileItem.getName().contains("plan")){
					planFilePaths.add(fileItem.getCanonicalPath());
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return planFilePaths;	
	}
}
