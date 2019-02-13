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

	public Decider(String dom, String domfile, String des, String pro, String out, String cri, String ini, String dotp, String dots) {
		super(dom, domfile, des, pro, out, cri, dotp, dots);
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

	public double[] computeProbabilityFeatures(ArrayList<ArrayList<StateVertex>> dpaths) {
		double c = computeCertaintyMetric();
		double [] prob = computeRiskDesirability(dpaths);
		return new double[]{c,prob[0],prob[1]}; 
	}

	public ArrayList<ArrayList<StateVertex>> getPathsLeadingToDesirable(){
		setUndesirableState();
		setDesirableState();
		return attackerState.getLikelyPathsForUser(attackerState.getDesirable().getDesirableStatePredicates(), domain);
	}

	//1/11/18 disabling this for now. 
	////Since the current state(root) is based on my ability to know for sure the current state, there is no uncertainty in the system.
	////we have sort of captured that with the uniform probability distributions for risk and desirability
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

	//computes risk and desirability together
	private double [] computeRiskDesirability(ArrayList<ArrayList<StateVertex>> dpth) {
		double[] m = new double[2]; //[risk, desirability]
		if(domain.equalsIgnoreCase("BLOCKS")){ //needs full and partial state matches
			m = computeRiskDesirabilityForActiveAttacker(dpth);
		}else { //by BFS on graph once
			m = computeRiskDesirabilityForPassiveAttacker(dpth);
		}
		return m;
	}

	private double[] computeRiskDesirabilityForActiveAttacker(ArrayList<ArrayList<StateVertex>> likelypaths) {
		ArrayList<ArrayList<StateVertex>> pathsmatchingcritical = new ArrayList<>();
		ArrayList<ArrayList<StateVertex>> onlydesirablepaths = new ArrayList<>();
		for (ArrayList<StateVertex> path : likelypaths) { //separate paths that reach the critical state while getting to desirable state
			boolean isCritical = false;
			for (StateVertex node : path) {
				if(node.containsPartialStateBlockWords(attackerState.getCritical().getCriticalStatePredicates(), true)){
					isCritical = true;
					break;
				}
			}
			if(isCritical) {
				pathsmatchingcritical.add(path);
			}else {
				onlydesirablepaths.add(path);
			}
		}
		double[] maxriskforeachpath = new double[pathsmatchingcritical.size()];
		int indexR = 0;
		for (ArrayList<StateVertex> path : pathsmatchingcritical) { //in paths triggering critical state, find the node that first goes into critical state. find the max risk across paths.
			double maxr = 0.0;
			for (StateVertex node : path) {
				if(node.containsPartialStateBlockWords(attackerState.getCritical().getCriticalStatePredicates(), true)) {
					if(node.getStateProbability()>maxr) {
						maxr = node.getStateProbability();
					}
				}
			}
			maxriskforeachpath[indexR++]=maxr;
		}
		double[] maxdesirabilityforeachpath = new double[onlydesirablepaths.size()];
		int indexD = 0;
		for (ArrayList<StateVertex> path : onlydesirablepaths) { //all likelypaths will have desirable state. get the ones that does not have undesirable state
			double maxd = 0.0;
			for (StateVertex node : path) {
				if(node.containsPartialStateBlockWords(attackerState.getDesirable().getDesirableStatePredicates(), false)) {
					if(node.getStateProbability()>maxd) {
						maxd = node.getStateProbability();
					}
				}
			}
			maxdesirabilityforeachpath[indexD++] = maxd;
		}
		//return sum of probability / size of likelipaths 
		double sumR = 0.0, sumD = 0.0;
		for (double d : maxdesirabilityforeachpath) {
			sumD += d;
		}
		for (double d : maxriskforeachpath) {
			sumR += d;
		}
		if(sumR==0.0 && sumD==0.0) {
			return new double [] {0.0, 0.0};
		}else if(sumR>0.0 && sumD==0.0) {
			return new double [] {sumR/(double)pathsmatchingcritical.size(), 0.0};
		}else if(sumR==0.0 && sumD>0.0){
			return new double [] {0.0, sumD/(double)(onlydesirablepaths.size())};
		}else {
			return new double [] {sumR/(double)pathsmatchingcritical.size(), sumD/(double)(onlydesirablepaths.size())};
		}
	}

	public double[] computeRiskDesirabilityForPassiveAttacker(ArrayList<ArrayList<StateVertex>> likelypaths) {
		ArrayList<ArrayList<StateVertex>> pathsmatchingcritical = new ArrayList<>();
		ArrayList<ArrayList<StateVertex>> onlydesirablepaths = new ArrayList<>();
		for (ArrayList<StateVertex> path : likelypaths) { //separate paths that reach the critical state while getting to desirable state 
			boolean isCritical = false;
			for (StateVertex node : path) {
				if(node.containsState(attackerState.getCritical().getCriticalStatePredicates())) {
					isCritical = true;
					break;
				}
			}
			if(isCritical) {
				pathsmatchingcritical.add(path);
			}else {
				onlydesirablepaths.add(path);
			}
		}
		double[] maxriskforeachpath = new double[pathsmatchingcritical.size()];
		int indexR = 0;
		for (ArrayList<StateVertex> path : pathsmatchingcritical) { //in paths triggering critical state, find the node that first goes into critical state. find the max risk across paths.
			double maxr = 0.0;
			for (StateVertex node : path) {
				if(node.containsState(attackerState.getCritical().getCriticalStatePredicates())) {
					if(node.getStateProbability()>maxr) {
						maxr = node.getStateProbability();
					}
				}
			}
			maxriskforeachpath[indexR++]=maxr;
		}
		double[] maxdesirabilityforeachpath = new double[onlydesirablepaths.size()];
		int indexD = 0;
		for (ArrayList<StateVertex> path : onlydesirablepaths) { //all likelypaths will have desirable state. Don't count the ones that has undesirable state
			double maxd = 0.0;
			for (StateVertex node : path) {
				if(node.containsState(attackerState.getDesirable().getDesirableStatePredicates())){
					if(node.getStateProbability()>maxd) {
						maxd = node.getStateProbability();
					}
				}
			}
			maxdesirabilityforeachpath[indexD++] = maxd;
		}
		//return the normalized probability (sum of maxr and maxd) / |likelypaths|
		double sumR = 0.0, sumD = 0.0;
		for (double d : maxdesirabilityforeachpath) {
			sumD += d;
		}
		for (double d : maxriskforeachpath) {
			sumR += d;
		}
		if(sumR==0.0 && sumD==0.0) {
			return new double [] {0.0, 0.0};
		}else if(sumR>0.0 && sumD==0.0) {
			return new double [] {sumR/(double)pathsmatchingcritical.size(), 0.0};
		}else if(sumR==0.0 && sumD>0.0){
			return new double [] {0.0, sumD/(double)(onlydesirablepaths.size())};
		}else {
			return new double [] {sumR/(double)pathsmatchingcritical.size(), sumD/(double)(onlydesirablepaths.size())};
		}
	}

	public int [] computeDistanceFeatures(ArrayList<ArrayList<StateVertex>> likelypaths) {
		int d[] = new int[2]; //[dist to critical, dist to desirable]
		if(domain.equalsIgnoreCase("BLOCKS")) { //allows state to be matched partially
			d[0] = getDistanceToCriticalForActiveAttacker(likelypaths);
			d[1] = getDistanceToDesirableForActiveAttacker(likelypaths);
		}else {
			d[0] = getDistanceToCriticalForPassiveAttacker(likelypaths);
			d[1] = getDistanceToDesirableForPassiveAttacker(likelypaths);
		}
		return d;
	}

	//from likelypaths that take the user to desirable state, filter out instances where attacker wins. 
	//count distance to first occurrence of critical state for each path. return mean
	public int getDistanceToCriticalForActiveAttacker(ArrayList<ArrayList<StateVertex>> alllikely){ 
		ArrayList<ArrayList<StateVertex>> criticalpaths = new ArrayList<ArrayList<StateVertex>>(); 
		int cpathcount = 0;
		for (ArrayList<StateVertex> path : alllikely) {
			boolean found = false;
			for (StateVertex stateVertex : path) {
				if(stateVertex.containsPartialStateBlockWords(attackerState.getCritical().getCriticalStatePredicates(), true)){
					found = true;
				}
			}
			if (found){
				criticalpaths.add(path);
				cpathcount++;
			}
		}
		int cLen [] = new int [cpathcount]; //there could be multiple paths that has the critical state
		int index = 0;
		for (ArrayList<StateVertex> currentpath : criticalpaths) {
			int length = 0;
			for (StateVertex stateVertex : currentpath) {
				length++;
				if(stateVertex.containsPartialStateBlockWords(attackerState.getCritical().getCriticalStatePredicates(), true)){
					length-=1;//count edges until first occurrence of state.
					break;
				}
			}
			cLen[index++]=length;
		}
		if(cpathcount>0){
			int sum = 0;
			for (int i : cLen) {
				sum+=i;
			}
			return (int) (Math.ceil((double)sum/(double)cLen.length)); 		//compute the mean distance across lens and return.
		}
		return -1; //there are no critical paths. 1 node graph
	}

	public int getDistanceToDesirableForActiveAttacker(ArrayList<ArrayList<StateVertex>> alllikely){ 
		ArrayList<ArrayList<StateVertex>> trulydesirablepaths = new ArrayList<ArrayList<StateVertex>>(); 
		for (ArrayList<StateVertex> path : alllikely) {
			boolean found = false;
			for (StateVertex stateVertex : path) {
				if(stateVertex.containsPartialStateBlockWords(attackerState.getCritical().getCriticalStatePredicates(), true)){
					found = true;
				}
			}
			if (!found){
				trulydesirablepaths.add(path);
			}
		}
		int dlens [] = new int [trulydesirablepaths.size()]; //there could be multiple paths that has the state
		int index = 0;
		for (ArrayList<StateVertex> arrayList : trulydesirablepaths) {
			int length = 0;
			for (StateVertex stateVertex : arrayList) {
				length++;
				if(stateVertex.containsPartialStateBlockWords(attackerState.getDesirable().getDesirableStatePredicates(), false)){
					length-=1;//count edges until first occurrence of state.
					break;
				}
			}
			dlens[index++]=length;
		}
		if(dlens.length>0){
			int sum = 0;
			for (int i : dlens) {
				sum+=i;
			}
			return (int) (Math.ceil((double)sum/(double)dlens.length)); 		//compute the mean distance across lens and return.
		}
		return -1; //there are no critical paths. 1 node graph
	}

	public int getDistanceToCriticalForPassiveAttacker(ArrayList<ArrayList<StateVertex>> alllikely){ 
		ArrayList<ArrayList<StateVertex>> criticalpaths = new ArrayList<ArrayList<StateVertex>>(); 
		int cpathcount = 0;
		for (ArrayList<StateVertex> path : alllikely) {
			boolean found = false;
			for (StateVertex stateVertex : path) {
				if(stateVertex.containsState(attackerState.getCritical().getCriticalStatePredicates())){
					found = true;
				}
			}
			if (found){
				criticalpaths.add(path);
				cpathcount++;
			}
		}
		int cLen [] = new int [cpathcount]; //there could be multiple paths that has the critical state
		int index = 0;
		for (ArrayList<StateVertex> currentpath : criticalpaths) {
			int length = 0;
			for (StateVertex stateVertex : currentpath) {
				length++;
				if(stateVertex.containsState(attackerState.getCritical().getCriticalStatePredicates())){
					length-=1;//count edges until first occurrence of state.
					break;
				}
			}
			cLen[index++]=length;
		}
		if(cpathcount>0){
			int sum = 0;
			for (int i : cLen) {
				sum+=i;
			}
			return (int) (Math.ceil((double)sum/(double)cLen.length)); 		//compute the mean distance across lens and return.
		}
		return -1; //there are no critical paths. 1 node graph
	}

	public int getDistanceToDesirableForPassiveAttacker(ArrayList<ArrayList<StateVertex>> alllikely){ 
		ArrayList<ArrayList<StateVertex>> trulydesirablepaths = new ArrayList<ArrayList<StateVertex>>(); 
		for (ArrayList<StateVertex> path : alllikely) {
			boolean found = false;
			for (StateVertex stateVertex : path) {
				if(stateVertex.containsState(attackerState.getCritical().getCriticalStatePredicates())){
					found = true;
				}
			}
			if (!found){
				trulydesirablepaths.add(path);
			}
		}
		int dlens [] = new int [trulydesirablepaths.size()]; //there could be multiple paths that has the state
		int index = 0;
		for (ArrayList<StateVertex> arrayList : trulydesirablepaths) {
			int length = 0;
			for (StateVertex stateVertex : arrayList) {
				length++;
				if(stateVertex.containsState(attackerState.getDesirable().getDesirableStatePredicates())){
					length-=1;//count edges until first occurrence of state.
					break;
				}
			}
			dlens[index++]=length;
		}
		if(dlens.length>0){
			int sum = 0;
			for (int i : dlens) {
				sum+=i;
			}
			return (int) (Math.ceil((double)sum/(double)dlens.length)); 		//compute the mean distance across lens and return.
		}
		return -1; //there are no critical paths. 1 node graph
	}

	//README: Call this method first before metric computation is called.
	public void generateVerifiedLandmarks(RelaxedPlanningGraph at, ConnectivityGraph con, String lmoutput){
		LandmarkExtractor lm = new LandmarkExtractor(at, con);
		LGG lgg = lm.extractLandmarks(critical.getCriticalStatePredicates());
		this.verifiedLandmarks = lm.verifyLandmarks(lgg, critical.getCriticalStatePredicates(), getInitialState().getState(), lmoutput);
	}

	//what percentage of active landmarks from all possible landmarks does the root contain?
	public double computePrecentActiveAttackLm() {
		StateVertex root = attackerState.getRoot();
		ArrayList<ArrayList<String>> cleanedlm = cleanLandmarks();
		int count = 0;
		for (String st : root.getStates()) {
			for (ArrayList<String> lm : cleanedlm) {
				if(listContainsState(lm, st)) {	
					count++;
				}
			}
		}
		System.out.println("ROOT=="+root);
		System.out.println(count + "/" + cleanedlm.size());
		DecimalFormat decimalFormat = new DecimalFormat("##.##");
		String format = decimalFormat.format(Double.valueOf(count)/Double.valueOf(cleanedlm.size()));
		return Double.valueOf(format);
	}

	private ArrayList<ArrayList<String>> cleanLandmarks(){ //remove landmarks that are equal to goal state. goal is trivial landmark
		ArrayList<ArrayList<String>> cleanedlm = new ArrayList<>();
		for (LGGNode node : this.verifiedLandmarks) {
			ArrayList<String> nodestate = node.getValue();
			if(!critical.getCriticalStatePredicates().equals(nodestate)) {
				cleanedlm.add(nodestate);
			}
		}
		return cleanedlm;
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
