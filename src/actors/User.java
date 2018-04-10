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
	
	public User(String dom, String des, String pro, String out, String cri, String ini, String dot){
		super(dom, des, pro, out, cri, dot);
		this.initFile = ini;
	}
	
	public void setDesirableState(){
		desirable = new DesirableState(this.desirableStateFile);
		desirable.readStatesFromFile();
	}
	
	public InitialState setInitialState(){
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
	
	private double computeDesirableMetric(){	
		ArrayList<StateVertex> visitOrder = userState.doBFSForStateTree(userState.getRoot());
		for (StateVertex stateVertex : visitOrder) {
			if(stateVertex.isContainsDesirableState()){
				return stateVertex.getStateProbability(); //give me the first one. 
			}
		}
		return 0.0;
	}
}
