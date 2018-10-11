package landmark;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import con.ConnectivityGraph;

public class RelaxedPlanningGraphGenerator {
	public boolean atL;
	public boolean propositionLevel;
	public boolean actionLevel;
	public boolean first;
	public int currentLevel;
	public int counter;
	public RelaxedPlanningGraph rpg;
	public ConnectivityGraph con;

	public RelaxedPlanningGraphGenerator(){
		rpg = new RelaxedPlanningGraph();
		con = new ConnectivityGraph("");
		propositionLevel = false;
		actionLevel = false;
		atL = false;
		currentLevel = 0;
		counter = 0;
		first = true;
	}

	public void populate(String inputStr){
		GraphLevel l = null;
		Pattern p0 = Pattern.compile("LEVEL\\s[0-9]:{1,}");
		Matcher m0 = p0.matcher(inputStr.trim());
		if(m0.find()){
			l = new GraphLevel();
			l.setLevelType(inputStr.substring(5, inputStr.length()-1));
			currentLevel = Integer.parseInt(inputStr.substring(5, inputStr.length()-1).trim());
			if(inputStr.substring(5, inputStr.length()-1).trim().equals("0")){
				atL = true;
				first = false;
				counter++;
			}
			if(counter<2 && !first){
				rpg.setLevel(l);
			}
		}
		if(counter<2){
			if(inputStr.trim().equalsIgnoreCase("FACTS:") && atL){
				propositionLevel = true;
			}else if (inputStr.trim().equalsIgnoreCase("EFS:") && atL){
				actionLevel = true;
			}
			if(propositionLevel && (inputStr.startsWith("(") )){
				rpg.getLevel(currentLevel).getPropositionLayer().add(inputStr.trim());
			}
			if(inputStr.trim().equals("") && propositionLevel){
				propositionLevel = false;
			}
			if(actionLevel && (inputStr.startsWith("effect") )){
				String eff = inputStr.substring(inputStr.indexOf("to")+3,inputStr.length()).trim();
				rpg.getLevel(currentLevel).getActionLayer().add(eff);
			}
			if(inputStr.trim().equals("") && actionLevel){
				actionLevel = false;
			}
		}
	}

	public void readFFOutput(String filename){	
		String outStr="";
		try {
			FileReader fileReader = new FileReader(filename);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			while((outStr = bufferedReader.readLine()) != null) {
				populate(outStr);             
			}    
			bufferedReader.close();            
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<ConnectivityGraph> readConnectivityGraphs(String inputfile){
		ArrayList<ConnectivityGraph> connectivities = new ArrayList<ConnectivityGraph>();
		ConnectivityGraph graph = new ConnectivityGraph(inputfile);
		graph.readConGraphOutput(inputfile);
		connectivities.add(graph);
		return connectivities;
	}

	public void runLandmarkGenerator(String inputfilerpg, String inputfilecon, ArrayList<String> critical, ArrayList<String> init, String lmoutput) {
		RelaxedPlanningGraphGenerator test = new RelaxedPlanningGraphGenerator();
		test.readFFOutput(inputfilerpg);
		ArrayList<ConnectivityGraph> cons = test.readConnectivityGraphs(inputfilecon);
//		System.out.println(test.rpg.toString());
		LandmarkExtractor lm = new LandmarkExtractor(test.rpg, cons.get(0));
		LGG lgg = lm.extractLandmarks(critical);
		lm.verifyLandmarks(lgg, critical, init, lmoutput); //writes cleaned landmarks to lmoutput
	}
	
	public static void main(String[] args) {
		RelaxedPlanningGraphGenerator rpgen= new RelaxedPlanningGraphGenerator();
		String inputfilerpg = "/home/sachini/BLOCKS/scenarios/30/outs/attacker/rpg-problem-a";
		String inputfilecon = "/home/sachini/BLOCKS/scenarios/30/outs/attacker/connectivity-problem-a";
		String lmoutput = "/home/sachini/BLOCKS/scenarios/30/outs/verifiedlm.txt";
		ArrayList<String> critical = new ArrayList<String>();
		ArrayList<String> init = new ArrayList<String>();
		critical.add("(ON C A)"); //prob 30
		critical.add("(ON B D)");
		init.add("(HANDEMPTY)");
		init.add("(CLEAR A)");
		init.add("(ONTABLE A)");
		init.add("(CLEAR B)");
		init.add("(ONTABLE B)");
		init.add("(ONTABLE C)");
		init.add("(ON D C)");
		init.add("(CLEAR D)");
		 
//		critical.add("(AT D)"); //prob 31. Give these with paranthesis
//		init.add("(AT A)");
//		init.add("(ADJ A B)");
//		init.add("(ADJ A E)");
//		init.add("(ADJ B C)");
//		init.add("(ADJ C D)");
//		init.add("(ADJ A D)");
		rpgen.runLandmarkGenerator(inputfilerpg, inputfilecon, critical, init, lmoutput);
	}
}
