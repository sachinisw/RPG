package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import con.ConnectivityGraph;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

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

	public static ArrayList<String> readRootPlan(String obs) {
		ArrayList<String> pl = new ArrayList<String>();
		Scanner reader;
		try {
			reader = new Scanner (new File(obs));
			while(reader.hasNextLine()) {
				pl.add(reader.nextLine());
			}
			reader.close();
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

	public static ArrayList<ArrayList<String>> extractLM(ArrayList<String> lmLines){
		ArrayList<ArrayList<String>> lm = new ArrayList<ArrayList<String>>();
		for (String string : lmLines) {
			ArrayList<String> data = new ArrayList<>();
			String parts [] = string.split(":");
			String key="",value="";
			ArrayList<String> keys = new ArrayList<>();
			ArrayList<String> values = new ArrayList<>();
			if (!parts[0].trim().isEmpty()) {
				key = parts[0].trim().substring(2, parts[0].length()-2);
				String keyparts[]=key.split(",");
				for (String k : keyparts) {
					keys.add(k.substring(k.indexOf("("),k.indexOf(")")+1));
				}
			}
			if(parts[1].trim().length()>0){
				value = parts[1].trim().substring(1, parts[1].length()-2);
				String valueparts[]=value.split("}");
				for (String v : valueparts) {
					values.add(v.substring(v.indexOf("("),v.indexOf(")")+1));
				}
			}
			data.addAll(keys);
			data.addAll(values);
			lm.add(data);
		}
		//				for (ArrayList<String> arrayList : lm) {
		//					System.out.println(arrayList);
		//				}
		return lm;
	}

	public static HashMap<String, ArrayList<String>> extractLMBeforeUndesirableState(ArrayList<ArrayList<String>> lm) {
		HashMap<String, ArrayList<String>> causes = new HashMap<>();
		ArrayList<String> goal = lm.get(0);
		for(int i=1; i<lm.size(); i++){
			List<String> data = lm.get(i).subList(1, lm.get(i).size());
			for (String c : goal) {
				String s = listContainsState(data, c);
				if(s!=null) {
					if(!causes.containsKey(c)) {
						ArrayList<String> val= new ArrayList<String>();
						val.add(lm.get(i).get(0));
						causes.put(c, val);
					}else {
						ArrayList<String> current = causes.get(c);
						current.add(lm.get(i).get(0));
					}
				}
			}
		}
		//		System.out.println("done");
		//		Iterator<Entry<String, ArrayList<String>>> it = causes.entrySet().iterator();
		//		while(it.hasNext()) {
		//			Entry<String, ArrayList<String>> e = it.next();
		//			System.out.println(e.getKey()+"---"+e.getValue());
		//		}
		return causes;
	}

	public static ArrayList<String> actionsAddingCriticalLandmark(HashMap<String, ArrayList<String>> clm, String congraphpath, String planpath){
		ArrayList<String> actions = new ArrayList<>();
		ConnectivityGraph con = new ConnectivityGraph(congraphpath);
		con.readConGraphOutput(congraphpath);
		ArrayList<String> rootplan = readRootPlan(planpath);
		int startpoint = 0;
		for (String step : rootplan) {
			ArrayList<String> ads = con.findStatesAddedByAction(step.substring(2));
			startpoint++;
			if(addContainsCriticalLM(ads, clm)) {
				break;
			}
		}
		for(int i=0; i<rootplan.size(); i++) {
			if(i<startpoint-1) {
				actions.add("*"+rootplan.get(i)); //README:: ignore obs with * when generating feature values for reduced trace
			}else {
				actions.add(rootplan.get(i));
			}
		}
		return actions;
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

	public static void generateRPGandConnectvity() {
		//FORMAT: /home/sachini/domains/BLOCKS/scenarios/TEST1/inst1/scenarios/0/outs/attacker
		for(int i=1; i<=TestGeneratorConfigs.testInstanceCount; i++) { 
			String pathprefix = TestGeneratorConfigs.prefix+String.valueOf(i)+TestGeneratorConfigs.problemgen_output;
			for (int j = 0; j < TestGeneratorConfigs.testProblemCount; j++) { //attacker
				String aplanspath = pathprefix+String.valueOf(j)+"/"+TestGeneratorConfigs.outdir+"/attacker/";
				String uplanspath = pathprefix+String.valueOf(j)+"/"+TestGeneratorConfigs.outdir+"/user/";
				String domainpath = pathprefix+String.valueOf(j)+TestGeneratorConfigs.domainFile;
				String problemspath = pathprefix+String.valueOf(j)+"/";
				Planner.runFF(1, domainpath, problemspath+TestGeneratorConfigs.aprobfilename, aplanspath); 
				Planner.runFF(2, domainpath, problemspath+TestGeneratorConfigs.aprobfilename, aplanspath);
				Planner.runFF(3, domainpath, problemspath+TestGeneratorConfigs.aprobfilename, aplanspath);
				Planner.runFF(1, domainpath, problemspath+TestGeneratorConfigs.uprobfilename, uplanspath); 
				Planner.runFF(2, domainpath, problemspath+TestGeneratorConfigs.uprobfilename, uplanspath);
				Planner.runFF(3, domainpath, problemspath+TestGeneratorConfigs.uprobfilename, uplanspath);
			}
		}
	}

	public static void generateLandmarks() {
		for(int i=1; i<=TestGeneratorConfigs.testInstanceCount; i++) { 
			String pathprefix = TestGeneratorConfigs.prefix+String.valueOf(i)+TestGeneratorConfigs.problemgen_output;
			for (int j = 0; j < TestGeneratorConfigs.testProblemCount; j++) { //only attacker's paths are needed because landmark considers attacker's problem only
				String rpgpath = pathprefix+String.valueOf(j)+"/"+TestGeneratorConfigs.outdir+"/attacker/"+TestGeneratorConfigs.arpg;
				String conpath = pathprefix+String.valueOf(j)+"/"+TestGeneratorConfigs.outdir+"/attacker/"+TestGeneratorConfigs.acon;
				String csfile = pathprefix+String.valueOf(j)+"/"+TestGeneratorConfigs.critical;
				String initfile = pathprefix+String.valueOf(j)+"/"+TestGeneratorConfigs.ainit;
				String lmoutpath = pathprefix+String.valueOf(j)+"/"+TestGeneratorConfigs.outdir+"/attacker/"+TestGeneratorConfigs.lmFile;
				ArrayList<String> criticals = readCriticals(csfile);
				ArrayList<String> inits = readInits(initfile);
				RelaxedPlanningGraphGenerator rpggen = new RelaxedPlanningGraphGenerator();
				rpggen.runLandmarkGenerator(rpgpath, conpath, criticals, inits, lmoutpath); //using file set for each scenario in each instance, generate landmarks
			}
		}
	}

	public static void writeToFile(ArrayList<String> data, String outputpath, String filename){
		PrintWriter writer = null;
		try{
			writer = new PrintWriter(outputpath+"/"+filename, "UTF-8");
			for (int i = 0; i < data.size(); i++) {
				writer.write(data.get(i));
				writer.println();
			}
		}catch (FileNotFoundException | UnsupportedEncodingException  e) {
			e.printStackTrace();
		}finally{
			writer.close();
		}
	}

	public static void generateReducedTrace() {
		generateRPGandConnectvity(); 		//generate rpg and cons
		generateLandmarks(); //generate landmarks and write output to lmfile
		for(int i=1; i<=TestGeneratorConfigs.testInstanceCount; i++) { 
			String pathprefix = TestGeneratorConfigs.prefix+String.valueOf(i)+TestGeneratorConfigs.problemgen_output;
			for (int j = 0; j < TestGeneratorConfigs.testProblemCount; j++) { //only attacker's paths are needed because landmark considers attacker's problem only
				String lmoutpath = pathprefix+String.valueOf(j)+"/"+TestGeneratorConfigs.outdir+"/attacker/"+TestGeneratorConfigs.lmFile;
				String conpath = pathprefix+String.valueOf(j)+"/"+TestGeneratorConfigs.outdir+"/attacker/"+TestGeneratorConfigs.acon;
				String obspath = pathprefix+String.valueOf(j)+"/"+TestGeneratorConfigs.obsdir+"/"+String.valueOf(j); //take this as the root plan
				String obslmpath = pathprefix+String.valueOf(j)+"/"+TestGeneratorConfigs.obslm;
				ArrayList<String> lmlines  = readLMData(lmoutpath);
				ArrayList<ArrayList<String>> lms = extractLM(lmlines);
				HashMap<String, ArrayList<String>> criticallm= extractLMBeforeUndesirableState(lms);
				ArrayList<String> clmactions = actionsAddingCriticalLandmark(criticallm, conpath, obspath);	//after this find the action that first adds this landmark. remove actions before that action from the trace
				writeToFile(clmactions, obslmpath, String.valueOf(j));//write trace to file
			}
		}
	}

	public static void main(String[] args) {
		generateReducedTrace();
		LOGGER.log(Level.INFO, "Reduced traces generated");
	}
}
