package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import con.ConnectivityGraph;
import plan.Plan;

public class InstanceProblemGenerator {
	/**
	 * Generate 20 problem scenarios (and files needed for producing test data) for each test instance
	 * This is called second. For each problem scenario in each instance, generate obs using a temp plan
	 * @author sachini
	 *
	 */
	private static final Logger LOGGER = Logger.getLogger(InstanceProblemGenerator.class.getName());

	public static ArrayList<String> readGoals(String cspath){ //call twice to read criticals and desirables
		ArrayList<String> gs = new ArrayList<String>();
		Scanner reader;
		try {
			reader = new Scanner (new File(cspath));
			while(reader.hasNextLine()) {
				String state = reader.nextLine();
				if(state.contains("-")) {
					gs.add(state.substring(0, state.indexOf("-")));
				}else {
					gs.add(state);
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return gs;
	}

	public static TreeSet<String> readInits(String initpath){
		TreeSet<String> init = new TreeSet<String>();
		Scanner reader;
		try {
			reader = new Scanner (new File(initpath));
			while(reader.hasNextLine()) {
				init.add(reader.nextLine());
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return init;
	}

	public static ArrayList<String> readTemplate(String templatepath){ 
		ArrayList<String> dom = new ArrayList<String>();
		Scanner reader;
		try {
			reader = new Scanner (new File(templatepath));
			while(reader.hasNextLine()) {
				dom.add(reader.nextLine());
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return dom;
	}

	public static void generateProblemsForTestInstance(int id, String critical, String desirable, 
			TreeSet<String> inits, String domainTemplatePath, String problemTemplatepath, String outputpath) {
		TreeSet<String> objects = new TreeSet<String>();
		ArrayList<String> adata = new ArrayList<>();
		ArrayList<String> udata = new ArrayList<>();
		TreeSet<String> cs = extractObjectsFromGoalState(critical);
		TreeSet<String> ds = extractObjectsFromGoalState(desirable);
		objects.addAll(cs);
		objects.addAll(ds);
		ArrayList<String> prob = readTemplate(problemTemplatepath);
		ArrayList<String> domout = generateDomain(readTemplate(domainTemplatePath), objects);
		ArrayList<String> aout = generateProblem(prob, filterInits(inits, objects), critical);
		ArrayList<String> uout = generateProblem(prob, filterInits(inits, ds), desirable); //TODO: i am doing only one deception scenario. to keep it simple. For that scenario inits must be filtered for the user
		writeToFile(domout, outputpath, String.valueOf(id), TestGeneratorConfigs.domfilename); //write domain
		writeToFile(aout, outputpath, String.valueOf(id), TestGeneratorConfigs.aprobfilename); //write problem_a.pddl
		writeToFile(uout, outputpath, String.valueOf(id), TestGeneratorConfigs.uprobfilename); //write problem_u.pddl
		adata.addAll(filterInits(inits, objects));
		udata.addAll(filterInits(inits, ds));
		writeToFile(adata, outputpath, String.valueOf(id), TestGeneratorConfigs.ainit); //write inits_a
		writeToFile(udata, outputpath, String.valueOf(id), TestGeneratorConfigs.uinit); //write inits_u
		writeToFile(getCriticalState(critical), outputpath, String.valueOf(id), TestGeneratorConfigs.critical); //write critical.txt
		writeToFile(getDesirableState(desirable), outputpath, String.valueOf(id), TestGeneratorConfigs.desirable); //write desirable.txt
		createDirectories(outputpath, String.valueOf(id)); //create directories to store output files
	}			

	public static void generateObservationTraceForTestInstance(String pathprefix, String scid) {
		String domainpath = pathprefix+scid+TestGeneratorConfigs.domainFile;
		String problempath= pathprefix+scid+"/"+TestGeneratorConfigs.aprobfilename;
		String planoutputpath=pathprefix+scid+"/"+TestGeneratorConfigs.tempplan+"/";
		Planner.runFF(1, domainpath, problempath, planoutputpath); //just create a plan for the attacker domain
		Planner.runFF(3, domainpath, problempath, planoutputpath); //and connectivity
		ArrayList<Plan> plans = Planner.readPlans(planoutputpath);//just 1 plan
		ArrayList<String> plansteps = plans.get(0).getPlanSteps();
		for (int x=0; x<plansteps.size(); x++) {
			plansteps.set(x, "?:"+plansteps.get(x).substring(plansteps.get(x).indexOf(":")+2,plansteps.get(x).length()));
		}
		addLabelsToTrace(plansteps, pathprefix, scid);
	}

	public static void addLabelsToTrace(ArrayList<String> trace, String pathprefix, String scid) {
		String obspath = pathprefix+scid+"/"+TestGeneratorConfigs.obsdir+"/"+scid;
		String congraphpath = pathprefix+scid+"/"+TestGeneratorConfigs.tempplan+"/"+TestGeneratorConfigs.acon;
		String cspath = pathprefix+scid+"/"+TestGeneratorConfigs.critical;
		String initspath = pathprefix+scid+"/"+TestGeneratorConfigs.ainit;
		ArrayList<String> labeledsteps = new ArrayList<String>();
		ConnectivityGraph con = new ConnectivityGraph(congraphpath);
		con.readConGraphOutput(congraphpath);
		ArrayList<String> cs = readGoals(cspath);
		TreeSet<String> inits = readInits(initspath);
		ArrayList<String> runningstate = new ArrayList<>();
		runningstate.addAll(inits);
		for (String s : trace) {
			ArrayList<String> adds = con.findStatesAddedByAction(s.substring(2));
			ArrayList<String> dels = con.findStatesDeletedByAction(s.substring(2));
			runningstate.removeAll(dels);
			runningstate.addAll(adds);
			if(currentContainsState(runningstate, cs)) {
				labeledsteps.add("N:"+s.substring(s.indexOf(":")+1,s.length()));
			}else {
				labeledsteps.add("Y:"+s.substring(s.indexOf(":")+1,s.length()));
			}
		}
		writePlanToObs(labeledsteps, obspath);
	}

	public static boolean currentContainsState(ArrayList<String> currentstate, ArrayList<String> tocheck) {
		int checkcount = 0;
		for (String chk : tocheck) {
			boolean found = false;
			for (String el : currentstate) {
				if(chk.equalsIgnoreCase(el)) {
					found = true;
				}
			}
			if(found) checkcount++;
		}
		return checkcount==tocheck.size();
	}

	public static void writePlanToObs(ArrayList<String> plan, String obspath) {
		PrintWriter writer = null;
		try{
			writer = new PrintWriter(obspath, "UTF-8");
			for (int i = 0; i < plan.size(); i++) {
				writer.write(plan.get(i));
				writer.println();
			}
		}catch (FileNotFoundException | UnsupportedEncodingException  e) {
			e.printStackTrace();
		}finally{
			writer.close();
		}
	}

	public static void createDirectories(String outputpath, String scenarioid) {
		new File(outputpath+scenarioid+"/"+TestGeneratorConfigs.datadir+"/decision/").mkdirs();
		new File(outputpath+scenarioid+"/"+TestGeneratorConfigs.datadir+"/inputdecisiontree/").mkdirs();
		new File(outputpath+scenarioid+"/"+TestGeneratorConfigs.datadir+"/weighted/").mkdirs();
		new File(outputpath+scenarioid+"/"+TestGeneratorConfigs.dotdir+"/full").mkdirs();
		new File(outputpath+scenarioid+"/"+TestGeneratorConfigs.dotdir+"/lm").mkdirs();
		new File(outputpath+scenarioid+"/"+TestGeneratorConfigs.outdir+"/attacker/").mkdirs();
		new File(outputpath+scenarioid+"/"+TestGeneratorConfigs.outdir+"/user/").mkdirs();
		new File(outputpath+scenarioid+"/"+TestGeneratorConfigs.obsdir+"/").mkdirs();
		new File(outputpath+scenarioid+"/"+TestGeneratorConfigs.obslm+"/").mkdirs();
		new File(outputpath+scenarioid+"/"+TestGeneratorConfigs.tempplan+"/").mkdirs();
	}

	public static ArrayList<String> getCriticalState(String critical) {
		ArrayList<String> c = new ArrayList<String>();
		String parts [] = critical.split(",");
		for (String string : parts) {
			c.add(string);
		}
		return c;
	}

	public static ArrayList<String> getDesirableState(String desirable) {
		ArrayList<String> c = new ArrayList<String>();
		c.add("desirable:"+desirable);
		return c;
	}

	//TODO: Currently applicable for BLOCKS domain only
	public static TreeSet<String> filterInits(TreeSet<String> initsfull, TreeSet<String> objects) {
		TreeSet<String> filtered = new TreeSet<String>();
		for (String s : initsfull) {
			String parts[] = s.substring(1,s.length()-1).split(" ");
			if(parts.length==1) {
				filtered.add(s);
			}else {
				for (int i=1; i<parts.length; i++) {
					for (String o : objects) {
						if(parts[i].equalsIgnoreCase(o)) {
							filtered.add(s);
						}
					}
				}
			}
		}
		return filtered;
	}

	public static ArrayList<String> generateProblem(ArrayList<String> probTemplate, TreeSet<String> inits, String goal) {
		ArrayList<String> out = new ArrayList<>();
		out.addAll(probTemplate);
		for (int i=0; i<probTemplate.size(); i++) {
			if(probTemplate.get(i).equals("<INITS>")){
				if(TestGeneratorConfigs.domain.equalsIgnoreCase("BLOCKS")) {
					String init = "";
					for (String string : inits) {
						init+= string +"\n";
					}
					out.set(i, init);
				}
			}else if(probTemplate.get(i).equals("<GOAL_STATE>")) {
				if(TestGeneratorConfigs.domain.equalsIgnoreCase("BLOCKS")) {
					out.set(i, goal.replace(",", ""));
				}
			}
		}
		return out;
	}

	public static ArrayList<String> generateDomain(ArrayList<String> Template, TreeSet<String> objects) {
		for (int i=0; i<Template.size(); i++) {
			if(Template.get(i).equals("<OBJECTS>")){
				if(TestGeneratorConfigs.domain.equalsIgnoreCase("BLOCKS")) {
					String obs = "";
					for (String string : objects) {
						obs+= string +" - block" +"\n";
					}
					Template.set(i, obs);
				}
			}
		}
		return Template;
	}

	public static void writeToFile(ArrayList<String> data, String outputpath, String scenarioid, String filename){
		PrintWriter writer = null;
		try{
			new File(outputpath+scenarioid+"/").mkdirs();
			writer = new PrintWriter(outputpath+scenarioid+"/"+filename, "UTF-8");
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

	public static TreeSet<String> extractObjectsFromGoalState(String goal){
		TreeSet<String> objects = new TreeSet<>();
		String[] gs = goal.split(",");
		for (String s : gs) {
			String[] sparts = s.substring(0,s.length()-1).split(" ");
			for(int i=1; i<sparts.length; i++) {
				objects.add(sparts[i]);
			}
		}
		return objects;
	}

	public static void main(String[] args) {
		for(int instance=1; instance<=TestGeneratorConfigs.testInstanceCount; instance++){
			String domainTemplate = TestGeneratorConfigs.prefix+instance+TestGeneratorConfigs.template_domain;
			String problemTemplate = TestGeneratorConfigs.prefix+instance+TestGeneratorConfigs.template_problemgen;
			String cspath = TestGeneratorConfigs.prefix+instance+TestGeneratorConfigs.criticalStateFile;
			String dspath = TestGeneratorConfigs.prefix+instance+TestGeneratorConfigs.desirablestates;
			String problemoutput = TestGeneratorConfigs.prefix+instance+TestGeneratorConfigs.problemgen_output; //add problem id (i)
			String initspath = TestGeneratorConfigs.prefix+instance+TestGeneratorConfigs.initFile;
			ArrayList<String> criticals = readGoals(cspath);
			ArrayList<String> desirables = readGoals(dspath);
			TreeSet<String> inits = readInits(initspath);
			for (int i=0; i<20; i++) {
				generateProblemsForTestInstance(i, criticals.get(i), desirables.get(i), inits, domainTemplate, problemTemplate,
						problemoutput);
				generateObservationTraceForTestInstance(problemoutput, String.valueOf(i));
				LOGGER.log(Level.INFO, "Finished trace: "+ i +" for test instance:" +instance );
			}
			LOGGER.log(Level.INFO, "Finished full trace for test instance:" +instance );
		}
	}
}
