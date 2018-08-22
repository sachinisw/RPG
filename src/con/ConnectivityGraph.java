package con;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class ConnectivityGraph {

	private ArrayList<EffectElement> effectArray;
	private int levelMarker;
	private String conGraphID;
	private EffectElement element;
	private boolean preCondRead;
	private boolean addRead;
	private boolean delRead;
	
	private ArrayList<String> preconds;
	private ArrayList<String> addEffects;
	private ArrayList<String> delEffects;
	
	
	public ConnectivityGraph(String id){
		this.effectArray = new ArrayList<EffectElement>();
		this.levelMarker = 0;
		this.element = null;
		this.preCondRead = false;
		this.addRead = false;
		this.delRead = false;
		this.setConGraphID(id);
	}

	public ConnectivityGraph(ConnectivityGraph cg){
		ArrayList<EffectElement> els = new ArrayList<EffectElement>();
		els.addAll(cg.getEffectArray());
		
		this.effectArray = els;
		this.levelMarker = cg.getLevelMarker();
		this.element = cg.getElement();
		this.preCondRead = cg.isPreCondRead();
		this.addRead = cg.isAddRead();
		this.delRead = cg.isDelRead();
		this.setConGraphID(cg.getConGraphID());
	}
	
	public String readConGraphOutput(String filename){
		String outStr="";	
	    try {
            FileReader fileReader = new FileReader(filename);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((outStr = bufferedReader.readLine()) != null) {
                int levelCounter =  populate(outStr);             
                //stop after reading effects array. 
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
	
	public int populate(String inputString){
		String elId="";
		if(inputString.contains("EFFECT ARRAY")){
			this.levelMarker++;
		}
		
		Pattern p = Pattern.compile("effect\\s\\d{1,}\\sof");
		Matcher m = p.matcher(inputString);
		if(m.find()){
			elId = inputString;
			this.preconds = new ArrayList<>();
			this.addEffects = new ArrayList<>();
			this.delEffects = new ArrayList<>();
		}
		
		if(inputString.equals("----------PCS:")){
			this.preCondRead = true;
		}
		if(preCondRead && !inputString.equals("----------PCS:") && !inputString.equals("----------f_PCS:")){
			this.preconds.add(inputString);
		}
		if(inputString.equals("----------f_PCS:")){
			this.preCondRead = false;
		}
		if(inputString.equals("----------ADDS:")){
			this.addRead = true;
		}
		if(addRead && !inputString.equals("----------ADDS:") && !inputString.equals("----------DELS:")){
			this.addEffects.add(inputString);
		}
		if(inputString.equals("----------DELS:")){
			this.addRead = false;
		}
		if(inputString.equals("----------DELS:")){
			this.delRead = true;
		}
		if(delRead && !inputString.equals("----------DELS:") && !inputString.equals("----------INCREASE:")){
			this.delEffects.add(inputString);
		}
		if(inputString.equals("----------INCREASE:")){
			this.delRead = false;
		}
		if(!elId.isEmpty() ){
			this.element = new EffectElement(elId);
			this.element.setAddEffects(addEffects);
			this.element.setDelEffects(delEffects);
			this.element.setPreconds(preconds);
			this.effectArray.add(this.element);
		}
		return this.levelMarker;
	}
	
	public String findPredicateinAdd(String predicate){
		String result = "";
		
		for(int i=0; i<effectArray.size(); i++){
			EffectElement el = effectArray.get(i);
			ArrayList<String> addEffects = el.getAddEffects();
			
			for(int j=0; j<addEffects.size(); j++){
				String addel = addEffects.get(j);
				if(addel.toLowerCase().contains(predicate)){
					result += el.getEffectString()+"##";
				}
			}
		}
		return result;
	}
	
	public ArrayList<String> findActionsTriggeringPredicate(String predicate){
		String effectID = findPredicateinAdd(predicate);
		ArrayList <String> actions = new ArrayList<>();
						
		if(!effectID.isEmpty()){
			String [] effects = effectID.split("##");
			
			for(int i=0; i<effects.length; i++){
				int startIndex = effects[i].indexOf(":")+2;
				int endIndex =  effects[i].length();
				
				String temp = effects[i].substring(startIndex, endIndex);
				
				if(!temp.isEmpty()){
					actions.add(temp);

				}
			}
		}
		return actions;
	}
	
	public String effectArrayToString(){
		String outstr = "";
		
		for(int i=0; i<effectArray.size(); i++){
			outstr += effectArray.get(i) + "\n======================================\n";
		}
		return outstr;
	}

	public ArrayList<String> findStatesAddedByAction(String action){
		ArrayList<String> states = new ArrayList<>();
		
		//System.out.println("CALLING findStatesByAction( ) for " + action.toLowerCase().trim());
		for(int i=0; i<effectArray.size(); i++){
			EffectElement el = effectArray.get(i);
//			System.out.println("ACTIONNNNNNNNNNNNNNNNNNNNN   "+action.toLowerCase().trim());
//			System.out.println("EFFECT ELLLLLL   "+ el.toString());
//			System.out.println("EFFECT STRING        "+el.getEffectString().substring(el.getEffectString().indexOf(":")+2).toLowerCase());
			if(el.getEffectString().substring(el.getEffectString().indexOf(":")+2).toLowerCase().equalsIgnoreCase(action.replace("\t", " ").toLowerCase().trim())){
				ArrayList<String> addEffects = el.getAddEffects();
//				System.out.println("MATCH!!!!!!!!!!");
				for(int j=0; j<addEffects.size(); j++){
					states.add(addEffects.get(j));
				}	
			}	
		}
		//System.out.println("STATES in findStatesAddedByAction( ): "+states);
		return states;
	}
	
	public ArrayList<String> findPreconditionsofAction(String action){
		ArrayList<String> preconditionsofAction = new ArrayList<>();
		
		for(int i=0; i<effectArray.size(); i++){
			EffectElement el = effectArray.get(i);
			if(el.getEffectString().substring(el.getEffectString().indexOf(":")+2).toLowerCase().equalsIgnoreCase(action.toLowerCase().trim())){
				ArrayList<String> preconditions = el.getPreconds();
				//System.out.println(preconditions);
				for(int j=0; j<preconditions.size(); j++){
					preconditionsofAction.add(preconditions.get(j));
				}	
			}	
		}
		
		//for some cases, starting action, attacker action, this return list will be empty. 
		//looks like stats in the init do not get added to connectivity graph.....sigh!
		return preconditionsofAction;			
	}
	
	public ArrayList<String> findStatesDeletedByAction(String action){
		ArrayList<String> deleteStates = new ArrayList<>();
		for(int i=0; i<effectArray.size(); i++){
			EffectElement el = effectArray.get(i);
			if(el.getEffectString().substring(el.getEffectString().indexOf(":")+2).toLowerCase().equalsIgnoreCase(action.replace("\t", " ").toLowerCase().trim())){
				ArrayList<String> delEffects = el.getDelEffects();
				for(int j=0; j<delEffects.size(); j++){
					deleteStates.add(delEffects.get(j));
				}	
			}	
		}
		return deleteStates;
	}
	
	public ArrayList<String> findApplicableActionsInState(ArrayList<String> state){ //requires state predicates to be in parenthesis
		ArrayList<String> applicableActions = new ArrayList<String>();
		for(int i=0; i<effectArray.size(); i++){
			EffectElement el = effectArray.get(i);
			ArrayList<String> preconds = el.getPreconds();
			if(state.containsAll(preconds)){
				applicableActions.add(el.getEffectString().substring(el.getEffectString().indexOf(":")+2).trim());
			}
		}
		return applicableActions;
	}
	
	public String getConGraphID() {
		return conGraphID;
	}

	public void setConGraphID(String conGraphID) {
		this.conGraphID = conGraphID;
	}
	
	public ArrayList<EffectElement> getEffectArray(){
		return this.effectArray;
	}

	public int getLevelMarker() {
		return levelMarker;
	}

	public void setLevelMarker(int levelMarker) {
		this.levelMarker = levelMarker;
	}

	public EffectElement getElement() {
		return element;
	}

	public void setElement(EffectElement element) {
		this.element = element;
	}

	public boolean isPreCondRead() {
		return preCondRead;
	}

	public void setPreCondRead(boolean preCondRead) {
		this.preCondRead = preCondRead;
	}

	public boolean isAddRead() {
		return addRead;
	}

	public void setAddRead(boolean addRead) {
		this.addRead = addRead;
	}

	public boolean isDelRead() {
		return delRead;
	}

	public void setDelRead(boolean delRead) {
		this.delRead = delRead;
	}

	public ArrayList<String> getPreconds() {
		return preconds;
	}

	public void setPreconds(ArrayList<String> preconds) {
		this.preconds = preconds;
	}

	public ArrayList<String> getAddEffects() {
		return addEffects;
	}

	public void setAddEffects(ArrayList<String> addEffects) {
		this.addEffects = addEffects;
	}

	public ArrayList<String> getDelEffects() {
		return delEffects;
	}

	public void setDelEffects(ArrayList<String> delEffects) {
		this.delEffects = delEffects;
	}

	public void setEffectArray(ArrayList<EffectElement> effectArray) {
		this.effectArray = effectArray;
	}
	
}
