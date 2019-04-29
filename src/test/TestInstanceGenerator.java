package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import plans.InterventionPlan;

import java.util.Scanner;

import test.HarnessConfigs;
/**
 * Generate observation traces for testing the trained model of the decision tree
 * This is called first. From templates, generate 3 test instances with 20 problems for each instance
 * @author sachini
 *
 */
public class TestInstanceGenerator {
	private static final Logger LOGGER = Logger.getLogger(TestInstanceGenerator.class.getName());
	public final static int filterLimit = 80;
	public final static double selectProbability = 0.80;

	private static void writeTracesToFile(HashMap<String, ArrayList<String>> traceset, String tracepath){
		PrintWriter writer = null;
		Iterator<Entry<String, ArrayList<String>>> itr = traceset.entrySet().iterator();
		while(itr.hasNext()) {
			Entry<String, ArrayList<String>> e = itr.next();
			ArrayList<String> tr = e.getValue();
			String key = e.getKey();
			try{
				writer = new PrintWriter(tracepath+"/"+Integer.parseInt(key), "UTF-8");
				for (String string : tr) {
					writer.write(string);
					writer.println();
				}
			}catch (FileNotFoundException | UnsupportedEncodingException  ex) {
				ex.printStackTrace();
			}finally{
				writer.close();
			}
		}
	}

	public static ArrayList<String> readCriticals(String cspath){
		ArrayList<String> cs = new ArrayList<String>();
		Scanner reader;
		try {
			reader = new Scanner (new File(cspath));
			while(reader.hasNextLine()) {
				String state = reader.nextLine();
				cs.add(state.substring(0, state.indexOf("#")));
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
		for (String cs : goalstates) { //replace tag <CRITICAL> with cs. contains a separating comma. dont have to remove it
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
		for (int i = 0; i < HarnessConfigs.testProblemCount; i++) {
			Planner.runFF(1, domainpath, problemspath+"problem_"+i+".pddl", planspath); //just create a plan
		}
		ArrayList<InterventionPlan> plans = Planner.readPlans(planspath);
		HashMap<String, ArrayList<String>> traceset = new HashMap<>();
		for (InterventionPlan plan : plans) {
			ArrayList<String> steps = plan.getPlanSteps();
			String pid= plan.getPlanID().substring(plan.getPlanID().indexOf("_")+1).trim();
			int index=0;
			for (String step : steps) {
				steps.set(index, "?:"+step.substring(step.indexOf(":")+2,step.length()));
				index++;
			}
			traceset.put(pid, steps); //insert in order of plan-id so that trace file aligns with plan file
		}
		writeTracesToFile(traceset, tracepath);
	}

	//generates common templates and full trace set (all actions including) for each test instance {1,2,3}
	public static void generateProblemsFromTemplate(int start){ 
		for(int instance=start; instance<=HarnessConfigs.testInstanceCount; instance++){
			LOGGER.log(Level.INFO, "Template generation for test instance "+ instance );
			String problemFile = HarnessConfigs.prefix+instance+HarnessConfigs.template_problem;
			String criticalStateFile = HarnessConfigs.prefix+instance+HarnessConfigs.criticalStateFile;
			String problemspath = HarnessConfigs.prefix+instance+HarnessConfigs.problems;
			ArrayList<String> criticals =  readCriticals(criticalStateFile);
			generateProblems(criticals, problemFile, problemspath);
		}
		LOGGER.log(Level.INFO, "Template generation done" );
	}	//README: This is called first. From templates, generate 3 test instances with 'testProblemCount' problems
}
