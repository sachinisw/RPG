package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import con.ConnectivityGraph;
import plans.InterventionPlan;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import landmark.RelaxedPlanningGraphGenerator;

public class ReducedTraceGenerator {
	/**
	 * This is called third. After all files are generated for each of the 20 problems each instance
	 * For each problem scenario in each instance, generate reduced trace considering landmarks
	 * @author sachini
	 *
	 */
	private static final Logger LOGGER = Logger.getLogger(ReducedTraceGenerator.class.getName());
	public static ArrayList<String> readCriticals(String cfile) {
		ArrayList<String> cs = new ArrayList<String>();
		Scanner reader;
		try {
			reader = new Scanner (new File(cfile));
			while(reader.hasNextLine()) {
				cs.add(reader.nextLine());
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return cs;
	}

	public static ArrayList<String> readInits(String infile){
		ArrayList<String> init = new ArrayList<String>();
		Scanner reader;
		try {
			reader = new Scanner (new File(infile));
			while(reader.hasNextLine()) {
				init.add(reader.nextLine());
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return init;
	}

	public static ArrayList<String> getDataFiles(String filedir){		
		ArrayList<String> dataFilePaths = new ArrayList<String>(); 
		try {
			File dir = new File(filedir);
			List<File> files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
			for (File fileItem : files) {
				dataFilePaths.add(fileItem.getCanonicalPath());
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return dataFilePaths;	
	}

	//read all full observation files and add them as plans
	public static ArrayList<InterventionPlan> readRootPlans(String obs) {
		ArrayList<String> files = getDataFiles(obs);
		ArrayList<InterventionPlan> pl = new ArrayList<InterventionPlan>();
		Scanner reader;
		try {
			for (String path : files) {
				ArrayList<String> plansteps = new ArrayList<String>();
				reader = new Scanner (new File(path));
				while(reader.hasNextLine()) {
					plansteps.add(reader.nextLine());
				}
				reader.close();
				InterventionPlan p = new InterventionPlan(plansteps, path);
				pl.add(p);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return pl;
	}

	public static ArrayList<String> readLMData(String lmfile){
		ArrayList<String> lines = new ArrayList<>();
		try {
			Scanner read = new Scanner(new File(lmfile));
			read.nextLine(); //omit the first line
			while(read.hasNextLine()){
				String line = read.nextLine();
				if(line.startsWith(":") || line.trim().isEmpty()){
					break;
				}
				lines.add(line);
			}
			read.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return lines;
	}

	public static HashMap<ArrayList<String>, ArrayList<String>> extractVerifiedLM(ArrayList<String> lmLines){
		HashMap<ArrayList<String> ,ArrayList<String>> lm = new HashMap<>();
		for (String string : lmLines) {
			String parts [] = string.split(":");
			ArrayList<String> keys = new ArrayList<>();
			ArrayList<String> values = new ArrayList<>();
			if (!parts[0].trim().isEmpty()) {
				String keyparts[]=parts[0].trim().substring(2, parts[0].length()-2).split(",");
				for (String k : keyparts) {
					keys.add(k.substring(k.indexOf("("),k.indexOf(")")+1));
				}
			}
			if(parts[1].trim().length()>0){
				String valueparts[]=parts[1].trim().substring(1, parts[1].length()-2).split("}");
				for (String v : valueparts) {
					values.add(v.substring(v.indexOf("("),v.indexOf(")")+1));
				}
			}
			lm.put(keys, values);
		}
		return lm;
	}

	public static ArrayList<ArrayList<String>> getLMBeforeCritical(HashMap<ArrayList<String>,ArrayList<String>> lm, String critical) {
		String temp [] = critical.split("#")[0].split(",");
		ArrayList<String> cs = new ArrayList<String>();
		cs.addAll(Arrays.asList(temp));
		ArrayList<ArrayList<String>> clearedLm = new ArrayList<>();
		Iterator<Entry<ArrayList<String>, ArrayList<String>>> it = lm.entrySet().iterator();
		while(it.hasNext()) {
			Entry<ArrayList<String>, ArrayList<String>> e = it.next();
			if(!e.getKey().equals(cs)) {
				clearedLm.add(e.getKey());
			}
		}
		return clearedLm;
	}

	public static ArrayList<InterventionPlan> reduceTraceByActiveLandmarkPercentage(ArrayList<ArrayList<String>> clm, ArrayList<String> init, 
			String congraphpath, String planpath){
		ArrayList<InterventionPlan> allplans = new ArrayList<>();
		ConnectivityGraph con = new ConnectivityGraph(congraphpath);
		con.readConGraphOutput(congraphpath);
		ArrayList<InterventionPlan> rootplans = readRootPlans(planpath);
		for (InterventionPlan plan : rootplans) {
			int [] limits = new int [2];
			ArrayList<String> steps = plan.getPlanSteps();
			ArrayList<String> currentstate = new ArrayList<String>();
			double[] ratios = new double [steps.size()];
			currentstate.addAll(init);
			int count = 0, halfpoint = 0;
			for (String step : steps) {
				ArrayList<String> stateafterstep = getEffectsOfAction(step, currentstate, con);
				double lmpercentage = getPercentageOfActiveLandmarksInState(stateafterstep, clm);
				ratios[count++] = lmpercentage;
				currentstate.clear();
				currentstate.addAll(stateafterstep);
			}
			for (int a=0; a<ratios.length; a++) {//find the latest observation that has 50<rate<75
				if(HarnessConfigs.activeLMRatio[0]<=ratios[a] && ratios[a]<HarnessConfigs.activeLMRatio[1]) {
					limits[0] = a;
					halfpoint = a;
				}
			}
			for (int a=0; a<ratios.length; a++) {//find the latest observation that has 75<=rate
				if(ratios[a]>=HarnessConfigs.activeLMRatio[1] && a>=halfpoint) {
					limits[1] = a;
				}
			}
			if(limits[1]==0) { //if no such 75% was found start from the largest percentage
				double max= ratios[0];
				for (int a=1; a<ratios.length; a++) {//find the latest observation that has 75<=rate
					if(ratios[a]>=max && a>=halfpoint) {
						limits[1] = a;
					}
				}
			}
			if(limits[0]>limits[1]) { //make sure the 75% mark is at least as far as 50%
				limits[1]=limits[0];
			}
			for (int i=0; i<limits.length; i++) {
				ArrayList<String> lmplan = new ArrayList<>();
				if(limits[i]<steps.size()) { //there is a point in the plan that activates the required limit on landmark percentage. do the decision from this point on.
					for(int j=0; j<steps.size(); j++) {
						if(j<limits[i]) {
							lmplan.add("*"+steps.get(j)); //README:: ignore obs with * when generating feature values for reduced trace
						}else {
							lmplan.add(steps.get(j));
						}
					}
				}
				String planid = plan.getPlanID()+"_"+HarnessConfigs.activeLMRatio[i]*100;
				allplans.add(new InterventionPlan(lmplan,planid.substring(0, planid.length()-2)));
			}
		}

		return allplans;
	}

	public static ArrayList<String> getEffectsOfAction(String action, ArrayList<String> currentstate, ConnectivityGraph con){
		ArrayList<String> updatedState = new ArrayList<String>();
		String ac = action.substring(2, action.length());
		ArrayList<String> adds = con.findStatesAddedByAction(ac);
		ArrayList<String> dels = con.findStatesDeletedByAction(ac);
		updatedState.addAll(currentstate);
		updatedState.removeAll(dels);
		updatedState.addAll(adds);
		return updatedState;
	}

	public static double getPercentageOfActiveLandmarksInState(ArrayList<String> state, ArrayList<ArrayList<String>> clm) {
		int count = 0;
		for (String st : state) {
			for (ArrayList<String> lm : clm) {
				if(listContainsState(lm, st) != null) {	
					count++;
				}
			}
		}
		DecimalFormat decimalFormat = new DecimalFormat("##.##");
		String format = decimalFormat.format(Double.valueOf(count)/Double.valueOf(clm.size()));
		return Double.valueOf(format);
	}

	public static boolean addContainsCriticalLM(ArrayList<String> adds, HashMap<String, ArrayList<String>> clm) {
		Iterator<Entry<String, ArrayList<String>>> it = clm.entrySet().iterator();
		while(it.hasNext()) {
			Entry<String, ArrayList<String>> e = it.next();
			ArrayList<String> l = e.getValue();//lms
			int counter = 0;
			for (String s : l) {
				if(listContainsState(adds, s)!=null) {
					counter++;
				}
			}
			if(counter==l.size()) {
				return true;
			}
		}
		return false;
	}

	private static String listContainsState(List<String> list, String state) {
		for (String string : list) {
			if(string.equalsIgnoreCase(state)) {
				return string;
			}
		}
		return null;
	}

	public static void generateRPGandConnectvity(int start) {
		//FORMAT: /home/sachini/domains/BLOCKS/scenarios/TEST1/inst1/scenarios/0/outs/attacker
		for(int i=start; i<=HarnessConfigs.testInstanceCount; i++) { 
			String pathprefix = HarnessConfigs.prefix+String.valueOf(i)+HarnessConfigs.problemgen_output;
			for (int j = 0; j < HarnessConfigs.testProblemCount; j++) { //attacker
				String aplanspath = pathprefix+String.valueOf(j)+"/"+HarnessConfigs.outdir+"/attacker/";
				String domainpath = pathprefix+String.valueOf(j)+HarnessConfigs.domainFile;
				String problemspath = pathprefix+String.valueOf(j)+"/";
				Planner.runFF(1, domainpath, problemspath+HarnessConfigs.aprobfilename, aplanspath); 
				Planner.runFF(2, domainpath, problemspath+HarnessConfigs.aprobfilename, aplanspath);
				Planner.runFF(3, domainpath, problemspath+HarnessConfigs.aprobfilename, aplanspath);
			}
		}
	}

	public static void generateLandmarks(int start) {
		for(int i=start; i<=HarnessConfigs.testInstanceCount; i++) { 
			String pathprefix = HarnessConfigs.prefix+String.valueOf(i)+HarnessConfigs.problemgen_output;
			for (int j = 0; j < HarnessConfigs.testProblemCount; j++) { //only attacker's paths are needed because landmark considers attacker's problem only
				String rpgpath = pathprefix+String.valueOf(j)+"/"+HarnessConfigs.outdir+"/attacker/"+HarnessConfigs.arpg;
				String conpath = pathprefix+String.valueOf(j)+"/"+HarnessConfigs.outdir+"/attacker/"+HarnessConfigs.acon;
				String csfile = pathprefix+String.valueOf(j)+"/"+HarnessConfigs.critical;
				String initfile = pathprefix+String.valueOf(j)+"/"+HarnessConfigs.ainit;
				String lmoutpath = pathprefix+String.valueOf(j)+"/"+HarnessConfigs.outdir+"/attacker/"+HarnessConfigs.lmFile;
				ArrayList<String> criticals = readCriticals(csfile);
				ArrayList<String> inits = readInits(initfile);
				RelaxedPlanningGraphGenerator rpggen = new RelaxedPlanningGraphGenerator();
				rpggen.runLandmarkGenerator(rpgpath, conpath, criticals, inits, lmoutpath); //using file set for each scenario in each instance, generate landmarks
			}
		}
	}

	public static void writeToFile(ArrayList<InterventionPlan> data, String outputpath){
		PrintWriter writer = null;
		try{
			for (InterventionPlan p : data) {
				String idparts[] = p.getPlanID().split("/");
				String fileid = idparts[idparts.length-1];
				writer = new PrintWriter(outputpath+"/"+fileid.substring(0, fileid.indexOf("_")), "UTF-8");
				for (int i = 0; i < p.getPlanSteps().size(); i++) {
					writer.write(p.getPlanSteps().get(i));
					writer.println();
				}
				writer.close();
			}
		}catch (FileNotFoundException | UnsupportedEncodingException  e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<InterventionPlan> filterPlansByDelayPercentage(int percentage, ArrayList<InterventionPlan> clmactions){
		ArrayList<InterventionPlan> filtered = new ArrayList<InterventionPlan>();
		for (InterventionPlan plan : clmactions) {
			String id = plan.getPlanID();
			if(id.contains("_"+String.valueOf(percentage))) {
				filtered.add(plan);
			}
		}
		return filtered;
	}
	
	public static void generateReducedTrace(int start) {
		generateRPGandConnectvity(start); 		//generate rpg and cons for attacker domain
		generateLandmarks(start); //generate landmarks and write output to lmfile
		for(int i=start; i<=HarnessConfigs.testInstanceCount; i++) { 
			String pathprefix = HarnessConfigs.prefix+String.valueOf(i)+HarnessConfigs.problemgen_output;
			String initspath = HarnessConfigs.prefix+String.valueOf(i)+"/"+HarnessConfigs.initFile;
			String cfile = HarnessConfigs.prefix+String.valueOf(i)+"/"+HarnessConfigs.criticalStateFile;
			ArrayList<String> inits = readInits(initspath);
			ArrayList<String> critical = readCriticals(cfile);
			for (int j = 0; j < HarnessConfigs.testProblemCount; j++) { //only attacker's paths are needed because landmark considers attacker's problem only
				String lmoutpath = pathprefix+String.valueOf(j)+"/"+HarnessConfigs.outdir+"/attacker/"+HarnessConfigs.lmFile;
				String conpath = pathprefix+String.valueOf(j)+"/"+HarnessConfigs.outdir+"/attacker/"+HarnessConfigs.acon;
				String obspath = pathprefix+String.valueOf(j)+"/"+HarnessConfigs.obsdir+"/"; //take this as the root plan
				String obslm50path = pathprefix+String.valueOf(j)+"/"+HarnessConfigs.obslm50;
				String obslm75path = pathprefix+String.valueOf(j)+"/"+HarnessConfigs.obslm75;
				ArrayList<String> lmlines  = readLMData(lmoutpath);
				HashMap<ArrayList<String>, ArrayList<String>> lms = extractVerifiedLM(lmlines);
				ArrayList<ArrayList<String>> criticallm= getLMBeforeCritical(lms,critical.get(j));
				ArrayList<InterventionPlan> clmactions = reduceTraceByActiveLandmarkPercentage(criticallm, inits, conpath, obspath);
				ArrayList<InterventionPlan> delay50 = filterPlansByDelayPercentage(50, clmactions);
				ArrayList<InterventionPlan> delay75 = filterPlansByDelayPercentage(75, clmactions);
				writeToFile(delay50, obslm50path);//write reduced traces to file.
				writeToFile(delay75, obslm75path);//write reduced traces to file.
			}
		}
		LOGGER.log(Level.INFO, "Reduced traces generated");
	}
}
