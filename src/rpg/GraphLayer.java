package rpg;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GraphLayer {

	private ArrayList<String> effects;
	private ArrayList<String> facts;
	private int layerID;
	
	public GraphLayer(){
		
		this.effects = new ArrayList<>();
		this.facts = new ArrayList<>();
		this.layerID = 0;
		
	}

	public void addEffect(String effectStr){
		this.effects.add(effectStr);
	}
	
	public void addFact(String factStr){
		this.facts.add(factStr);
	}
	
	public int findEffect(String effect){
		if(!effect.isEmpty()){
			for(int i=0; i<effects.size(); i++){
				String str = effects.get(i);	

				String pattern = "effect\\s\\d{1,2}\\sto*";
				String match = "";

				Pattern r = Pattern.compile(pattern);
				Matcher m = r.matcher(str);

				if (m.find( )) {
					match = m.group();
				}

				String compare = str.substring(match.length()+1, str.length());
//				if(str.indexOf(effect) >= 0){
//					return layerID;
//				}
				if(compare.equalsIgnoreCase(effect)){
					return layerID;
				}
				
			}
		}
		return -1;
	}
	
	
	public int findEffectL0(String effect){
		if(!effect.isEmpty()){
			for(int i=0; i<effects.size(); i++){
				String str = effects.get(i);	
				//System.out.println(str+"&&&&&&&&&&");
				if(str.contains(effect)){
					//System.out.println("MAAAAAAAAAAATCH");
					return 1;
				}		
			}
		}
		return -1;
	}
	
	
	public int findFactLayer(String fact){
		
		if(!fact.isEmpty()){
			for(int i=0; i<facts.size(); i++){
				String str = facts.get(i);
				String lettersonly = str.substring(1, str.length()-1);
//				if(str.indexOf(fact) >= 0){
//					return layerID;
//				}
				if(lettersonly.equalsIgnoreCase(fact)){
					return layerID;
				}
			}
		}
		return -1;
	}
	
	public String toString(){
		String outstr, factStr = "", effectStr = "";
		
		for(int i=0; i<facts.size(); i++){
			factStr += facts.get(i) + "\n";
		}
		
		for(int i=0; i<effects.size(); i++){
			effectStr += effects.get(i) + "\n";
		}
		
		outstr = "LAYER " + layerID + " \n\nFACTS:\n" + factStr + " \n\nEFFECTS:\n" + effectStr;
		return outstr;
	}
	
	
	public ArrayList<String> getEffects() {
		return effects;
	}

	public void setEffects(ArrayList<String> effects) {
		this.effects = effects;
	}

	public ArrayList<String> getFacts() {
		return facts;
	}

	public void setFacts(ArrayList<String> facts) {
		this.facts = facts;
	}

	public int getLayerID() {
		return layerID;
	}

	public void setLayerID(int layerID) {
		this.layerID = layerID;
	}
}
