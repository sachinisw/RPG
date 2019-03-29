package igplan;
import java.util.ArrayList;

import con.ConnectivityGraph;

//Plan generated with Metric-FF
public class Plan {

	private ArrayList<String> planSteps;
	private String planID;
	private boolean solved;
	private int stepCount;

	public Plan(ArrayList<String> arr, String id){
		this.setPlanSteps(arr);
		this.setPlanID(id);
		if(arr.size()==0){
			this.solved = false;
			this.setStepCount(0);
		}else{
			this.solved = true;
			this.stepCount = arr.size();
		}
	}

	public Plan(Plan toCopy){
		ArrayList<String> stepsCopy = new ArrayList<String>();
		stepsCopy.addAll(toCopy.getPlanSteps());
		
		this.setPlanSteps(stepsCopy);
		this.setPlanID(toCopy.getPlanID());
		this.solved = toCopy.solved;
		this.stepCount = toCopy.getStepCount();
	}
	
	public boolean compareIDs(String id){
		if(id.substring(id.indexOf("plan-"), id.length()).equals(planID.substring(planID.indexOf("plan-"), planID.length()))){
			return true;
		}
		return false;
	}
	public void addPlanStep(String step){
		planSteps.add(step);
	}
	
	public ArrayList<String> getPlanSteps() {
		return planSteps;
	}

	public void setPlanSteps(ArrayList<String> planSteps) {
		this.planSteps = planSteps;
	}

	public String getPlanID() {
		return planID;
	}

	public void setPlanID(String planID) {
		this.planID = planID;
	}
	
	public String toString(){
		String str = "", strtemp = "";
		
		for(int i=0; i<planSteps.size(); i++){
			strtemp += planSteps.get(i) + "||";
		}
		return str +planID + "->" + strtemp;
	}

	public boolean isSolved() {
		return solved;
	}

	public void setSolved(boolean solved) {
		this.solved = solved;
	}

	public int getStepCount() {
		return stepCount;
	}

	public void setStepCount(int stepCount) {
		this.stepCount = stepCount;
	}
	
	public ArrayList<ArrayList<String>> getStateSequence(ArrayList<String> init, ConnectivityGraph g){
		ArrayList<ArrayList<String>> seq = new ArrayList<>();
		int current = 0;
		seq.add(init);
		for(int i=0; i<getPlanSteps().size(); i++){
			String ac = getPlanSteps().get(i).substring(getPlanSteps().get(i).indexOf(":")+1,getPlanSteps().get(i).length()).trim();
			ArrayList<String> copy = new ArrayList<String>();
			copy.addAll(seq.get(current));
			for(int a=0; a<g.findStatesDeletedByAction(ac).size(); a++){
				copy.remove(g.findStatesDeletedByAction(ac).get(a));
			}
			copy.addAll(g.findStatesAddedByAction(ac));
			seq.add(copy);
			current++;
		}
		return seq;
	}
}
