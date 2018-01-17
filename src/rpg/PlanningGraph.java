package rpg;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PlanningGraph {

	private ArrayList<GraphLayer> RPG;
	private HashMap<Integer, GraphLayer> RPGL0;
	private int L0Counter;
	private int actionCounter;
	private int level;
	private boolean levelStart;
	private boolean readingFacts;
	private boolean readingEffects;
	private boolean atL0;
	private String rpgID;
	GraphLayer layer;
	GraphLayer l0Layer;
	
	public PlanningGraph(String id){
		
		this.RPG = new ArrayList<>();
		this.RPGL0 = new HashMap<Integer, GraphLayer>();
		this.L0Counter = 0;
		this.actionCounter = 0;
		this.setLevelStart(false);
		this.level = -1;
		this.readingFacts = false;
		this.readingEffects = false;
		this.atL0 = false;
		this.layer = null;
		this.l0Layer = null;
		this.setRpgID(id);
	}
	
	//this is used to read the entire output file. Then just extract L0 Effects
	public void readFFOutputL0(String filename){	
		String outStr="";
	    try {
            FileReader fileReader = new FileReader(filename);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while((outStr = bufferedReader.readLine()) != null) {
                populateL0(outStr);             
    		}    
            bufferedReader.close();            
	    } 
	    catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	public void populateL0(String inputStr){
		if(inputStr.equals("LEVEL 0:")){
			l0Layer = new GraphLayer();
			l0Layer.setLayerID(0);
			atL0 = true;
		}
		
		if(inputStr.trim().equalsIgnoreCase("FACTS:") && atL0 ){
			readingFacts = true;
		}else if (inputStr.trim().equalsIgnoreCase("EFS:") && atL0){
			readingEffects = true;
		}

		if(readingFacts && (inputStr.startsWith("(") )){
			l0Layer.addFact(inputStr);
		}
		if(inputStr.equals("") && readingFacts){
				readingFacts = false;
		}
		if(readingEffects && (inputStr.startsWith("effect") )){
			l0Layer.addEffect(inputStr);
		}
		if(inputStr.equals("") && readingEffects){
				readingEffects = false;
		}		
		//cond 1: will catch all L0 states before goal state. cond2: will catch goal state.
		if(inputStr.trim().equalsIgnoreCase("LEVEL 1:") && atL0 || inputStr.trim().equalsIgnoreCase("ff: found legal plan as follows") && atL0){ 
			RPGL0.put(actionCounter,l0Layer);
			this.actionCounter++;
			atL0 = false;
		}
	}
	
	public String toStringL0(){
		String outstr = "";
		
		Iterator<Entry<Integer, GraphLayer>> itRPG = RPGL0.entrySet().iterator();
		while(itRPG.hasNext()){
			Map.Entry<Integer, GraphLayer> pair = (Map.Entry<Integer, GraphLayer>)itRPG.next();
			GraphLayer layer = (GraphLayer) pair.getValue();
			outstr += (pair.getKey()+"  -----------    "+ layer.toString()) + "\n--------------------\n";
		}
		return outstr;
	}
	
	public GraphLayer getEffectsAtL0(String predicateEffect, int actionid){
		Iterator<Entry<Integer, GraphLayer>> itl0RPG = RPGL0.entrySet().iterator();
//		int result = -1;
		GraphLayer layer = null;
		
		while(itl0RPG.hasNext()){
			Map.Entry<Integer, GraphLayer> pair = (Map.Entry<Integer, GraphLayer>)itl0RPG.next();
			layer = (GraphLayer)pair.getValue();
			int actionID = (Integer)pair.getKey();
			
			layer.findEffectL0(predicateEffect);
			
			if(actionID==actionid){
				return layer;
			}
//			if(result != -1){
//				break;
//			}
		}
//		System.out.println("EFFECT SEARCHED:: " + predicateEffect);
		System.out.println("RPG ID::: " + rpgID);
		System.out.println("LAYER VALUES::  " + layer);
//		System.out.println("SERACH RESULT: " + result);
		 //return the graph layer corresponding to the action given in actionid
//		if (result > -1){
//			return layer;
//		}else{
//			return null;
//		}
		
		return null;

	}
	
	public int getFactsAtL0(String predicateFact){
		
		int findFact = -1;
		TreeSet<Integer> tree = new TreeSet<Integer>();
		
		for(int i=0; i<RPGL0.size(); i++){
			GraphLayer layer = RPGL0.get(i);
			findFact = layer.findFactLayer(predicateFact);
			
			if(findFact > -1){
				tree.add(findFact);
			}
		}
		
		return tree.first();
	}
		
	//this is to extract RPG after the first action
	public String readFFOutput(String filename){
		
		String outStr="";
		
	    try {
            FileReader fileReader = new FileReader(filename);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((outStr = bufferedReader.readLine()) != null) {
                int levelCounter =  populate(outStr);             
                //stop reading after 1 iteration. 
    			if(levelCounter > 1 )	{
    				break;
    			}else{
    				continue;
    			}	
    		}    

            bufferedReader.close();            
	    } 
	    catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
			e.printStackTrace();
		}
	    
	    return outStr;
	}
	
	public int populate(String inputStr){
	
		if(inputStr.equals("LEVEL 0:")){			
			this.L0Counter++;
		}

		Pattern p = Pattern.compile("LEVEL\\s\\d{1,}:");
		Matcher m = p.matcher(inputStr);

		if(m.find()){
			this.level++;
			layer = new GraphLayer();
			layer.setLayerID(level);	
			levelStart = true;
		}

		if(inputStr.equalsIgnoreCase("FACTS:")){
			readingFacts = true;

		}else if (inputStr.equalsIgnoreCase("EFS:")){
			readingEffects = true;
		}

		if(readingFacts && (inputStr.startsWith("(") )){
			layer.addFact(inputStr);

			if(inputStr.equals("")){
				readingFacts = false;
			}
		}

		if(readingEffects && (inputStr.startsWith("effect") )){
			layer.addEffect(inputStr);

			if(inputStr.equals("")){
				readingEffects = false;
			}
		}

		if(levelStart && L0Counter <= 1){
			//add layer to array list.
			//levelstart = false
			RPG.add(layer);
			levelStart = false;
		}

		return L0Counter;
	}

	public String toString(){
		String outstr = "";
		
		for (int i=0; i<RPG.size(); i++){
			outstr += RPG.get(i).toString() + "\n-------------------------------------------\n\n";
		}
		
		return outstr;
	}
	
	public int findEffect(String predicateEffect){
		
		int findEffect = -1;
		TreeSet<Integer> tree = new TreeSet<Integer>();
		
		for(int i=0; i<RPG.size(); i++){
			GraphLayer layer = RPG.get(i);
			findEffect = layer.findEffect(predicateEffect);
			
			if(findEffect > -1){
				tree.add(findEffect);
			}
		}
		
		return tree.first();
		
	}
	
	public int findFact(String predicateFact){
		
		int findFact = -1;
		TreeSet<Integer> tree = new TreeSet<Integer>();
		
		for(int i=0; i<RPG.size(); i++){
			GraphLayer layer = RPG.get(i);
			findFact = layer.findFactLayer(predicateFact);
			
			if(findFact > -1){
				tree.add(findFact);
			}
		}
		
		return tree.first();
	}
	
	public boolean isLevelStart() {
		return levelStart;
	}

	public void setLevelStart(boolean levelStart) {
		this.levelStart = levelStart;
	}

	public ArrayList<GraphLayer> getRPG() {
		return RPG;
	}

	public void setRPG(ArrayList<GraphLayer> rPG) {
		RPG = rPG;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getRpgID() {
		return rpgID;
	}

	public void setRpgID(String rpgID) {
		this.rpgID = rpgID;
	}

	public HashMap<Integer, GraphLayer> getRPGL0() {
		return RPGL0;
	}

	public void setRPGL0(HashMap<Integer, GraphLayer> rPGL0) {
		RPGL0 = rPGL0;
	}

	public boolean isAtL0() {
		return atL0;
	}

	public void setAtL0(boolean atL0) {
		this.atL0 = atL0;
	}

	public int getActionCounter() {
		return actionCounter;
	}

	public void setActionCounter(int actionCounter) {
		this.actionCounter = actionCounter;
	}
}
