package actors;

import java.util.ArrayList;
import java.util.TreeSet;

import graph.StateGraph;
import graph.StateVertex;
import run.CriticalState;
import run.InitialState;

public class Attacker extends Agent{

	public double attackerActionProbability;	
	public CriticalState critical;
	public String initFile;
	public StateGraph attackerState;
	
	public Attacker(String dom, String des, String pro, String out, String cri, String ini, String dot) {
		super(dom, des, pro, out, cri, dot);
		this.attackerActionProbability = 0.1;
		this.initFile = ini;
	}
	
	public void setUndesirableState(){
		this.critical = new CriticalState(this.criticalStateFile);
		this.critical.readCriticalState();
	}
	
	public InitialState setInitialState(){
		InitialState init = new InitialState();
		init.readInitsFromFile(initFile);
		return init;
	}
	
	public CriticalState getUndesirableState(){
		return this.critical;
	}

	public void setState(StateGraph g){
		this.attackerState = g;
	}
	
	@Override
	public double[] computeMetric() {
		setUndesirableState();
		double c = computeCertaintyMetric();
		double r = computeRiskMetric();
		return new double[]{c,r};
	}
	
	private double computeCertaintyMetric(){
		StateVertex attakerRoot = this.attackerState.getRoot();
		TreeSet<StateVertex> rootNeighbors = this.attackerState.getAdjacencyList().get(attakerRoot);
		if(!rootNeighbors.isEmpty()){
			double neighbors = (double) rootNeighbors.size();
			return attakerRoot.getStateProbability()/neighbors;
		}else{
			return 1.0; //single root. no neighbors
		}
	}
	
	private double computeRiskMetric(){	
		ArrayList<StateVertex> visitOrder = attackerState.doBFSForStateTree(attackerState.getRoot());	
		for (StateVertex stateVertex : visitOrder) {
			if(stateVertex.isContainsCriticalState()){
				return stateVertex.getStateProbability(); //give me the first one. i want the first instance where attack is generated.
			}
		}
		return 0.0;
	}
}
