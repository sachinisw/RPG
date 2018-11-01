package actors;

import java.text.DecimalFormat;
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
import run.DesirableState;
import run.InitialState;

public class Decider extends Agent{

	public double attackerActionProbability;	
	public CriticalState critical;
	public DesirableState desirable;
	public String initFile;
	public StateGraph attackerState;
	public ArrayList<LGGNode> verifiedLandmarks;

	public Decider(String dom, String des, String pro, String out, String cri, String ini, String dotp, String dots) {
		super(dom, des, pro, out, cri, dotp, dots);
		this.attackerActionProbability = 0.1;
		this.initFile = ini;
	}

	public void setDesirableState(){
		desirable = new DesirableState(this.desirableStateFile);
		desirable.readStatesFromFile();
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

	public double[] computeMetrics() {
		setUndesirableState();
		setDesirableState();
		double c = computeCertaintyMetric();
		double [] prob = computeProbabilityMetrics();
		return new double[]{c,prob[0],prob[1]}; 
	}

	//1/11/18 disabling this for now. Since the current state(root) is based on my ability to know for sure the current state, there is no uncertainty in the system.
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

	//computes risk and desirability together, by BFS on graph once
	private double [] computeProbabilityMetrics() {
		double[] m = new double[2]; //[risk, desirability]
		ArrayList<StateVertex> visitOrder = attackerState.doBFSForStateTree(attackerState.getRoot());	
		for (StateVertex stateVertex : visitOrder) {
			if(stateVertex.containsState(attackerState.getCritical().getCriticalState())){
				m[0] = stateVertex.getStateProbability(); //give me the first one. i want the first instance where attack is generated.
				break;
			}
		}
		for (StateVertex stateVertex : visitOrder) {
			if(stateVertex.containsState(attackerState.getDesirable().getDesirable())){
				m[1] = stateVertex.getStateProbability(); //give me the first one. 
				break;
			}
		}
		return m;
	}
	
	public int [] computeDistanceMetrics(String domain) {
		int d[] = new int[2];
		ArrayList<ArrayList<StateVertex>> dfsPaths = attackerState.getAllPathsFromRoot();
		d[0] = getDistanceToStateFromRoot(dfsPaths, critical.getCriticalState());
		d[1] = getDistanceToStateFromRoot(dfsPaths, desirable.getDesirable());
		return d;
	}
		
	//number of steps from root (current state) to specified state state. If undesirable state occur multiple times, take the min distance
	public int getDistanceToStateFromRoot(ArrayList<ArrayList<StateVertex>> alldfs, ArrayList<String> state){ 
		ArrayList<ArrayList<StateVertex>> necessaryPaths = new ArrayList<ArrayList<StateVertex>>(); //there could be 1 or more critical paths. take the min.
		for (ArrayList<StateVertex> path : alldfs) {
			boolean found = false;
			for (StateVertex stateVertex : path) {
				if(stateVertex.containsState(state)){
					found = true;
				}
			}
			if (found){
				necessaryPaths.add(path);
			}
		}
		int lens [] = new int [necessaryPaths.size()];
		int index = 0;
		for (ArrayList<StateVertex> arrayList : necessaryPaths) {
			int length = 0;
			for (StateVertex stateVertex : arrayList) {
				length++;
				if(stateVertex.containsState(state)){
					length-=1;//count edges until first occurrence.
				}
			}
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
		return 0; //there are no critical paths. 1 node graph
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
		for (LGGNode node : this.verifiedLandmarks) {
			ArrayList<String> data = node.getValue();
			for (StateVertex v : visitOrder) {
				int count = 0;
				for (String item : data) {
					if(listContainsState(v.getStates(), item)){
						count++;
					}
				}
				if(count==data.size()){
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
		DecimalFormat decimalFormat = new DecimalFormat("##.##");
		String format = decimalFormat.format(Double.valueOf(count)/Double.valueOf(verifiedLandmarks.size()));
		return Double.valueOf(format);
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
