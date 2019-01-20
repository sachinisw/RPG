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
import train.TraceGenerator;

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
				if(state.contains("#")) {
					gs.add(state.substring(0, state.indexOf("#")));
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
		TreeSet<String> cs, ds = null;
		if(HarnessConfigs.domain.equalsIgnoreCase("BLOCKS")) { //init objects are different from cs & ds
			cs = extractObjectsFromGoalState(critical);
			ds = extractObjectsFromGoalState(desirable);
			objects.addAll(cs);
			objects.addAll(ds);
		}else if(HarnessConfigs.domain.equalsIgnoreCase("EASYIPC") || 
				HarnessConfigs.domain.equalsIgnoreCase("NAVIGATOR") ||
				HarnessConfigs.domain.equalsIgnoreCase("FERRY") ) { //init objects cs, ds are the same
			cs = extractObjectsFromGoalState(critical);
			ds = extractObjectsFromGoalState(desirable);
			objects.addAll(extractObjectsFromInits(inits));
		}
		ArrayList<String> prob = readTemplate(problemTemplatepath);
		ArrayList<String> domout = generateDomain(readTemplate(domainTemplatePath), objects);
		ArrayList<String> aout = generateProblem(prob, filterInits(inits, objects), critical);
		ArrayList<String> uout = generateProblem(prob, filterInits(inits, ds), desirable); //TODO: i am doing only one deception scenario. to keep it simple. For that scenario inits must be filtered for the user
		writeToFile(domout, outputpath, String.valueOf(id), HarnessConfigs.domfilename); //write domain
		writeToFile(aout, outputpath, String.valueOf(id), HarnessConfigs.aprobfilename); //write problem_a.pddl
		writeToFile(uout, outputpath, String.valueOf(id), HarnessConfigs.uprobfilename); //write problem_u.pddl
		adata.addAll(filterInits(inits, objects));
		udata.addAll(filterInits(inits, ds));
		writeToFile(adata, outputpath, String.valueOf(id), HarnessConfigs.ainit); //write inits_a
		writeToFile(udata, outputpath, String.valueOf(id), HarnessConfigs.uinit); //write inits_u
		writeToFile(getCriticalState(critical), outputpath, String.valueOf(id), HarnessConfigs.critical); //write critical.txt
		writeToFile(getDesirableState(desirable), outputpath, String.valueOf(id), HarnessConfigs.desirable); //write desirable.txt
		createDirectories(outputpath, String.valueOf(id)); //create directories to store output files
	}			

	public static void generateObservationTraceForTestInstance(String pathprefix, String scid) {
//		String domainpath = pathprefix+scid+HarnessConfigs.domainFile;
//		String problempath= pathprefix+scid+"/"+HarnessConfigs.aprobfilename;
//		String planoutputpath=pathprefix+scid+"/"+HarnessConfigs.tempplan+"/";
//		String domain = pathprefix+scid+HarnessConfigs.domain;
//		String desirablefile = pathprefix+scid+"/"+HarnessConfigs.desirable;
//		String a_prob = pathprefix+scid+"/"+HarnessConfigs.aprobfilename;
//		String a_out = pathprefix+scid+"/"+HarnessConfigs.outdir+"/"+HarnessConfigs.aout+"/";
//		String criticalfile = pathprefix+scid+"/"+HarnessConfigs.critical;
//		String a_init = pathprefix+scid+"/"+HarnessConfigs.ainit;
//		String a_dotpre = pathprefix+scid+"/"+HarnessConfigs.dotdir+"/";
//		String a_dotsuf = HarnessConfigs.u_dotFileSuffix;
		//		Planner.runFF(1, domainpath, problempath, planoutputpath); //just create a plan for the attacker domain
		//		Planner.runFF(3, domainpath, problempath, planoutputpath); //and connectivity
		//		ArrayList<Plan> plans = Planner.readPlans(planoutputpath);//just 1 plan
		//		ArrayList<String> plansteps = plans.get(0).getPlanSteps();
		//		for (int x=0; x<plansteps.size(); x++) {
		//			plansteps.set(x, "?:"+plansteps.get(x).substring(plansteps.get(x).indexOf(":")+2,plansteps.get(x).length()));
		//		}
		//		addLabelsToTrace(plansteps, pathprefix, scid);
	}
	
	public static void addLabelsToTrace(ArrayList<String> trace, String pathprefix, String scid) {
		String obspath = pathprefix+scid+"/"+HarnessConfigs.obsdir+"/"+scid;
		String congraphpath = pathprefix+scid+"/"+HarnessConfigs.tempplan+"/"+HarnessConfigs.acon;
		String cspath = pathprefix+scid+"/"+HarnessConfigs.critical;
		String initspath = pathprefix+scid+"/"+HarnessConfigs.ainit;
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
		new File(outputpath+scenarioid+"/"+HarnessConfigs.datadir+"/decision/").mkdirs();
		new File(outputpath+scenarioid+"/"+HarnessConfigs.datadir+"/inputdecisiontree/").mkdirs();
		new File(outputpath+scenarioid+"/"+HarnessConfigs.datadir+"/weighted/").mkdirs();
		new File(outputpath+scenarioid+"/"+HarnessConfigs.dotdir+"/full").mkdirs();
		new File(outputpath+scenarioid+"/"+HarnessConfigs.dotdir+"/lm").mkdirs();
		new File(outputpath+scenarioid+"/"+HarnessConfigs.outdir+"/attacker/").mkdirs();
		new File(outputpath+scenarioid+"/"+HarnessConfigs.outdir+"/user/").mkdirs();
		new File(outputpath+scenarioid+"/"+HarnessConfigs.obsdir+"/").mkdirs();
		new File(outputpath+scenarioid+"/"+HarnessConfigs.obslm+"/").mkdirs();
		new File(outputpath+scenarioid+"/"+HarnessConfigs.tempplan+"/").mkdirs();
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

	public static TreeSet<String> filterInits(TreeSet<String> initsfull, TreeSet<String> objects) {
		TreeSet<String> filtered = new TreeSet<String>();
		if(HarnessConfigs.domain.equalsIgnoreCase("blocks")) { //for domains where user's inits and attacker's inits are different
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
		}else if(HarnessConfigs.domain.equalsIgnoreCase("easyipc")||
				HarnessConfigs.domain.equalsIgnoreCase("navigator") || 
				HarnessConfigs.domain.equalsIgnoreCase("ferry") ){//for domains where user's and attacker's inits are the same
			filtered.addAll(initsfull);
		}

		return filtered;
	}

	public static ArrayList<String> generateProblem(ArrayList<String> probTemplate, TreeSet<String> inits, String goal) {
		ArrayList<String> out = new ArrayList<>();
		out.addAll(probTemplate);
		for (int i=0; i<probTemplate.size(); i++) {
			if(probTemplate.get(i).equals("<INITS>")){
				if(HarnessConfigs.domain.equalsIgnoreCase("BLOCKS") || 
						HarnessConfigs.domain.equalsIgnoreCase("EASYIPC") || 
						HarnessConfigs.domain.equalsIgnoreCase("NAVIGATOR") ||
						HarnessConfigs.domain.equalsIgnoreCase("FERRY") ){
					String init = "";
					for (String string : inits) {
						init+= string +"\n";
					}
					out.set(i, init);
				}
			}else if(probTemplate.get(i).equals("<GOAL_STATE>")) {
				if(HarnessConfigs.domain.equalsIgnoreCase("BLOCKS")) {
					out.set(i, goal.replace(",", ""));
				}else if(HarnessConfigs.domain.equalsIgnoreCase("EASYIPC")||
						(HarnessConfigs.domain.equalsIgnoreCase("NAVIGATOR")) ||
						HarnessConfigs.domain.equalsIgnoreCase("FERRY") ) {
					out.set(i, goal);
				}
			}
		}
		return out;
	}

	public static ArrayList<String> generateDomain(ArrayList<String> Template, TreeSet<String> objects) {
		for (int i=0; i<Template.size(); i++) {
			if(Template.get(i).equals("<OBJECTS>")){
				String obs = "";
				if(HarnessConfigs.domain.equalsIgnoreCase("BLOCKS")) {
					for (String string : objects) {
						obs+= string +" - block" +"\n";
					}
				}else if(HarnessConfigs.domain.equalsIgnoreCase("EASYIPC")){
					for (String string : objects) {
						if(string.contains("PLACE_")) {
							obs+= string +"\n";
						}
					}
					obs+=" - place"+"\n";
					for (String string : objects) {
						if(string.contains("KEY")) {
							obs+= string +"\n";
						}
					}
					obs+=" - key"+"\n";
					for (String string : objects) {
						if(string.contains("SHAPE")) {
							obs+= string +"\n";
						}
					}
					obs+=" - shape"+"\n";
				}else if(HarnessConfigs.domain.equalsIgnoreCase("NAVIGATOR")){
					for (String string : objects) {
						if(string.contains("PLACE_")) {
							obs+= string +"\n";
						}
					}
					obs+=" - place"+"\n";
				}else if(HarnessConfigs.domain.equalsIgnoreCase("FERRY")){
					for (String string : objects) {
						if(string.contains("C")) {
							obs+= string +"\n";
						}
					}
					obs+=" - car"+"\n";
					for (String string : objects) {
						if(string.contains("L")) {
							obs+= string +"\n";
						}
					}
					obs+=" - location"+"\n";
				}
				Template.set(i, obs);
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

	public static TreeSet<String> extractObjectsFromInits(TreeSet<String> inits){
		TreeSet<String> objects = new TreeSet<>();
		if(HarnessConfigs.domain.equalsIgnoreCase("EASYIPC")) {
			for (String in : inits) { //add places
				if(in.contains("CONN")){
					String[] inparts = in.trim().substring(1,in.length()-1).split(" "); //its ok to have duplicates here. treeset will remove that
					objects.add(inparts[1].trim());
					objects.add(inparts[2].trim());
				}
			}
			for (String in : inits) { //add shapes
				if(in.contains("LOCK-SHAPE")){
					String[] inparts = in.trim().substring(1,in.length()-1).split(" "); 
					objects.add(inparts[2].trim());
				}
			}
			for (String in : inits) { //add keys
				if(in.contains("KEY-SHAPE")){
					String[] inparts = in.trim().substring(1,in.length()-1).split(" "); 
					objects.add(inparts[1].trim()); 
				}
			}
		}else if(HarnessConfigs.domain.equalsIgnoreCase("NAVIGATOR")) {
			for (String in : inits) { //add places
				if(in.contains("CONNECTED")){
					String[] inparts = in.trim().substring(1,in.length()-1).split(" ");
					objects.add(inparts[1].trim());
					objects.add(inparts[2].trim());
				}
			}
		}else if(HarnessConfigs.domain.equalsIgnoreCase("FERRY")) {
			for (String in : inits) { //add locations
				if(in.contains("NOT-EQ")){
					String[] inparts = in.trim().substring(1,in.length()-1).split(" ");
					objects.add(inparts[1].trim());
					objects.add(inparts[2].trim());
				}
			}
			for (String in : inits) { //add cars
				if(in.contains("AT") && !in.contains("AT-FERRY")){
					String[] inparts = in.trim().substring(1,in.length()-1).split(" ");
					objects.add(inparts[1].trim());
					objects.add(inparts[2].trim());
				}
			}
		}
		return objects;
	}

	public static void generateTracesForInstances(int start) {
		for(int instance=start; instance<=HarnessConfigs.testInstanceCount; instance++){
			String domainTemplate = HarnessConfigs.prefix+instance+HarnessConfigs.template_domain;
			String problemTemplate = HarnessConfigs.prefix+instance+HarnessConfigs.template_problemgen;
			String cspath = HarnessConfigs.prefix+instance+HarnessConfigs.criticalStateFile;
			String dspath = HarnessConfigs.prefix+instance+HarnessConfigs.desirablestates;
			String problemoutput = HarnessConfigs.prefix+instance+HarnessConfigs.problemgen_output; //add problem id (i)
			String initspath = HarnessConfigs.prefix+instance+HarnessConfigs.initFile;
			ArrayList<String> criticals = readGoals(cspath);
			ArrayList<String> desirables = readGoals(dspath);
			TreeSet<String> inits = readInits(initspath);
			for (int i=0; i<HarnessConfigs.testProblemCount; i++) { //generate labeled obs traces for the 20 problems using TraceGenerator
				String domainpath = problemoutput+i+"/"+HarnessConfigs.domfilename;
				String domain = HarnessConfigs.domain;
				String desirablefile = problemoutput+i+"/"+HarnessConfigs.desirable;
				String a_prob = problemoutput+i+"/"+HarnessConfigs.aprobfilename;
				String a_out = problemoutput+i+"/"+HarnessConfigs.outdir+"/"+HarnessConfigs.aout+"/";
				String criticalfile = problemoutput+i+"/"+HarnessConfigs.critical;
				String a_init = problemoutput+i+"/"+HarnessConfigs.ainit;
				String a_dotpre = problemoutput+i+"/"+HarnessConfigs.dotdir+"/";
				String a_dotsuf = HarnessConfigs.a_dotFileSuffix;
				String obsout = problemoutput+i+"/"+HarnessConfigs.obsdir+"/";
				generateProblemsForTestInstance(i, criticals.get(i), desirables.get(i), inits, domainTemplate, problemTemplate,
						problemoutput);
				TraceGenerator.generateTestingObservationTrace(domain, domainpath, desirablefile, a_prob, criticalfile, 
						a_out, a_init, a_dotpre, a_dotsuf, obsout);
//				generateObservationTraceForTestInstance(problemoutput, String.valueOf(i));
//				break;
			}
			LOGGER.log(Level.INFO, "Trace generator input files for :" +instance + " for scenario ["+ HarnessConfigs.testscenario+"] done");
		}
	}
	//This is called second.
}
