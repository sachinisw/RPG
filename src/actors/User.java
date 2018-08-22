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
	
	public int computeDistanceToDesirableStateFromRoot(){ //number of steps from root (current state) to desirable state
		ArrayList<ArrayList<StateVertex>> dfsPaths = userState.getAllPathsFromRoot();
		ArrayList<ArrayList<StateVertex>> desirablepaths = new ArrayList<ArrayList<StateVertex>>(); //assumes multiple desirable states. for now just 1
		for (ArrayList<StateVertex> path : dfsPaths) {
			if(path.get(path.size()-1).containsCriticalState(desirable.getDesirable())){
				desirablepaths.add(path);
			}
		}
		int length = 0;
//		for (ArrayList<StateVertex> arrayList : desirablepaths) {
//			System.out.println(Arrays.toString(arrayList.toArray()));
//		}
		for (ArrayList<StateVertex> arrayList : desirablepaths) {
			for (StateVertex stateVertex : arrayList) {
				length++;
//				System.out.println("vertex"+ stateVertex + "\nlen="+length);
				if(stateVertex.containsCriticalState(desirable.getDesirable())){
//					System.out.println("breaking=="+ stateVertex + " len="+(length-1));
					return length-1;//count edges until first occurrence.
				}
			}
		}
		return -1;
	}
}
