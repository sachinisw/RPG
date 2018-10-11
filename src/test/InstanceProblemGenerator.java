package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeSet;

public class InstanceProblemGenerator {
	/**
	 * Generate 20 problem scenarios (and files needed for producing test data) for each test instance
	 * This is called second. For each problem scenario in each instance, generate reduced trace considering landmarks
	 * @author sachini
	 *
	 */
	public static ArrayList<String> readGoals(String cspath){ //call twice to read criticals and desirables
		ArrayList<String> gs = new ArrayList<String>();
		Scanner reader;
		try {
			reader = new Scanner (new File(cspath));
			while(reader.hasNextLine()) {
				String state = reader.nextLine();
				gs.add(state.substring(0, state.indexOf("-")));
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
			TreeSet<String> inits, String domainTemplatePath, String problemTemplatepath, String outputpath, String origtracepath) {
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
		ArrayList<String> uout = generateProblem(prob, filterInits(inits, ds), desirable); //TODO: i am doing only one deception scenario. For that scenario inits must be filtered for the user
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
		copyApplicableObsFileToScenario(origtracepath+String.valueOf(id), outputpath+String.valueOf(id)+"/"+TestGeneratorConfigs.obsdir+"/"+String.valueOf(id)); //copy idth file from /traces at template level to scenarios/i/obs
		//TODO: copy reduced lm based observations to obslm dir
	}			

	public static void createDirectories(String outputpath, String scenarioid) {
		new File(outputpath+scenarioid+"/"+TestGeneratorConfigs.datadir+"/decision/").mkdirs();
		new File(outputpath+scenarioid+"/"+TestGeneratorConfigs.datadir+"/inputdecisiontree/").mkdirs();
		new File(outputpath+scenarioid+"/"+TestGeneratorConfigs.datadir+"/weighted/").mkdirs();
		new File(outputpath+scenarioid+"/"+TestGeneratorConfigs.dotdir+"/").mkdirs();
		new File(outputpath+scenarioid+"/"+TestGeneratorConfigs.outdir+"/attacker/").mkdirs();
		new File(outputpath+scenarioid+"/"+TestGeneratorConfigs.outdir+"/user/").mkdirs();
		new File(outputpath+scenarioid+"/"+TestGeneratorConfigs.obsdir+"/").mkdirs();
		new File(outputpath+scenarioid+"/"+TestGeneratorConfigs.obslm+"/").mkdirs();
	}


	public static void copyApplicableObsFileToScenario(String spath, String dpath){ //copy from ../TEST1/inst1/traces/i to ../TEST1/inst1/scenarios/i
		File source = new File(spath);
		File dest = new File(dpath);
		try {
			Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		for(int instance=1; instance<=3; instance++){
			String domainTemplate = TestGeneratorConfigs.prefix+instance+TestGeneratorConfigs.template_domain;
			String problemTemplate = TestGeneratorConfigs.prefix+instance+TestGeneratorConfigs.template_problemgen;
			String cspath = TestGeneratorConfigs.prefix+instance+TestGeneratorConfigs.criticalStateFile;
			String dspath = TestGeneratorConfigs.prefix+instance+TestGeneratorConfigs.desirablestates;
			String problemoutput = TestGeneratorConfigs.prefix+instance+TestGeneratorConfigs.problemgen_output; //add problem id (i)
			String initspath = TestGeneratorConfigs.prefix+instance+TestGeneratorConfigs.initFile;
			String origtracepath = TestGeneratorConfigs.prefix+instance+TestGeneratorConfigs.traces;
			ArrayList<String> criticals = readGoals(cspath);
			ArrayList<String> desirables = readGoals(dspath);
			TreeSet<String> inits = readInits(initspath);
			for (int i=0; i<20; i++) {
				generateProblemsForTestInstance(i, criticals.get(i), desirables.get(i), inits, domainTemplate, problemTemplate,
						problemoutput, origtracepath);
				//if(i==2) break;
			}
			//Call Run.java for files that get generated after this.
		}
	}
}