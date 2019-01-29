package train;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.Scanner;
import java.util.TreeSet;

/**
 * Generate planning problems for critical state to produce training data for decision tree
 * Using templates, generate 20 pairs of critical and desirable planning problems
 * @author sachini
 *
 */
public class PlanningProblemGenerator {
	private static final Logger LOGGER = Logger.getLogger(PlanningProblemGenerator.class.getName());

	public static ArrayList<String> readGoalStates(String cspath){
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

	public static ArrayList<String> readTemplate(String templatepath){
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

	public static void generateTrainCases(int currentCase, ArrayList<String> cstates, 
			ArrayList<String> dstates, 	ArrayList<String> t_problem, ArrayList<String> t_domain, 
			ArrayList<String> t_init, String outprefix) {
		TreeSet<String> a_init_t = new TreeSet<String>();
		ArrayList<String> a_init = new ArrayList<String>();
		ArrayList<String> u_init = new ArrayList<String>();
		TreeSet<String> domainObjects = null;
		if(TraceConfigs.domain.equalsIgnoreCase("blocks")) { //filter objects based on goal state. not everything in t_init is needed
			ArrayList<String> a_init_temp = extractInitPredicatesFromTemplate(t_init, cstates.get(currentCase));
			u_init = extractInitPredicatesFromTemplate(t_init, dstates.get(currentCase));
			a_init_t.addAll(u_init);
			a_init_t.addAll(a_init_temp); 
			a_init.addAll(a_init_t);//add both user and attacker inits to attacker problem
		}else if(TraceConfigs.domain.equalsIgnoreCase("easyipc") || 
				TraceConfigs.domain.equalsIgnoreCase("navigator") ) { //attacker and user inits are identical. everything in t_init is needed
			domainObjects = extractDomainObjectsFromInit(t_init);
			a_init.addAll(t_init);
			u_init.addAll(t_init);
		}
		ArrayList<String> a_temp_problem = addGoalStateToProblem(currentCase, t_problem, cstates.get(currentCase));//problem_a.pddl
		ArrayList<String> u_temp_problem = addGoalStateToProblem(currentCase, t_problem, dstates.get(currentCase));//problem_u.pddl
		ArrayList<String> a_problem = addInitsToProblem(currentCase, a_temp_problem, a_init);
		ArrayList<String> u_problem = addInitsToProblem(currentCase, u_temp_problem, u_init);
		ArrayList<String> probDomain = addObjectsToDomain(currentCase, domainObjects, t_domain, cstates.get(currentCase), dstates.get(currentCase));
		writeProblemToFile(currentCase, a_problem, outprefix, 0);
		writeProblemToFile(currentCase, u_problem, outprefix, 1);
		writeInitToFile(currentCase, a_init, outprefix, 0);
		writeInitToFile(currentCase, u_init, outprefix, 1);
		writeDomainToFile(probDomain, outprefix);
		writeCriticalStateToFile(cstates.get(currentCase), outprefix);
		writeDesirableStateToFile(dstates.get(currentCase), outprefix);
	}

	//TODO:: add case for domains where attacker and users inits are equal
	public static ArrayList<String> extractInitPredicatesFromTemplate(ArrayList<String> t_init, String goalstate) {
		ArrayList<String> init = new ArrayList<String>();
		TreeSet<String> objects = new TreeSet<String>();
		String parts[] = goalstate.split(",");//(ON R A),(ON A W)
		for (int x=0; x<parts.length; x++) {
			String sparts [] = parts[x].substring(1,parts[x].length()-1).split(" ");
			for (int i=1; i<sparts.length; i++) {
				objects.add(sparts[i]);
			}
		}
		if(TraceConfigs.domain.equalsIgnoreCase("blocks")) {
			for (String t : t_init) {
				for (String o : objects) {
					if(t.contains(" ")) {
						String []tparts = t.substring(1,t.length()-1).split(" ");
						if(tparts[tparts.length-1].equalsIgnoreCase(o)) {
							init.add(t);
						}
					}
				}
				if(t.equalsIgnoreCase("(HANDEMPTY)")) {
					init.add(t);
				}
			}
		}
		else if(TraceConfigs.domain.equalsIgnoreCase("easyipc") ||
				TraceConfigs.domain.equalsIgnoreCase("navigator")) { //template inits are equal to train scenario inits
			init.addAll(t_init);
		}
		return init;
	}

	//objects to add to domain.pddl. Blockswords (active attacker).
	public static String extractDomainObjectsFromGoalState(String cstate, String dstate) {
		TreeSet<String> objects = new TreeSet<>();
		String ob = "";
		if(TraceConfigs.domain.equalsIgnoreCase("blocks")) {
			String ctemp[] = cstate.split(",");
			for (String s : ctemp) {
				String cparts [] = s.substring(1,s.length()-1).split(" ");
				for (int x=1; x<cparts.length; x++) {
					objects.add(cparts[x]);
				}
			}
			String dtemp[] = dstate.split(",");
			for (String s : dtemp) {
				String dparts [] = s.substring(1,s.length()-1).split(" ");
				for (int x=1; x<dparts.length; x++) {
					objects.add(dparts[x]);
				}
			}
			for (String s : objects) {
				ob+=s+" - block \n";
			}
		}
		return ob;
	}

	//TODO: for grid-like domains, objects to add to domain.pddl comes from init template
	public static TreeSet<String> extractDomainObjectsFromInit(ArrayList<String> inits) {
		TreeSet<String> objects = new TreeSet<>();
		if(TraceConfigs.domain.equalsIgnoreCase("EASYIPC")) {
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
		}else if(TraceConfigs.domain.equalsIgnoreCase("navigator")) {
			for (String in : inits) { //add places
				if(in.contains("PLACE") && in.contains("CONNECTED")){
					String[] inparts = in.trim().substring(1,in.length()-1).split(" "); 
					objects.add(inparts[1].trim()); 
					objects.add(inparts[2].trim());
				}
			}
		}
		return objects;
	}
	
	public static ArrayList<String> addObjectsToDomain(int currentCase, TreeSet<String> objs, 
			ArrayList<String> t_domain, String cri, String des){
		String ob = "";
		if(TraceConfigs.domain.equalsIgnoreCase("blocks")) {
			ob = extractDomainObjectsFromGoalState(cri, des);
		}else if(TraceConfigs.domain.equalsIgnoreCase("easyipc")) {
			String places = "", keys="", shapes = "";
			for (String s : objs) {
				if(s.contains("PLACE_")) {
					places+=s+"\n";
				}else if(s.contains("KEY_")) {
					keys += s+"\n";
				}else if(s.contains("SHAPE_")) {
					shapes += s+"\n";
				}
			}
			ob = places + " - place\n" + keys + " - key\n" + shapes + " - shape\n";
		}else if(TraceConfigs.domain.equalsIgnoreCase("navigator")) {
			String places = "";
			for (String s : objs) {
				if(s.contains("PLACE_")) {
					places+=s+"\n";
				}
			}
			ob = places + " - place\n";
		}
		ArrayList<String> domupdated = new ArrayList<String>();
		domupdated.addAll(t_domain);
		for (int i=0; i<t_domain.size(); i++) {
			if(t_domain.get(i).contains("<OBJECTS>")) {
				domupdated.set(i, ob);
				break;
			}
		}
		return domupdated;
	}

	public static ArrayList<String> addGoalStateToProblem(int currentCase, ArrayList<String> p_template, String gstate) {
		ArrayList<String> updatedpr = new ArrayList<>();//problem_u/a.pddl
		updatedpr.addAll(p_template);
		updatedpr.set(p_template.size()-3, gstate);//replace tag <GOAL_STATE> with gstate. contains a separating comma. dont have to remove it
		return updatedpr;
	}

	public static ArrayList<String> addInitsToProblem(int currentCase, ArrayList<String> pcurrent, ArrayList<String> init) {
		ArrayList<String> updatedpr = new ArrayList<>();//problem_u/a.pddl
		updatedpr.addAll(pcurrent);
		String inits = "";
		for (String s : init) {
			inits += s+"\n";
		}
		updatedpr.set(3, inits);//replace tag <INITS> at index 3 with init predicates
		return updatedpr;
	}

	public static void writeProblemToFile(int currentCase, ArrayList<String> problem, 
			String problemsout, int type){
		PrintWriter writer = null;
		try{// home/sachini/domains/BLOCKS/scenarios/0/train/cases/0/
			if(type==0)
				writer = new PrintWriter(new File(problemsout+"problem_a.pddl"), "UTF-8");
			else
				writer = new PrintWriter(new File(problemsout+"problem_u.pddl"), "UTF-8");
			for (int i = 0; i < problem.size(); i++) {
				writer.write(problem.get(i));
				writer.println();
			}
			writer.close();
		}catch (FileNotFoundException | UnsupportedEncodingException  e) {
			e.printStackTrace();
		}
	}

	public static void writeInitToFile(int currentCase, ArrayList<String> init, 
			String initsout, int type){
		PrintWriter writer = null;
		try{
			if(type==0)
				writer = new PrintWriter(new File(initsout+"init_a.txt"), "UTF-8");
			else
				writer=new PrintWriter(new File(initsout+"init_u.txt"), "UTF-8");
			for (int i = 0; i < init.size(); i++) {
				writer.write(init.get(i));
				writer.println();
			}
			writer.close();
		}catch (FileNotFoundException | UnsupportedEncodingException  e) {
			e.printStackTrace();
		}
	}

	public static void writeDomainToFile(ArrayList<String> domain, String domainout){
		PrintWriter writer = null;
		try{
			writer = new PrintWriter(new File(domainout+"domain.pddl"), "UTF-8");
			for (int i = 0; i < domain.size(); i++) {
				writer.write(domain.get(i));
				writer.println();
			}
			writer.close();
		}catch (FileNotFoundException | UnsupportedEncodingException  e) {
			e.printStackTrace();
		}
	}

	public static void writeCriticalStateToFile(String critical, String crioutput){
		PrintWriter writer = null;
		try{
			writer = new PrintWriter(new File(crioutput+"critical.txt"), "UTF-8");
			String parts [] = critical.split(",");
			for (int i = 0; i < parts.length; i++) {
				writer.write(parts[i]);
				writer.println();
			}
			writer.close();
		}catch (FileNotFoundException | UnsupportedEncodingException  e) {
			e.printStackTrace();
		}
	}

	public static void writeDesirableStateToFile(String desirable, String desoutput){
		PrintWriter writer = null;
		try{
			writer = new PrintWriter(new File(desoutput+"desirable.txt"), "UTF-8");
			writer.write("desirable:"+desirable);
			writer.println();
		}catch (FileNotFoundException | UnsupportedEncodingException  e) {
			e.printStackTrace();
		}finally{
			writer.close();
		}
	}

	public static void createDirectories(int cases) {
		String outputpath = TraceConfigs.prefix+TraceConfigs.cases;
		for (int caseid=0; caseid<cases; caseid++){
			new File(outputpath+caseid+"/"+TraceConfigs.datadir+"/decision/").mkdirs();
			new File(outputpath+caseid+"/"+TraceConfigs.datadir+"/inputdecisiontree/").mkdirs();
			new File(outputpath+caseid+"/"+TraceConfigs.datadir+"/weighted/").mkdirs();
			new File(outputpath+caseid+"/"+TraceConfigs.dot+"/full").mkdirs();
			new File(outputpath+caseid+"/"+TraceConfigs.out+"/attacker/").mkdirs();
			new File(outputpath+caseid+"/"+TraceConfigs.out+"/user/").mkdirs();
			new File(outputpath+caseid+"/"+TraceConfigs.obs).mkdirs();
		}
	}

	//generates common templates and full trace set (all actions including) for each test instance {1,2,3}
	public static void generateProblemsFromTemplate(){
		String problemtemplate = TraceConfigs.prefix+TraceConfigs.template_problem;
		String domaintemplate = TraceConfigs.prefix+TraceConfigs.template_domain;
		String inittemplate = TraceConfigs.prefix+TraceConfigs.template_init;
		String criticalStateFile = TraceConfigs.prefix+TraceConfigs.t_CriticalStates;
		String desirableStateFile = TraceConfigs.prefix+TraceConfigs.t_DesirableStates;
		ArrayList<String> criticals =  readGoalStates(criticalStateFile);
		ArrayList<String> desirables = readGoalStates(desirableStateFile);
		ArrayList<String> t_problem = readTemplate(problemtemplate);
		ArrayList<String> t_domain = readTemplate(domaintemplate);
		ArrayList<String> t_init = readTemplate(inittemplate);
		createDirectories(TraceConfigs.trainingcases);
		for(int c=0; c<TraceConfigs.trainingcases; c++){
			LOGGER.log(Level.INFO, "Problem generation for training case "+ c );
			String outprefix = TraceConfigs.prefix+TraceConfigs.cases+c+"/";
			generateTrainCases(c, criticals, desirables, t_problem, t_domain, t_init, outprefix);
		}
		LOGGER.log(Level.INFO, "Planning problem generation done" );
	}
}
