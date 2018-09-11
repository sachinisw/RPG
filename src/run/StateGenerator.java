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

import actors.Agent;
import con.ConnectivityGraph;
import graph.ActionEdge;
import graph.GraphDOT;
import graph.StateGraph;
import graph.StateVertex;
import landmark.RelaxedPlanningGraph;
import landmark.RelaxedPlanningGraphGenerator;
import plan.Plan;
import plan.PlanExtractor;
import rpg.PlanningGraph;

public class StateGenerator {
	public final static String ffPath = "/home/sachini/BLOCKS/Metric-FF-new/ff";
	public final static String domain = "grid";//changes stoppable condition.
	public final static int grid_size = 2; //max coordinate in grid. only applicable for grid domains. TODO: find way to automate the extraction of this value
	public final static String dotFileExt = ".dot";
	public final static double initProbability = 1.0;
	public Agent agent;
	
	public StateGenerator(Agent a){
		agent = a;
	}
	
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
			writeConsoleOutputtoFile(agent.outputPath +"plan-"+probfilename.substring(probfilename.length()-14,probfilename.length()-5), plan);
			break;
		case 2:	//create relaxed planning graph
			String command_rpg = ffPath +" -o "+ domainpath+ " -f "+ probfilename+" -i 126";
			String rpg = executeShellCommand(command_rpg);
			writeConsoleOutputtoFile(agent.outputPath +"rpg-"+probfilename.substring(probfilename.length()-14,probfilename.length()-5), rpg);
			break;
		case 3:	//create connectivity graph
			String command_con = ffPath +" -o "+ domainpath+ " -f "+ probfilename+" -i 125";
			String con = executeShellCommand(command_con);
			writeConsoleOutputtoFile(agent.outputPath+"connectivity-"+probfilename.substring(probfilename.length()-14,probfilename.length()-5), con);
			break;
		default:
			System.out.println("UNSUPPORTED COMMAND");
			break;
		}
	}

	public ArrayList<String> getRPGFiles(){		
		ArrayList<String> rpgFilePaths = new ArrayList<String>();
		try {
			File dir = new File(agent.outputPath);
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
			File dir = new File(agent.outputPath);
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
			File dir = new File(agent.outputPath);
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

	public ArrayList<RelaxedPlanningGraph> readRelaxedPlanningGraph(){ //used exclusively for landmarks
		ArrayList<String> rpgFiles = getRPGFiles();
		ArrayList<RelaxedPlanningGraph> relaxedpgs = new ArrayList<RelaxedPlanningGraph>();
		for(int i=0; i<rpgFiles.size(); i++){
			RelaxedPlanningGraphGenerator rpgGen = new RelaxedPlanningGraphGenerator();
			rpgGen.readFFOutput(rpgFiles.get(i));
			relaxedpgs.add(rpgGen.rpg);
		}
		return relaxedpgs;
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
		runFF(1, agent.domainFile, agent.problemFile); //Plan
		runFF(2, agent.domainFile, agent.problemFile); //RPG
		runFF(3, agent.domainFile, agent.problemFile); //connectivity
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
		}else if(domain.equals("grid")){
			return stoppableGrid(state);
		}else if(domain.equals("pag")){
			return stoppablePAG(state);
		}
		return false;
	}

	private boolean stoppablePAG(ArrayList<String> state){
		//stop expanding if state contains"information-leakage" for attacker or "msg-sent safe-email" for user
		for(int i=0; i<state.size(); i++){
//			System.out.println(">>> "+state.get(i));
			if(state.get(i).contains("information-leakage") || (state.get(i).contains("MSG-SENT SAFE-EMAIL"))){
				return true;
			}
		}
		return false;
	}

	private boolean stoppableBlocks(ArrayList<String> state){
		//stop expanding if only one block is onTable, one block is clear and hand is empty. i.e. all blocks are vertically stacked. can make this more descriptive later.
		int onTableCount = 0;
		int handEmptyCount = 0;
		int clearCount = 0;
		for(int i=0; i<state.size(); i++){
			if(state.get(i).contains("HANDEMPTY")){
				handEmptyCount++;
			}
			if(state.get(i).contains("CLEAR")){
				clearCount++;
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

	private boolean stoppableGrid(ArrayList<String> state){ //NOTE: change grid_size value for larger problem instances
		//stop expanding if state<> indicate that robot is at the top-right edge of the grid (i.e. largest x,y). assumes robot always start at 0,0
		String curpos = "";
		for (String string : state) {
			if(string.contains("AT-ROBOT")){
				curpos = string.substring(string.indexOf("PLACE_"));
				//System.out.println(string);
				break;
			}
		}
		int x = Integer.parseInt(curpos.substring(curpos.indexOf("_")+1,curpos.indexOf("_")+2));
		int y = Integer.parseInt(curpos.substring(curpos.length()-2,curpos.length()-1));
		//System.out.println("x="+x+" y="+y);
		if((x==grid_size) && (y==grid_size)){ //if at right edge or at top edge
			//System.out.println("!!!!!!!!!!met");
			return true;
		}
		//System.out.println("==========not met");
		return false;
	}
	
	public StateGraph enumerateStates(State in, ArrayList<State> seen){ //draw a state transition graph starting with state 'in'
		runPlanner();
		ArrayList<ConnectivityGraph> cons = readConnectivityGraphs();		
		DesirableState ds = new DesirableState(agent.desirableStateFile);
		ds.readStatesFromFile();
		CriticalState cs = new CriticalState(agent.criticalStateFile);
		cs.readCriticalState();
		StateGraph graph = new StateGraph(cs, ds);
		graph.addVertex(in.getState());

		for(int i=0; i<cons.size(); i++){
			ArrayList<String> currentState = in.getState();
			recursiveAddEdge(currentState, cons.get(i), graph, seen);
		}
		graph.markVerticesContainingCriticalState(cs);
		graph.markVerticesContainingDesirableState(ds);
//		System.out.println("---------------enumerateStates()--------------------");
//		System.out.println(graph.toString());
//		System.out.println(graph.printEdges());
		return graph; //this graph is bidirectional
	}

	public ArrayList<State> getStatesAfterObservations(Observation ob, State in, boolean tracegenerator){//true if running trace generator, false otherwise
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
				if(!tracegenerator){
					ArrayList<String> added = cons.get(i).findStatesAddedByAction(o.split(":")[1]);
					ArrayList<String> dels = cons.get(i).findStatesDeletedByAction(o.split(":")[1]);
					current.statePredicates.removeAll(dels);
					current.statePredicates.addAll(added);
				}else{
					ArrayList<String> added = cons.get(i).findStatesAddedByAction(o);
					ArrayList<String> dels = cons.get(i).findStatesDeletedByAction(o);
					current.statePredicates.removeAll(dels);
					current.statePredicates.addAll(added);
				}
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
		for(int i=0; i<actions.size(); i++){ //remove if the action undoes the immediately previous state (reverting). if not for this, infinite loop (1)
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
			System.out.println("STOPPeING AT---------------+"+currentState);
			return;
		}else{
			ArrayList<String> actions = con.findApplicableActionsInState(currentState);
			ArrayList<String> cleaned = null;
			if(domain.equals("blocks") || domain.equals("grid") )//reversible domains. i.e. you can go back to previous state
				//Treat each path as from root as an independent path. so when cleaning you only need to clean up actions that will take you back up the tree toward root. don't have to consider if state on path A is also on path B
				cleaned = cleanActions(actions, currentState, graph, seen, con); //actions should be cleaned by removing bidirectional connections that are already visited. and any other actions leading to already visited states so far.
			else if(domain.equals("pag") )//sequential domains
				cleaned = cleanActionsSequential(actions, currentState, graph);
			System.out.println("ac->"+actions);System.out.println("cl->"+cleaned);//DEBUG
			
			for (String action : cleaned) {
				System.out.println("current====="+action); //DEBUG	
				ArrayList<String> newState = addGraphEdgeForAction(action, currentState, con, graph);
				recursiveAddEdge(newState, con, graph, seen);
			}
		}
	}

	public void graphToDOT(StateGraph g, int namesuffix, int foldersuffix, boolean writeDOTFile){
		if(writeDOTFile){
			GraphDOT dot = new GraphDOT(g); //name format = /home/sachini/BLOCKS/scenarios/2/dot/graph_ag_noreverse_1_4.dot
			dot.generateDOT(agent.dotFilePrefix+agent.dotFileSuffix+foldersuffix+"_"+namesuffix+dotFileExt);
		}
	}

	public void graphToDOTNoUndo(StateGraph g){//not used
		DesirableState state = new DesirableState(agent.desirableStateFile);
		state.readStatesFromFile();
		CriticalState cs = new CriticalState(agent.criticalStateFile);
		cs.readCriticalState();
		GraphDOT dot = new GraphDOT(g);
		dot.generateDOTNoUndo(agent.dotFilePrefix);
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
	}

}
