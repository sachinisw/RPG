package dot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Scanner;
import java.util.TreeSet;

/*
 * reads two dot files and merges ag (agent's dot) into adversary's dot */
public class DOTCompare {
	private ArrayList<String> agDOTFile;
	private ArrayList<String> adDOTFile;

	public DOTCompare(ArrayList<String> ag, ArrayList<String> ad){
		this.agDOTFile = ag;
		this.adDOTFile = ad;
	}

	/*
	 * ignore all nodes that have xxxx (this is the state I don't care for) in adversaryDOT.
	 * if node in ag == node in ad, Mark the agentDOT in adversaryDOT. produce a new compareDOT file*/
	public ArrayList<String> showAgentInAdversary(TreeSet<String> agent, TreeSet<String> adversary){
		ArrayList<String> compareDOTLines = new ArrayList<String>();
		TreeSet<String> properties = new TreeSet<String>();
		for(int i=0; i<this.adDOTFile.size()-1; i++){ //add all but last }
			compareDOTLines.add(this.adDOTFile.get(i));
		}
		Iterator<String> adIter = adversary.iterator();
		while(adIter.hasNext()){	//then add properties for similar nodes between ad and ag.
			String adNode = (String) adIter.next();
			String [] nodeParts_ad = adNode.substring(1,adNode.length()-1).split("\\\\n");
//			System.out.println(Arrays.toString(nodeParts_ad));
			Iterator<String> agIter = agent.iterator();
			while(agIter.hasNext()){
				String agNode = agIter.next();
				String [] nodeParts_ag = agNode.substring(1,agNode.length()-1).split("\\\\n");
				if(isStateEqual(nodeParts_ag, nodeParts_ad)){
					properties.add(adNode+"[style=filled,color=cyan]"); //change DOT property for the node. for now simply color it differently
				}
			}
		}
		compareDOTLines.addAll(properties);
		compareDOTLines.add(this.adDOTFile.get(this.adDOTFile.size()-1)); 		//add  last }
		return compareDOTLines;
	}

	public ArrayList<String> showAgentInAdversaryWithDeception(TreeSet<String> agent, TreeSet<String> adversary){
		ArrayList<String> compareDOTLines = new ArrayList<String>();
		TreeSet<String> properties = new TreeSet<String>();
		for(int i=0; i<this.adDOTFile.size()-1; i++){ //add all but last }
			compareDOTLines.add(this.adDOTFile.get(i));
		}
		Iterator<String> adIter = adversary.iterator();
		while(adIter.hasNext()){	//then add properties for similar nodes between ad and ag.
			String adNode = (String) adIter.next();
			String [] nodeParts_ad = adNode.substring(1,adNode.length()-1).split("\\\\n");
//			System.out.println(Arrays.toString(nodeParts_ad));
			Iterator<String> agIter = agent.iterator();
			while(agIter.hasNext()){
				String agNode = agIter.next();
				String [] nodeParts_ag = agNode.substring(1,agNode.length()-1).split("\\\\n");
				if(isStateEqualWithDeception(nodeParts_ag, nodeParts_ad)){
					properties.add(adNode+"[style=filled,color=cyan]"); //change DOT property for the node. for now simply color it differently
				}
			}
		}
		compareDOTLines.addAll(properties);
		compareDOTLines.add(this.adDOTFile.get(this.adDOTFile.size()-1)); 		//add  last }
		return compareDOTLines;
	}

	private TreeSet<String> extractNodeNames(ArrayList<String> lines){
		TreeSet<String> nodes = new TreeSet<String>();
		for(int i=0; i<lines.size(); i++){
			String item = lines.get(i);
			if(item.contains("->")){
				nodes.add(item.split("->")[0].trim());
				nodes.add(item.split("->")[1].substring(0, item.split("->")[1].indexOf("[")).trim());
			}
		}
		return nodes;
	}

	//ignoring xxxx all other elements must be equal
	private static boolean isStateEqual(String[] agState, String[] adState){
		ArrayList<String> ag = new ArrayList<String>();
		ArrayList<String> ad = new ArrayList<String>();
		for (String s : agState) {
			ag.add(s);
		}
		for (String string : adState) {
			if(!string.contains("XXXX")){
				ad.add(string);
			}
		}
		if(ag.size()!=ad.size()){
			return false;
		}else{
			Collections.sort(ag);
			Collections.sort(ad);
			return ag.equals(ad);
		}
	}

//	compare the agent's state node with adversary's state node using deception rule.
//	rule: ag=X O P [X==T], ad=XX X O P [X=T, XX=M]
//	compare states without probability number, which is the last element
		private static boolean isStateEqualWithDeception(String[] agState, String[] adState ){
			int [] check = new int[agState.length-1];
			for (int i=0; i<agState.length-1; i++) {
				for (int j=0; j<adState.length-1; j++) {
					if(!adState[j].contains("XXXX")){
						if(agState[i].equalsIgnoreCase(adState[j])){
							check[i]=1;
						}
					}else if(adState[j].contains("XXXX")){
						String partsAd[] = adState[j].split(" ");
						String optAd = partsAd[0].trim();
						String partsAg[] = agState[i].split(" ");
						String optAg = partsAg[0].trim();
						if(partsAd.length==partsAg.length){
							int count = 0;
							for(int x=1; x<partsAd.length; x++){
								if(partsAd[x].equals("XXXX") && partsAg[x].equals("XX")){
									count++;
								}else if(partsAd[x].equals(partsAg[x])){
									count++;
								}
							}
							if(optAd.equalsIgnoreCase(optAg) && count==partsAg.length-1){
								check[i]=1;
							}
						}
					}
				}
			}
			int sum = 0;
			for(int i=0; i<check.length; i++){
				sum += check[i];
			}
			return (sum==check.length);
		}

	public void writeCompareDOTFile(String filename, ArrayList<String> compareLines){
		FileWriter writer = null;
		try {
			File file = new File(filename);
			writer = new FileWriter(file);
			for(int i=0; i<compareLines.size(); i++){
				writer.write(compareLines.get(i));
				writer.write("\n");
			}	
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	public static ArrayList<String> readDOTFile(String filename){
		ArrayList<String> dotLines = new ArrayList<String>();
		try {
			Scanner scan = new Scanner(new File(filename));
			while(scan.hasNextLine()){
				String line = scan.nextLine();
				if(line.contains("->")){
					dotLines.add(line);
				}else{
					dotLines.add(line);
				}
			}
			scan.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return dotLines;
	}

	public ArrayList<String> getAgent() {
		return agDOTFile;
	}
	public void setAgent(ArrayList<String> agent) {
		this.agDOTFile = agent;
	}
	public ArrayList<String> getAdversary() {
		return adDOTFile;
	}
	public void setAdversary(ArrayList<String> ad) {
		this.adDOTFile = ad;
	}

	public static void main(String[] args) {
		String adDOT = "/home/sachini/BLOCKS/graph_ad_noreverse.dot";
		String agDOT = "/home/sachini/BLOCKS/graph_ag_noreverse.dot";
		String compareOut = "/home/sachini/BLOCKS/3_4_norev_prob.dot";

		ArrayList<String> ad = readDOTFile(adDOT);
		ArrayList<String> ag = readDOTFile(agDOT);

		DOTCompare cdot = new DOTCompare(ag, ad);
		TreeSet<String> adversaryNodes = cdot.extractNodeNames(ad);
		TreeSet<String> agentNodes = cdot.extractNodeNames(ag);
//		System.out.println(Arrays.toString(agentNodes.toArray()));
//		System.out.println(Arrays.toString(adversaryNodes.toArray()));

		//ArrayList<String> compareLines = cdot.showAgentInAdversary(agentNodes, adversaryNodes);
		ArrayList<String> compareLines = cdot.showAgentInAdversaryWithDeception(agentNodes, adversaryNodes);
		cdot.writeCompareDOTFile(compareOut, compareLines);
	}
}
