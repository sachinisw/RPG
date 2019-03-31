package rg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Problem implements Cloneable{
	private ArrayList<String> header;
	private ArrayList<String> init;
	private ArrayList<String> goal;
	private String problemPath;
	
	public Problem() {
		header = new ArrayList<>();
		init = new ArrayList<>();
		goal = new ArrayList<>();
		problemPath = "";
	}

	public void readProblemPDDL(String infile) {
		ArrayList<String> lines = new ArrayList<>();
		try {
			Scanner sc = new Scanner (new File(infile));
			while(sc.hasNextLine()) {
				lines.add(sc.nextLine());
			}
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		setObjects(lines);
		setProblemPath(infile);
	}

	public void setObjects(ArrayList<String> lines) {
		boolean initstart = false;
		boolean headerstart = false;
		boolean goalstart = false;
		for (String s : lines) {
			if(s.contains("define") || s.contains("domain")) {
				header.add(s);
			}else if(s.contains("(:init")) {
				initstart = true;
				headerstart = false;
			}else if(s.contains("(:goal")) {
				goalstart = true;
				initstart = false;
			}
			if(initstart) {
				init.add(s);
			}
			if(initstart && s.equals(")")) {
				initstart = false;
			}
			if(!initstart && !headerstart && goalstart) {
				goal.add(s);
			}
		}
	}

	public void writeProblemFile(String filename) {
		FileWriter writer = null;
		setProblemPath(filename);
		try {
			File file = new File(filename);
			writer = new FileWriter(file);
			for (String s : header) {
				writer.write(s);
				writer.write("\n");
			}
			for (String s : init) {
				writer.write(s);
				writer.write("\n");
			}
			for (String s : goal) {
				writer.write(s);
				writer.write("\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		Problem clone = null;
		try{
			clone = (Problem) super.clone();//do a deep copy here. because array list is a shallow copy by default
			ArrayList<String> cloneheader = new ArrayList<>();
			ArrayList<String> cloneinit = new ArrayList<>();
			ArrayList<String> clonegoal = new ArrayList<>();
			for (String string : goal) {
				clonegoal.add(string);
			}
			for (String string : init) {
				cloneinit.add(string);
			}
			for (String string : header) {
				cloneheader.add(string);
			}
			clone.setGoal(clonegoal);
			clone.setInit(cloneinit);
			clone.setHeader(cloneheader);
		}catch (CloneNotSupportedException e){
			throw new RuntimeException(e);
		}
		return clone;
	}
	
	public Problem negateGoal() { //create a clone and negate the predicates added from observations.
		Problem copy = null;
		try {
			copy = (Problem) clone();
			for (String g : copy.getGoal()) {
				if(g.contains("_")) {
					String parts [] = g.split("\\)\\(");
					String neg = "";
					for (String s : parts) {
						if(s.contains("obp")) {
							if(s.startsWith("(")) {
								s = "( not " + s + "))";
							}
							else {
								s = "( not (" + s + "))";
							}
						}else {
							s = "("+s;
						}
						neg+= s;
					}
					copy.getGoal().set(copy.getGoal().indexOf(g), neg);
				}
			}
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return copy;
	}
	
	public Problem addPredicateToGoal(String pred, String hyp) { 
		//create a clone and negate the predicates added from observations.
		Problem copy = null;
		try {
			copy = (Problem) clone();
			copy.getGoal().set(copy.getGoal().size()-3, pred+hyp);
		}catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return copy;
	}
	
	public ArrayList<String> getHeader() {
		return header;
	}
	public void setHeader(ArrayList<String> header) {
		this.header = header;
	}
	public ArrayList<String> getInit() {
		return init;
	}
	public void setInit(ArrayList<String> init) {
		this.init = init;
	}
	public ArrayList<String> getGoal() {
		return goal;
	}
	public void setGoal(ArrayList<String> goal) {
		this.goal = goal;
	}

	public String getProblemPath() {
		return problemPath;
	}

	public void setProblemPath(String problemPath) {
		this.problemPath = problemPath;
	}
}
