package actors;

import java.util.ArrayList;
import java.util.HashMap;
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
	public ArrayList<LGGNode> verifiedLandmarks;

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

	//number of steps from root (current state) to undesirable state. If undesirable state occur multiple times, take the min distance
	public int computeDistanceToCriticalStateFromRoot(String domain){ 
		ArrayList<ArrayList<StateVertex>> dfsPaths = attackerState.getAllPathsFromRoot();
		ArrayList<ArrayList<StateVertex>> criticalpaths = new ArrayList<ArrayList<StateVertex>>(); //there could be 1 or more critical paths. take the min.
		for (ArrayList<StateVertex> path : dfsPaths) {
			boolean found = false;
			for (StateVertex stateVertex : path) {
				if(stateVertex.containsCriticalState(critical.getCriticalState())){
					found = true;
				}
			}
			if (found){
				criticalpaths.add(path);
			}
			//			if(path.get(path.size()-1).containsCriticalState(critical.getCriticalState())){ //TODO: see if code above will work for blocks example. if works then we can delete this code
			//				criticalpaths.add(path);
			//			}
		}
		int lens [] = new int [criticalpaths.size()];
		int index = 0;
		for (ArrayList<StateVertex> arrayList : criticalpaths) {
			int length = 0;
			for (StateVertex stateVertex : arrayList) {
				length++;
				//				System.out.println("vertex====="+stateVertex + " length->"+length);
				if(stateVertex.containsCriticalState(critical.getCriticalState())){
					length-=1;//count edges until first occurrence.
				}
			}
			//			System.out.println("dist to critical=========="+length);
			lens[index++]=length;
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
		return -1; //there are no critical paths. 1 node graph
	}

	//README: Call this method first before metric computation is called.
	public void setVerifiedLandmarks(RelaxedPlanningGraph at, ConnectivityGraph con, String lmoutput){
		LandmarkExtractor lm = new LandmarkExtractor(at, con);
		LGG lgg = lm.extractLandmarks(critical.getCriticalState());
		this.verifiedLandmarks = lm.verifyLandmarks(lgg, critical.getCriticalState(), getInitialState().getState(), lmoutput);
	}

	//number of remaining undesirable landmarks in the domain. 
	//Finding:: This is not a very telling feature. even if you are making your way down the undesirable path, the graph contains the full set of landmarks in the state space
	//This is because by following the path that undoes the action you just did you can add back previous states (and landmarks)
	public int countRemainingLandmarks(){
		int[] lmcounter = new int[this.verifiedLandmarks.size()];
		int currentLM = 0;
		int remainingLm = 0;
		ArrayList<StateVertex> visitOrder = attackerState.doBFSForStateTree(attackerState.getRoot());
		//		System.out.println("***************************************** "+visitOrder);
		for (LGGNode node : this.verifiedLandmarks) {
			//			System.out.println("verified lm=="+node);
			ArrayList<String> data = node.getValue();
			for (StateVertex v : visitOrder) {
				int count = 0;
				for (String item : data) {
					if(listContainsState(v.getStates(), item)){
						count++;
					}
				}
				if(count==data.size()){
					//					System.out.println("v has lm="+v);
					lmcounter[currentLM] = 1;
				}
			}
			currentLM++;
		}
		for (int item : lmcounter) {
			if(item==1){
				remainingLm++;
			}
		}
		return remainingLm;
	}

	//what percetage of landmarks does the root contain?
	public double percentOfLandmarksStateContain() {
		StateVertex root = attackerState.getRoot();
		int count = 0;
		for (String st : root.getStates()) {
			for (LGGNode node : this.verifiedLandmarks) {
				ArrayList<String> nodestate = node.getValue();
				if(listContainsState(nodestate, st)) {	
					count++;
				}
			}
		}
		return Double.valueOf(count)/Double.valueOf(verifiedLandmarks.size());
	}
	
	//from states that are immediately possible what percentage adds some landmark?
	public double stateAddsFactLandmark() {
		HashMap<StateVertex, TreeSet<StateVertex>> adjlist = attackerState.getAdjacencyList();
		TreeSet<StateVertex> rootsneighbors = adjlist.get(attackerState.getRoot());
		int nbrhavinglm = 0;
		for (StateVertex v : rootsneighbors) {
			ArrayList<String> vstate = v.getStates();
			int count = 0;
			for (String st : vstate) {
				for (LGGNode node : this.verifiedLandmarks) {
					ArrayList<String> nodestate = node.getValue();
					if(listContainsState(nodestate, st)) {	
						count++;
					}
				}
			}
			if(count>0) { //neighbor v has landmarks
				nbrhavinglm++;
			}
			
		}
		return Double.valueOf(nbrhavinglm)/Double.valueOf(rootsneighbors.size());
	}

	private boolean listContainsState(ArrayList<String> states, String state){ //state has surrounding paranthesis.
		for (String s : states) {
			if(s.equalsIgnoreCase(state)){
				return true;
			}
		}
		return false;
	}
}
