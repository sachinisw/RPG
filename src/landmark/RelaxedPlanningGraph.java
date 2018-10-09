package landmark;

import java.util.ArrayList;

public class RelaxedPlanningGraph {
	private ArrayList<GraphLevel> levels;

	public RelaxedPlanningGraph(){
		levels = new ArrayList<GraphLevel>();
	}

	//returns RPG index of the first proposition/action layer that contains the fact/action.
	public int getLevelofAction(String action){
		for (GraphLevel graphLevel : levels) {
			ArrayList<String> actions = graphLevel.getActionLayer();
			for (String a : actions) {
				if(a.equalsIgnoreCase(action)){
					return Integer.parseInt(graphLevel.getLevelType());
				}
			}
		}
		return -1;
	}

	//returns RPG index of the first proposition/action layer that contains the fact/action.
	public int getLevelofEffect(String eff){
		for (GraphLevel graphLevel : levels) {
			ArrayList<String> facts = graphLevel.getPropositionLayer();
			for (String b : facts) {
				if(b.equalsIgnoreCase(eff)){
					return Integer.parseInt(graphLevel.getLevelType().trim());
				}
			}
		}
		return -1;
	}

	//returns true if graph contains all goal predicates
	public boolean containsGoal(ArrayList<String> goalpredicates){
		int [] check = new int [goalpredicates.size()];
		for (String g : goalpredicates) {
			int index = 0;
			for (GraphLevel l : levels) {
				ArrayList<String> facts = l.getPropositionLayer();
				for (String f : facts) {
					if(f.equalsIgnoreCase(g)){
						check[index]=1;
						index++;
					}
				}
			}
		}
		int checksum = 0;
		for (int i : check) {
			checksum+=i;
		}
		return (checksum==goalpredicates.size());
	}

	public ArrayList<GraphLevel> getLevels() {
		return levels;
	}

	public void setLevel(GraphLevel l){
		levels.add(l);
	}

	public GraphLevel getLevel(int index){
		return levels.get(index);
	}

	public String toString(){
		String out = "";
		for (GraphLevel graphLevel : levels) {
			out += graphLevel.toString() + "\n";
		}
		return out.substring(0, out.length()-1);
	}
}
