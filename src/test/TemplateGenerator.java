package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import graph.ActionEdge;
import graph.StateGraph;
import graph.StateVertex;
import plan.Plan;
import test.ProblemGeneratorConfigs;
/**
 * Generate observation traces for testing the trained model of the decision tree
 * @author sachini
 *
 */
public class TemplateGenerator {

	public final static int filterLimit = 80;
	public final static double selectProbability = 0.80;

//	//generates the trace with flagged observations to train the classifier
//	private static ArrayList<ArrayList<String>> generateTrace(StateGraph attacker, StateGraph user){ 
//		ArrayList<ArrayList<String>> trace = new ArrayList<ArrayList<String>>();
//		ArrayList<ArrayList<StateVertex>> at = attacker.getAllPathsFromRoot();
//		ArrayList<ArrayList<StateVertex>> undesirable = attacker.getUndesirablePaths(at,ProblemGeneratorConfigs.domain);
//		for(int i=0; i<at.size(); i++){
//			ArrayList<StateVertex> list = at.get(i);
//			ArrayList<String> trc = new ArrayList<String>();
//			for(int j=0; j<list.size()-1; j++){
//				ArrayList<ActionEdge> actions = attacker.findEdgeForStateTransition(list.get(j), list.get(j+1));
//				for (ActionEdge actionEdge : actions) {
//					if(edgeInUndesirablePath(actionEdge, undesirable)){ //find trouble action. causing critical state. must be flagged
//						//Y for steps until critical state N for steps after critical state
//						if(edgeTriggersCriticalState(actionEdge, attacker.getCritical().getCriticalState())){ 
//							trc.add("N:"+actionEdge.getAction());
//						}else{
//							trc.add("Y:"+actionEdge.getAction());
//						}
//					}else{
//						trc.add("N:"+actionEdge.getAction());
//					}
//				}
//			}
//			System.out.println(trc);
//			trace.add(trc);
//		}
//		System.out.println("*******************************************************");
//		System.out.println(trace);
//		System.out.println("*****************Filtering*****************************");
//		//		System.out.println(selectTracesRandomly(trace));
//		return selectTracesRandomly(trace);
//		//		return trace;
//	}

	//generates the trace with unflagged observations to test the trained classifier
	public static ArrayList<ArrayList<String>> generateTestingTrace(StateGraph attacker, StateGraph user, HashMap<String, ArrayList<String>> lm){ 
		ArrayList<ArrayList<String>> trace = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<StateVertex>> at = attacker.getAllPathsFromRoot();
		for(int i=0; i<at.size(); i++){
			ArrayList<StateVertex> path = at.get(i);
			ArrayList<String> trc = new ArrayList<String>();
			for(int j=0; j<path.size()-1; j++){
				ArrayList<ActionEdge> actions = attacker.findEdgeForStateTransition(path.get(j), path.get(j+1));
				for (ActionEdge actionEdge : actions) {
					if(actionAddsLandmark(actionEdge, lm)) {
						trc.add("?:"+actionEdge.getAction());
					}
				}
			}
			//				System.out.println(trc);
			trace.add(trc);
		}
		//			System.out.println("*******************************************************");
		//			System.out.println(trace);
		//			System.out.println("*****************Filtering*****************************");
		//		System.out.println(selectTracesRandomly(trace));
		return selectTracesRandomly(trace);
		//		return trace;
	}

	private static boolean actionAddsLandmark(ActionEdge edge, HashMap<String, ArrayList<String>> lm) {
		Iterator<Entry<String, ArrayList<String>>> itr = lm.entrySet().iterator();
		return false;
	}
	
	private static ArrayList<ArrayList<String>> selectTracesRandomly(ArrayList<ArrayList<String>> unfiltered){
		ArrayList<ArrayList<String>> trace = new ArrayList<ArrayList<String>>();
		if(unfiltered.size() > filterLimit){
			for (int i = 0; i < unfiltered.size(); i++) { //select all Y
				ArrayList<String> tr = unfiltered.get(i);
				for (String s : tr) {
					if(s.contains("Y:")){
						trace.add(tr);
						break;
					}
				}
			}
			for (int i = 0; i < unfiltered.size() && trace.size()<=100; i++) {
				ArrayList<String> tr = unfiltered.get(i);
				if(listContainsAllNo(tr) && selectCurrentTrace(tr)){ //filter and select all N traces
					trace.add(tr);
				}
			}
		}else{
			trace.addAll(unfiltered);
		}
		return trace;
	}

	private static boolean listContainsAllNo(ArrayList<String> list){
		int count = 0;
		for (String string : list) {
			if(string.contains("N:")){
				count++;
			}
		}
		return count==list.size();
	}

	private static double randDouble(int min, int max) {
		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}

	private static boolean selectCurrentTrace(ArrayList<String> tr){
		if(randDouble(0, 1)> selectProbability){
			return true;
		}
		return false;
	}

//	private static boolean edgeInUndesirablePath(ActionEdge e, ArrayList<ArrayList<StateVertex>> undesirable){
//		for (ArrayList<StateVertex> undesirablePath : undesirable) {
//			for (StateVertex undesirableState : undesirablePath) {
//				if(e.getTo().isEqual(undesirableState)){
//					return true;
//				}
//			}
//		}
//		return false;
//	}

//	private static boolean edgeTriggersCriticalState(ActionEdge e, ArrayList<String> criticalstate) {
//		if(e.getTo().containsCriticalState(criticalstate)){
//			return true;
//		}
//		return false;
//	}

	private static void writeTracesToFile(ArrayList<ArrayList<String>> traceset, String tracepath){
		PrintWriter writer = null;
		for (int i = 0; i < traceset.size(); i++) {
			ArrayList<String> tr = traceset.get(i);
			try{
				writer = new PrintWriter(tracepath+"/"+i, "UTF-8");
				for (String string : tr) {
					writer.write(string);
					writer.println();
				}
			}catch (FileNotFoundException | UnsupportedEncodingException  e) {
				e.printStackTrace();
			}finally{
				writer.close();
			}
		}
	}

	private static ArrayList<String> readLMData(String lmfile){
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

	private static ArrayList<ArrayList<String>> extractLM(ArrayList<String> lmLines){
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

	private static HashMap<String, ArrayList<String>> extractLMBeforeUndesirableState(ArrayList<ArrayList<String>> lm) {
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
		System.out.println("done");
		Iterator<Entry<String, ArrayList<String>>> it = causes.entrySet().iterator();
		while(it.hasNext()) {
			Entry<String, ArrayList<String>> e = it.next();
			System.out.println(e.getKey()+"---"+e.getValue());
		}
		return causes;
	}

	private static String listContainsState(List<String> list, String state) {
		for (String string : list) {
			if(string.equalsIgnoreCase(state)) {
				return string;
			}
		}
		return null;
	}

	private static ArrayList<String> readCriticals(String cspath){
		ArrayList<String> cs = new ArrayList<String>();
		Scanner reader;
		try {
			reader = new Scanner (new File(cspath));
			while(reader.hasNextLine()) {
				String state = reader.nextLine();
				cs.add(state.substring(0, state.indexOf("-")));
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return cs;
	}
	
	private static ArrayList<String> readTemplateProblem(String templatepath){
		ArrayList<String> lines = new ArrayList<String>();
		Scanner reader;
		try {
			reader = new Scanner (new File(templatepath));
			while(reader.hasNextLine()) {
				lines.add(reader.nextLine());
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return lines;
	}
	
	private static void generateProblems(ArrayList<String> goalstates, String templatepath, String problemsout) {
		ArrayList<String> problem = readTemplateProblem(templatepath);
		ArrayList<ArrayList<String>> problems = new ArrayList<>();
		for (String cs : goalstates) { //replace tag <CRITICAL> with cs
			ArrayList<String> current = new ArrayList<String>();
			problem.set(problem.size()-3, cs);
			current.addAll(problem);
			problems.add(current);
		}
		writeProblemsToFiles(problems, problemsout);
	}
	
	public static void writeProblemsToFiles(ArrayList<ArrayList<String>> problems, String problemsout){
		PrintWriter writer = null;
		for (int i = 0; i < problems.size(); i++) {
			ArrayList<String> prob = problems.get(i);
			try{
				writer = new PrintWriter(problemsout+"problem_"+i+".pddl", "UTF-8");
				for (String string : prob) {
					writer.write(string);
					writer.println();
				}
			}catch (FileNotFoundException | UnsupportedEncodingException  e) {
				e.printStackTrace();
			}finally{
				writer.close();
			}
		}
	}
	
	public static void generatePlans(String problemspath, String domainpath, String planspath, String tracepath) {
		for (int i = 0; i < ProblemGeneratorConfigs.problemCount; i++) {
			Planner.runFF(1, domainpath, problemspath+"problem_"+i+".pddl", planspath); //just create a plan
		}
		ArrayList<Plan> plans = Planner.readPlans(planspath);
		ArrayList<ArrayList<String>> traceset = new ArrayList<>();
		for (Plan plan : plans) {
			ArrayList<String> steps = plan.getPlanSteps();
			int index=0;
			for (String step : steps) {
				steps.set(index, "?:"+step.substring(step.indexOf(":")+2,step.length()));
				index++;
			}
			traceset.add(steps);
		}
		writeTracesToFile(traceset, tracepath);
	}
	
	//generates common templates and full trace set (all actions including) for each test instance {1,2,3}
	public static void generateTemplatesAndFullTrace(){ 
		for(int instance=1; instance<=3; instance++){
			if(instance==3){
				String domainFile = ProblemGeneratorConfigs.prefix+instance+ProblemGeneratorConfigs.domainFile;
				String problemFile = ProblemGeneratorConfigs.prefix+instance+ProblemGeneratorConfigs.template_problem;
				String criticalStateFile = ProblemGeneratorConfigs.prefix+instance+ProblemGeneratorConfigs.criticalStateFile;
				String tracepath = ProblemGeneratorConfigs.prefix+instance+ProblemGeneratorConfigs.traces;
				String problemspath = ProblemGeneratorConfigs.prefix+instance+ProblemGeneratorConfigs.problems;
				String planspath = ProblemGeneratorConfigs.prefix+instance+ProblemGeneratorConfigs.plans;
				ArrayList<String> criticals =  readCriticals(criticalStateFile);
				generateProblems(criticals, problemFile, problemspath);
				generatePlans(problemspath, domainFile, planspath, tracepath);
			}
		}
	}

	//TODO: add traces that shows decision delay until latest possible landmark added
	public static void generateReducedTrace() {
		
	}
	
	public static void main(String[] args) { 
		generateTemplatesAndFullTrace();
	}
}
