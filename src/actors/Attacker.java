package actors;

import java.util.ArrayList;
import java.util.TreeSet;

import con.ConnectivityGraph;
import graph.StateGraph;
import graph.StateVertex;
import landmark.LGG;
import landmark.LGGNode;
import landmark.LandmarkExtractor;
import landmark.RelaxedPlanningGraph;
import run.CriticalState;
import run.InitialState;

public class Attacker extends Agent{

	public double attackerActionProbability;	
	public CriticalState critical;
	public String initFile;
	public StateGraph attackerState;
	
	public Attacker(String dom, String des, String pro, String out, String cri, String ini, String dotp, String dots) {
		super(dom, des, pro, out, cri, dotp, dots);
		this.attackerActionProbability = 0.1;
		this.initFile = ini;
	}
	
	public void setUndesirableState(){
		this.critical = new CriticalState(this.criticalStateFile);
		this.critical.readCriticalState();
	}
	
	public InitialState getInitialState(){
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
	
	private double computeRiskMetric(){	//probability of the node containing the first occurrence of undesirable state in attacker's state graph
		ArrayList<StateVertex> visitOrder = attackerState.doBFSForStateTree(attackerState.getRoot());	
		for (StateVertex stateVertex : visitOrder) {
			if(stateVertex.containsCriticalState(attackerState.getCritical().getCriticalState())){
				return stateVertex.getStateProbability(); //give me the first one. i want the first instance where attack is generated.
			}
		}
		return 0.0;
	}
	
	public int computeDistanceToCriticalStateFromRoot(){ //number of steps from root (current state) to undesirable state
		ArrayList<ArrayList<StateVertex>> dfsPaths = attackerState.getAllPathsFromRoot();
		ArrayList<ArrayList<StateVertex>> criticalpaths = new ArrayList<ArrayList<StateVertex>>(); //assumes multiple critical states. for now just 1
		for (ArrayList<StateVertex> path : dfsPaths) {
			if(path.get(path.size()-1).containsCriticalState(critical.getCriticalState())){
				criticalpaths.add(path);
			}
		}
		int length = 0;
		for (ArrayList<StateVertex> arrayList : criticalpaths) {
			for (StateVertex stateVertex : arrayList) {
				length++;
				if(stateVertex.containsCriticalState(critical.getCriticalState())){
					return length-1;//count edges until first occurrence.
				}
			}
		}
		return -1;
	}
	
	//number of remaining undesirable landmarks in the domain
	public int computeLandmarkMetric(RelaxedPlanningGraph at, ConnectivityGraph con){
		LandmarkExtractor lm = new LandmarkExtractor(at, con);
//		ArrayList<String> criticals = new ArrayList<String>();
//		for (String item : critical.getCriticalState()) { //make criticals lose paranthesis
//			criticals.add(item.substring(1, item.length()-1));
//		}
//		ArrayList<String> inits = new ArrayList<String>();
//		for (String item : getInitialState().getState()) { //make inits lose paranthesis
//			inits.add(item.substring(1, item.length()-1));
//		}
		LGG lgg = lm.extractLandmarks(critical.getCriticalState()); //OK to have paranthesis; TODO:: CHECK WITH ACTUAL DATA SET
		ArrayList<LGGNode> verified = lm.verifyLandmarks(lgg, critical.getCriticalState(), getInitialState().getState());
		int[] lmcounter = new int[verified.size()];
		int index = 0;
		int remainingLm = 0;
		ArrayList<StateVertex> visitOrder = attackerState.doBFSForStateTree(attackerState.getRoot());	
		for (LGGNode node : verified) {
			ArrayList<String> data = node.getValue();
			for (String item : data) {
				for (StateVertex v : visitOrder) {
					if(listContainsState(v.getStates(), item)){
						lmcounter[index] = 1;
					}
				}
			}
			index++;
		}
		for (int item : lmcounter) {
			if(item==1){
				remainingLm++;
			}
		}
		return remainingLm;
	}
	
	private boolean listContainsState(ArrayList<String> states, String state){
		for (String s : states) {
			if(s.substring(1, s.length()-1).equalsIgnoreCase(state)){
				return true;
			}
		}
		return false;
	}
}
