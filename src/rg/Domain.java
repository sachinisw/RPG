package rg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Domain implements Cloneable{
	private ArrayList<String> header;
	private ArrayList<String> constants;
	private ArrayList<String> predicates;
	private ArrayList<Action> actions;
	private String domainPath;
	
	public Domain() {
		header = new ArrayList<>();
		constants = new ArrayList<>();
		predicates = new ArrayList<>();
		actions = new ArrayList<>();
		domainPath = "";
	}

	public void readDominPDDL(String infile) {
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
		setDomainPath(infile);
	}

	public void setObjects(ArrayList<String> lines) {
		boolean conststart = false;
		boolean predsart = false;
		boolean acstart = false;
		boolean accont = false;
		String acname = "";
		for (String s : lines) {
			if(s.contains("define") || s.contains("requirements") 
					|| s.contains("types")) {
				header.add(s);
			}else if(s.contains("(:constants")) {
				conststart = true;
			}else if(s.contains("(:predicates")) {
				predsart = true;
			}else if(s.contains("(:action")) {
				acstart = true;
				predsart = false;
			}
			if(conststart) {
				constants.add(s);
			}
			if(conststart && s.contains(")")) {
				conststart = false;
			}
			if(predsart) {
				predicates.add(s);
			}
			if(acstart) {
				Action ac = new Action(s.split(" ")[1]);
				actions.add(ac);
				acstart = false;
				accont = true;
				acname = s.split(" ")[1];
			}
			if(accont) {
				if(s.contains(":parameters")) {
					AcParameters acp = new AcParameters();
					String[] parts = s.substring(s.indexOf("(")+1,s.indexOf(")")).split("\\?");
					for (String st : parts) {
						if(!st.isEmpty()) {
							acp.addParameter("?"+st.trim());
						}
					}
					for (Action ac : actions) {
						if(ac.getHeader().contains(acname)) {
							ac.setParams(acp);
						}
					}
				}else if(s.contains(":precondition")) {
					AcPrecondition pre = new AcPrecondition();
					if(s.contains("(and")) { //multiple preconds
						String pattern = "\\((.*?)\\){1,2}";
						Pattern r = Pattern.compile(pattern);
						Matcher m = r.matcher(s.substring(5,s.length()-1));
						while (m.find()) {
							pre.addPrecondition(m.group());
						}
					}else { //single precond
						pre.addPrecondition(s.substring(s.indexOf("("),s.indexOf(")")));
					}
					for (Action ac : actions) {
						if(ac.getHeader().contains(acname)) {
							ac.setPreconds(pre);
						}
					}
				}else if(s.contains(":effect")) {
					AcEffect eff = new AcEffect();
					if(s.contains("(and")) { //multiple effects
						String pattern = "\\((.*?)\\){1,2}";
						Pattern r = Pattern.compile(pattern);
						Matcher m = r.matcher(s.substring(5,s.length()-1));
						while (m.find()) {
							eff.addEffect(m.group());
						}
					}else { //single effect
						eff.addEffect(s.substring(s.indexOf("("),s.indexOf(")")));
					}
					for (Action ac : actions) {
						if(ac.getHeader().contains(acname)) {
							ac.setEffects(eff);
						}
					}
					accont = false;
					acname ="";
				}
			}
		}
	}

	public void addPredicate(String predicate) { //add a new predicate to the beginning if it doesn't already exist
		boolean found = false;
		for (String p : predicates) {
			if(p.equalsIgnoreCase(predicate)) {
				found = true;
				break;
			}
		}
		if(!found) {
			predicates.add(1, predicate);
		}
	}
	
	public void writeDomainFile(String filename) {
		FileWriter writer = null;
		setDomainPath(filename);
		try {
			File file = new File(filename);
			writer = new FileWriter(file);
			for (String s : header) {
				writer.write(s);
				writer.write("\n");
			}
			writer.write("\n");
			for (String s : constants) {
				writer.write(s);
				writer.write("\n");
			}
			writer.write("\n");
			for (String s : predicates) {
				writer.write(s);
				writer.write("\n");
			}
			for (Action s : actions) {
				writer.write(s.toString());
				writer.write("\n");
			}
			writer.write(" )\n");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	public String createPrediateFromObservation(String obs) {
		String [] parts = obs.split(" ");
		String namesuffix = "_";
		for (int i=1; i<parts.length; i++) {
			namesuffix+=parts[i]+"-";
		}
		return "(obp_"+parts[0]+namesuffix.substring(0,namesuffix.length()-1)+")";
	}

	//ramirez geffener 2009 (2010 compile observation method for a->b is not clear. it's the same thing has 2009 with just that change)
	public Domain compileObservation (String curO, String prevO) { //create a grounded action from this observation. when grounded no parameters
		Domain domCopy = null;
		try {
			domCopy = (Domain) this.clone();
			Action acCopy = domCopy.findAction(curO.split(" ")[0]);
			Action acCopyClone = (Action) acCopy.clone();
			if(acCopyClone!=null) {
				String predicate = createPrediateFromObservation(curO);
				String prevPred = "";
				if(!prevO.isEmpty()) {
					prevPred = createPrediateFromObservation(prevO);
				}
				String header_new = "(:action ob_"+predicate.substring(4,predicate.length()-1)+"\n";
				//domCopy.getPredicates().add(1,predicate); //add the new predicate for observation to domain
				domCopy.addPredicate(predicate);
				acCopyClone.setHeader(header_new);
				HashMap<String, String> maps = new HashMap<>();
				String pattern = "\\?([a-z,A-Z]{0,})";
				Pattern r = Pattern.compile(pattern);
				int id=1;
				for (String string : acCopyClone.getParams().getParamlist()) { //ground variables
					Matcher m = r.matcher(string);
					while (m.find()) {
						maps.put(m.group().toUpperCase(),curO.split(" ")[id++]);
					}
				}
				acCopyClone.getParams().getParamlist().clear();
				for (String string : acCopyClone.getPreconds().getPredicates()) { //ground preconditions
					Matcher m = r.matcher(string);
					int idx = acCopyClone.getPreconds().getPredicates().indexOf(string);
					while (m.find()) {
						string = string.replace(m.group(), maps.get(m.group().toUpperCase()));
						acCopyClone.getPreconds().getPredicates().set(idx, string);
					}
				}
				acCopyClone.getPreconds().getPredicates().add(prevPred); //add prev obs predicate as a precondition
				for (String string : acCopyClone.getEffects().getPredicates()) { //ground effects
					Matcher m = r.matcher(string);
					int idx = acCopyClone.getEffects().getPredicates().indexOf(string);
					while (m.find()) {
						string = string.replace(m.group(), maps.get(m.group().toUpperCase()));
						acCopyClone.getEffects().getPredicates().set(idx, string);
					}
				}
				acCopyClone.getEffects().getPredicates().add(predicate); //add the new predicate for observation to effects
				domCopy.getActions().add(acCopyClone);
			}
		} catch (CloneNotSupportedException e1) {
			e1.printStackTrace();
		}
		return domCopy;
	}


	@Override
	public Object clone() throws CloneNotSupportedException {
		Domain clone = null;
		try{
			clone = (Domain) super.clone();//do a deep copy here. because array list is a shallow copy by default
			ArrayList<String> cloneparams = new ArrayList<>();
			ArrayList<String> cloneconsts = new ArrayList<>();
			ArrayList<Action> cloneactions = new ArrayList<>();
			ArrayList<String> cloneheader = new ArrayList<>();
			for (String string : predicates) {
				cloneparams.add(string);
			}
			for (Action ac : actions) {
				cloneactions.add((Action) ac.clone());
			}
			for (String string : constants) {
				cloneconsts.add(string);
			}
			for (String string : header) {
				cloneheader.add(string);
			}
			clone.setPredicates(cloneparams);
			clone.setActions(cloneactions);
			clone.setConstants(cloneconsts);
			clone.setHeader(cloneheader);
		}catch (CloneNotSupportedException e){
			throw new RuntimeException(e);
		}
		return clone;
	}

	public Action findAction(String ac) {
		for (Action action : actions) {
			if(action.getHeader().toLowerCase().contains(ac.toLowerCase())) {
				return action;
			}
		}
		return null;
	}

	public ArrayList<String> getHeader() {
		return header;
	}

	public void setHeader(ArrayList<String> header) {
		this.header = header;
	}

	public ArrayList<String> getConstants() {
		return constants;
	}

	public void setConstants(ArrayList<String> constants) {
		this.constants = constants;
	}

	public ArrayList<String> getPredicates() {
		return predicates;
	}

	public void setPredicates(ArrayList<String> predicates) {
		this.predicates = predicates;
	}

	public ArrayList<Action> getActions() {
		return actions;
	}

	public void setActions(ArrayList<Action> actions) {
		this.actions = actions;
	}

	public String getDomainPath() {
		return domainPath;
	}

	public void setDomainPath(String domainPath) {
		this.domainPath = domainPath;
	}
}
