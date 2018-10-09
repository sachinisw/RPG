package con;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class EffectElement {

	private String effectString;
	private int effectID;
	private ArrayList<String> preconds;
	private ArrayList<String> addEffects;
	private ArrayList<String> delEffects;
	
	
	public EffectElement(String eid){
		this.effectString = eid;
		this.setEffectID(extractEffectId(eid));
		this.preconds = new ArrayList<>();
		this.addEffects = new ArrayList<>();
		this.delEffects = new ArrayList<>();
	}
	
	private int extractEffectId(String effect){
		int eId = -1;
		String copy = effect;
		
		String pattern = "effect\\s\\d{1,2}\\sto*";
		String match = "", matchout = "";

		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(copy);

		if (m.find( )) { 
			match = m.group();
		}

		if(!match.isEmpty()){
			matchout = match.replaceAll("\\D+","");	
		}
		
		if(!matchout.isEmpty()){
			return eId = Integer.parseInt(matchout);
		}

		return eId;
	}
	
	public String toString(){
		String outstr = "";
		String precon = precondToString();
		String adds = addToString();
		String dels = delToString();
		
		outstr = "EFFECT ID: " + effectString + "\nPRECOND:\n"+ precon +"\nADD:\n"+ adds + "\nDEL:\n"+ dels;
		return outstr;
	}
	
	public String precondToString(){
		String outstr = "";
		
		for(int i=0; i<this.preconds.size(); i++){
			outstr += preconds.get(i) + "\n";
		}
		
		return outstr;
	}
	
	public String addToString(){
		String outstr = "";
		
		for(int i=0; i<this.addEffects.size(); i++){
			outstr += addEffects.get(i) + "\n";
		}
		
		return outstr;
	}
	
	public String delToString(){
		String outstr = "";
		
		for(int i=0; i<this.delEffects.size(); i++){
			outstr += delEffects.get(i) + "\n";
		}
		
		return outstr;
	}
	
	
	
	public String getEffectString() {
		return effectString;
	}
	public void setEffectString(String effectID) {
		this.effectString = effectID;
	}

	public ArrayList<String> getPreconds() {
		return preconds;
	}

	public void setPreconds(ArrayList<String> arrayList) {
		this.preconds = arrayList;
	}

	public ArrayList<String> getAddEffects() {
		return addEffects;
	}

	public void setAddEffects(ArrayList<String> arrayList) {
		this.addEffects = arrayList;
	}

	public ArrayList<String> getDelEffects() {
		return delEffects;
	}

	public void setDelEffects(ArrayList<String> arrayList) {
		this.delEffects = arrayList;
	}

	public void setEffectID(int effectID) {
		this.effectID = effectID;
	}
	
	public int getEffectID(){
		return this.effectID;
	}
	
	
	
}
