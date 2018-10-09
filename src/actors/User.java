package actors;

import java.util.ArrayList;

import graph.StateGraph;
import graph.StateVertex;
import run.DesirableState;
import run.InitialState;

public class User extends Agent{
	
	public DesirableState desirable;
	public String initFile;
	public StateGraph userState;
	
	public User(String dom, String des, String pro, String out, String cri, String ini, String dotp, String dots){
		super(dom, des, pro, out, cri, dotp, dots);
		this.initFile = ini;
	}
	
	public void setDesirableState(){
		desirable = new DesirableState(this.desirableStateFile);
		desirable.readStatesFromFile();
	}
	
	public InitialState getInitialState(){
		InitialState init = new InitialState();
		init.readInitsFromFile(initFile);
		return init;
	}
	
	public DesirableState getDesirableState(){
		return desirable;
	}

	public void setState(StateGraph g){
		this.userState = g;
	}
	
	@Override
	public double[] computeMetric() {
		setDesirableState();
		double d = computeDesirableMetric();
		return new double[]{d};
	}
	
	private double computeDesirableMetric(){	//probability of the node containing the desirable state in user's state graph
		ArrayList<StateVertex> visitOrder = userState.doBFSForStateTree(userState.getRoot());
		for (StateVertex stateVertex : visitOrder) {
			if(stateVertex.containsDesirableState(userState.getDesirable().getDesirable())){
				return stateVertex.getStateProbability(); //give me the first one. 
			}
		}
		return 0.0;
	}
	
	public int computeDistanceToDesirableStateFromRoot(){ //number of steps from root (current state) to desirable state. there could be 1 or more desirable paths. take the min.
		ArrayList<ArrayList<StateVertex>> dfsPaths = userState.getAllPathsFromRoot();
		ArrayList<ArrayList<StateVertex>> desirablepaths = new ArrayList<ArrayList<StateVertex>>(); 
		for (ArrayList<StateVertex> path : dfsPaths) {
//			if(path.get(path.size()-1).containsCriticalState(desirable.getDesirable())){ //TODO: see Attacker.java line-91
//				desirablepaths.add(path);
//			}
			boolean found = false;
			for (StateVertex stateVertex : path) {
				if(stateVertex.containsCriticalState(desirable.getDesirable())){
					found = true;
				}
			}
			if (found){
				desirablepaths.add(path);
			}
		}
		int lens [] = new int [desirablepaths.size()];
		int index = 0;
		for (ArrayList<StateVertex> arrayList : desirablepaths) {
			int length = 0;
			for (StateVertex stateVertex : arrayList) {
				length++;
				if(stateVertex.containsCriticalState(desirable.getDesirable())){
					length-=1;//count edges until first occurrence.
				}
			}
			lens[index++]=length;//loop counts vertices. edges=vertex count-1
		}
		if(lens.length>0){
			int min = lens[0];
			for (int x=1; x<lens.length; x++) {
				if(min>lens[x]){
					min=lens[x];
				}
			}
			return min;
		}
		return -1; //there are no desirable paths. 1 node graph
	}
}
