package landmark;

import java.util.ArrayList;
import java.util.Arrays;

public class GraphLevel {
	private ArrayList<String> propositionLayer;
	private ArrayList<String> actionLayer;
	private String levelType;
	
	public GraphLevel(){
		propositionLayer = new ArrayList<>();
		actionLayer = new ArrayList<>();
		levelType = "";
	}

	public ArrayList<String> getPropositionLayer() {
		return propositionLayer;
	}

	public void setPropositionLayer(ArrayList<String> literals) {
		this.propositionLayer = literals;
	}

	public String getLevelType() {
		return levelType;
	}

	public void setLevelType(String levelType) {
		this.levelType = levelType;
	}
	
	public String toString(){
		return levelType + ": " + Arrays.toString(propositionLayer.toArray()) + "\n=========================\n"
				+ Arrays.toString(actionLayer.toArray()) + "\n\n";
	}

	public ArrayList<String> getActionLayer() {
		return actionLayer;
	}

	public void setActionLayer(ArrayList<String> effectLiterals) {
		this.actionLayer = effectLiterals;
	}
}
