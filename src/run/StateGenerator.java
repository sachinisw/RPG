package run;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import con.ConnectivityGraph;
import graph.ActionEdge;
import graph.GraphDOT;
import graph.StateGraph;
import graph.StateVertex;
import plan.Plan;
import plan.PlanExtractor;
import rpg.PlanningGraph;

public class StateGenerator {
	public final static String ffPath = "/home/sachini/BLOCKS/Metric-FF-new/ff";
	public final static String domain = "blocks";//changes stoppable condition.
	public final static String domainFile = "/home/sachini/BLOCKS/domain.pddl";
	public final static String desirableStateFile = "/home/sachini/BLOCKS/states4.txt";//empty for attacker. one line for user's desirable goal
	public final static String problemFile = "/home/sachini/BLOCKS/problem_4.pddl";
	public final static String outputPath = "/home/sachini/BLOCKS/outs/"; //clean this directory before running. if not graphs will be wrong
	public final static String criticalStateFile = "/home/sachini/BLOCKS/critical.txt";
	public final static String dotFilePrefix = "/home/sachini/BLOCKS/graph_ad_noreverse_";
	public final static String dotFileExt = ".dot";
	public final static double initProbability = 1.0;
	public final static double attackerActionProbability = 0.1;	

	public void writeConsoleOutputtoFile(String outfile, String text){
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(outfile, "UTF-8");
			writer.write(text);
			writer.println();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}finally{
			writer.close();
		}
	}

	public String executeShellCommand(String command){
		StringBuilder sb = new StringBuilder();
		try {
			Process p = Runtime.getRuntime().exec(command);
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";           
			while ((line = reader.readLine())!= null) {
				sb.append(line + "\n");
			}
			p.waitFor();
		} catch (IOException e) {
			System.err.println("TraceGenerator:runFF()"+ command+ " " + e.getMessage());
		} catch (InterruptedException e) {
			System.err.println("TraceGenerator:runFF()"+ command+ " " + e.getMessage());
		}
		return sb.toString();
	}

	public void runFF(int config, String domainpath, String probfilename){
		switch (config){
		case 1: //create plan
			String command = ffPath+ " -o "+ domainpath+ " -f "+ probfilename;
			String plan = executeShellCommand(command);
			writeConsoleOutputtoFile(outputPath +"plan-"+probfilename.substring(probfilename.length()-14,probfilename.length()-5), plan);
			break;
		case 2:	//create relaxed planning graph
			String command_rpg = ffPath +" -o "+ domainpath+ " -f "+ probfilename+" -i 126";
			String rpg = executeShellCommand(command_rpg);
			writeConsoleOutputtoFile(outputPath +"rpg-"+probfilename.substring(probfilename.length()-14,probfilename.length()-5), rpg);
			break;
		case 3:	//create connectivity graph
			String command_con = ffPath +" -o "+ domainpath+ " -f "+ probfilename+" -i 125";
			String con = executeShellCommand(command_con);
			writeConsoleOutputtoFile(outputPath+"connectivity-"+probfilename.substring(probfilename.length()-14,probfilename.length()-5), con);
			break;
		default:
			System.out.println("UNSUPPORTED COMMAND");
			break;
		}
	}

	public ArrayList<String> getRPGFiles(){		
		ArrayList<String> rpgFilePaths = new ArrayList<String>();
		try {
			File dir = new File(outputPath);
			List<File> files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
			for (File fileItem : files) {
				if(fileItem.getName().contains("rpg")){
					rpgFilePaths.add(fileItem.getCanonicalPath());
				}
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rpgFilePaths;	
	}

	public ArrayList<String> getConnectivityFiles(){		
		ArrayList<String> conFilePaths = new ArrayList<String>();
		try {
			File dir = new File(outputPath);
			List<File> files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
			for (File fileItem : files) {
				if(fileItem.getName().contains("con")){
					conFilePaths.add(fileItem.getCanonicalPath());
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return conFilePaths;	
	}

	public ArrayList<String> getPlanFiles(){		
		ArrayList<String> planFilePaths = new ArrayList<String>();
		try {
			File dir = new File(outputPath);
			List<File> files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
			for (File fileItem : files) {
				if(fileItem.getName().contains("plan")){
					planFilePaths.add(fileItem.getCanonicalPath());
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return planFilePaths;	
	}


	public ArrayList<PlanningGraph> readRPG(){
		ArrayList<String> rpgFiles = getRPGFiles();
		ArrayList<PlanningGraph> rpgs = new ArrayList<PlanningGraph>();
		for(int i=0; i<rpgFiles.size(); i++){
			PlanningGraph planningGraph = new PlanningGraph(rpgFiles.get(i));
			planningGraph.readFFOutputL0(rpgFiles.get(i));
			rpgs.add(planningGraph);
		}
		return rpgs;
	}

	public ArrayList<ConnectivityGraph> readConnectivityGraphs(){
		ArrayList<String> conGraphFiles = getConnectivityFiles();
		ArrayList<ConnectivityGraph> connectivities = new ArrayList<ConnectivityGraph>();
		for(int i=0; i<conGraphFiles.size(); i++){
			ConnectivityGraph graph = new ConnectivityGraph(conGraphFiles.get(i));
			graph.readConGraphOutput(conGraphFiles.get(i));
			connectivities.add(graph);
		}
		return connectivities;
	}

	public ArrayList<Plan> readPlans(){
		ArrayList<String> planFiles = getPlanFiles();
		PlanExtractor px = new PlanExtractor();
		for(int i=0; i<planFiles.size(); i++){
			px.readFFPlanOutput(planFiles.get(i));
		}
		return px.getPlanSet();
	}

	public void runPlanner(){
		runFF(1, StateGenerator.domainFile, StateGenerator.problemFile); //Plan
		runFF(2, StateGenerator.domainFile, StateGenerator.problemFile); //RPG
		runFF(3, StateGenerator.domainFile, StateGenerator.problemFile); //connectivity
	}

	public ArrayList<String> updateStateForAction(String action, ArrayList<String> currentState, ConnectivityGraph conGraph){
		Set<String> set = new HashSet<String>();	//no duplicates
		ArrayList<String> add = conGraph.findStatesAddedByAction(action);
		ArrayList<String> del = conGraph.findStatesDeletedByAction(action);
		set.addAll(currentState); 		//copy currentstate to newState
		for(int i=0; i<del.size(); i++){	//remove del from newstate
			set.remove(del.get(i));
		}
		for(int i=0; i<add.size(); i++){	//add add to newState
			set.add(add.get(i));
		}
		ArrayList<String> newState = new ArrayList<String>();
		newState.addAll(set);
		return newState;
	}

	public boolean stoppable(ArrayList<String> state){
		if(domain.equals("blocks")){
			return stoppableBlocks(state);
		}else if(domain.equals("pag")){
			return stoppablePAG(state);
		}
		return false;
	}

	private boolean stoppablePAG(ArrayList<String> state){
		//stop expanding if state contains"information-leakage" for attacker or "msg-sent safe-email" for user
		for(int i=0; i<state.size(); i++){
			System.out.println(">>> "+state.get(i));
			if(state.get(i).contains("information-leakage") || (state.get(i).contains("MSG-SENT SAFE-EMAIL"))){
				return true;
			}
		}
		return false;
	}

	private boolean stoppableBlocks(ArrayList<String> state){
		//stop expanding if only one block is onTable, one block is clear and hand is empty. can make this more descriptive later.
		int onTableCount = 0;
		int handEmptyCount = 0;
		int clearCount = 0;
		for(int i=0; i<state.size(); i++){
			if(state.get(i).contains("HANDEMPTY")){
				handEmptyCount++;
			}
			if(state.get(i).contains("CLEAR")){
				handEmptyCount++;
			}
			if(state.get(i).contains("ONTABLE")){
				onTableCount++;
			}
		}
		if(onTableCount==1 && handEmptyCount==1 && clearCount==1){
			return true;
		}
		return false;
	}

	public StateGraph enumerateStates(State in, ArrayList<State> seen){ //draw a state transition graph starting with state 'in'
		runPlanner();
		//ArrayList<PlanningGraph> rpgs = readRPG();
		ArrayList<ConnectivityGraph> cons = readConnectivityGraphs();

		StateGraph graph = new StateGraph();
		graph.addVertex(in.getState());

		for(int i=0; i<cons.size(); i++){
			ArrayList<String> currentState = in.getState();
			recursiveAddEdge(currentState, cons.get(i), graph, seen);
		}
		System.out.println("---------------enumerateStates()--------------------");
		System.out.println(graph.toString());
		System.out.println(graph.printEdges());
		return graph;
	}

	public ArrayList<State> getStatesAfterObservations(Observation ob, State in){
		ArrayList<State> stateseq = new ArrayList<State>();
		ArrayList<String> obs = ob.getObservations();
		ArrayList<ConnectivityGraph> cons = readConnectivityGraphs();
		stateseq.add(in);				//add init
		int index = 1;
		for (String o : obs) { 			//update init based on o
			State current = new State();
			ArrayList<String> temp = new ArrayList<String>();
			temp.addAll(stateseq.get(index-1).getState());
			current.setState(temp);
			for(int i=0; i<cons.size(); i++){ //this size=1 because there is 1 problem
				ArrayList<String> added = cons.get(i).findStatesAddedByAction(o);
				ArrayList<String> dels = cons.get(i).findStatesDeletedByAction(o);
				current.statePredicates.removeAll(dels);
				current.statePredicates.addAll(added);
			}
			index++;
			stateseq.add(current);
		}
		return stateseq; //has all states for which you need to generate state graphs for
	}

	public ArrayList<String> addGraphEdgeForAction(String action, ArrayList<String> currentState, ConnectivityGraph con, StateGraph graph){
		ArrayList<String> newState = updateStateForAction(action, currentState, con);
		if(!newState.isEmpty()){
			graph.addEdge(currentState, newState, action);
		}
		return newState;
	}

	public ArrayList<String> addGraphEdgeForActionWithoutUndo(String action, ArrayList<String> currentState, ArrayList<String> prevState,
			ConnectivityGraph con, StateGraph graph){
		ArrayList<String> newState = updateStateForAction(action, currentState, con);
		if(!newState.isEmpty() && !isNowEqualsPrevious(prevState, newState)){
			graph.addEdge(currentState, newState, action);
			return newState;
		}
		return null;
	}

	public ArrayList<String> cleanActions(ArrayList<String> actions, ArrayList<String> currentState, StateGraph graph, 
			ArrayList<State> seen, ConnectivityGraph con){
		ArrayList<String> bi  = new ArrayList<String>();
		ArrayList<String> cleaned  = new ArrayList<String>();
		for(int i=0; i<actions.size(); i++){ //remove bidirectional connections (1)
			if(!graph.isEdgeBidirectional(actions.get(i), currentState)){
				bi.add(actions.get(i));
			}
		}
		for(int i=0; i<bi.size(); i++){ //remove any actions that changes the state to anything in seen. (2)
			String cur = bi.get(i);
			int set = 0;
			ArrayList<String> adds = con.findStatesAddedByAction(cur);
			ArrayList<String> dels = con.findStatesDeletedByAction(cur);
			ArrayList<String> copyCurrent = new ArrayList<String>();
			copyCurrent.addAll(currentState);
			copyCurrent.removeAll(dels);
			copyCurrent.addAll(adds);
			State updated = new State();
			updated.setState(copyCurrent);
			for(int j=0; j<seen.size(); j++){
				if(updated.equals(seen.get(j))){ //effect of current action in cleaned is equal to an already state seen. remove from cleaned
					set++;
				}
			}
			if(set==0){
				cleaned.add(cur);
			}
		}
		return cleaned;
	}

	public ArrayList<String> cleanActionsSequential(ArrayList<String> actions, ArrayList<String> currentState, StateGraph graph){
		ArrayList<String> cleaned  = new ArrayList<String>();
		for(int i=0; i<actions.size(); i++){
			if(!graph.isActionInEdgeSet(actions.get(i))){//remove from actions if action is already in edge set.
				cleaned.add(actions.get(i));
			}			
		}
		return cleaned;
	}

	public void recursiveAddEdge(ArrayList<String> currentState, ConnectivityGraph con, StateGraph graph, ArrayList<State> seen){
		if(stoppable(currentState)){
			return;
		}else{
			ArrayList<String> actions = con.findApplicableActionsInState(currentState);
			ArrayList<String> cleaned = null;
			if(domain.equals("blocks"))//reversible domains
				cleaned = cleanActions(actions, currentState, graph, seen, con); //actions should be cleaned by removing bidirectional connections that are already visited. and any other actions leading to already visited states so far.
			else if(domain.equals("pag"))//sequential domains
				cleaned = cleanActionsSequential(actions, currentState, graph);
			System.out.println("ac->"+actions);System.out.println("cl->"+cleaned);
			for (String action : cleaned) {
				System.out.println(action);
				ArrayList<String> newState = addGraphEdgeForAction(action, currentState, con, graph);
				recursiveAddEdge(newState, con, graph, seen);
			}
		}
	}

	public void graphToDOT(StateGraph g, int namesuffix){
		DesirableState state = new DesirableState();
		state.readStatesFromFile(desirableStateFile);
		CriticalState cs = new CriticalState(criticalStateFile);
		cs.readCriticalState();
		GraphDOT dot = new GraphDOT(g, state, cs);
		dot.generateDOT(dotFilePrefix+namesuffix+dotFileExt);
	}

	public void graphToDOTNoUndo(StateGraph g){//not used
		DesirableState state = new DesirableState();
		state.readStatesFromFile(desirableStateFile);
		CriticalState cs = new CriticalState(criticalStateFile);
		cs.readCriticalState();
		GraphDOT dot = new GraphDOT(g, state, cs);
		dot.generateDOTNoUndo(dotFilePrefix);
	}

	private boolean isNowEqualsPrevious(ArrayList<String> now, ArrayList<String> prev){
		Collections.sort(now);
		Collections.sort(prev);
		return now.equals(prev);
	}

	public void applyUniformProbabilitiesToStates(StateGraph g, State in){	//g = graph converted to tree
		StateVertex initVertex = g.findVertex(in.getState());
		ArrayList<StateVertex> visitOrder = g.doBFSForStateTree(initVertex);
		for (StateVertex current : visitOrder) {
			if(current.isEqual(initVertex)){
				current.setStateProbability(initProbability);
			}
			TreeSet<StateVertex> neighbors = g.getAdjacencyList().get(current);
			int neighborCount = neighbors.size();
			for (StateVertex neighbor : neighbors) {
				ActionEdge e = g.getEdgeBetweenVertices(current, neighbor);
				e.setActionProbability(1.0/neighborCount);//assumes equal probability to go to any one of neighbor states
				neighbor.setStateProbability(neighbor.getStateProbability()+current.getStateProbability()*e.getActionProbability());
			}
		}
	}

	public StateVertex getInitVertex(StateGraph g, State in){
		return g.findVertex(in.getState());
	}

	//DONT KNOW HOW TO DO THIS YET.
	public void applyBiasedProbabilitiesToStates(StateGraph g){//g is graph converted to tree
		//		ArrayList<Plan> plans = readPlans(); //for the attacker's domain I use this to assign varying probabilities to actions in state graph.
		//		ArrayList<ConnectivityGraph> cons = readConnectivityGraphs();
		//		InitialState i = setInitialState();	
		//		StateVertex initVertex = g.findVertex(i.getInit());
		//		ArrayList<StateVertex> visitOrder = g.doDFSForStateTree(initVertex);
		//		for (StateVertex current : visitOrder) {
		//			if(current.isEqual(initVertex)){
		//				current.setStateProbability(initProbability);
		//			}
		//			TreeSet<StateVertex> neighbors = g.getAdjacencyList().get(current);
		//			int neighborCount = neighbors.size();
		//			for (StateVertex neighbor : neighbors) {
		//				ActionEdge e = g.getEdgeBetweenVertices(current, neighbor);
		//				if(e.isEdgeInPlan(plans.get(0), cons.get(0), i)){ //For now this arraylist has only 1 element
		//					System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"+e);
		//					e.setActionProbability(attackerActionProbability);
		//				}else{
		//					double unsetneighborCount=0;
		//					for (StateVertex stateVertex : neighbors) {
		//						if(stateVertex.getStateProbability()==0.0){
		//							unsetneighborCount++;
		//						}
		//					}
		//					e.setActionProbability(1.0/unsetneighborCount);//assumes equal probability to go to any one of neighbor states
		//				}
		//				e.setActionProbability(1.0/neighborCount);//assumes equal probability to go to any one of neighbor states
		//				neighbor.setStateProbability(neighbor.getStateProbability()+current.getStateProbability()*e.getActionProbability());
		//			}
		//		}
	}

}