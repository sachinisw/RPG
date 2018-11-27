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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.logging.Logger;

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
	public static final Logger LOGGER = Logger.getLogger(StateGenerator.class.getName());
	public final static String ffPath = "/home/sachini/domains/Metric-FF-new/ff";
	public final static String dotFileExt = ".dot";
	public final static double initProbability = 1.0;
	public Agent agent;
	public String domain;

	public StateGenerator(Agent a, String dom){
		domain = dom;
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

	public boolean stoppable(ArrayList<String> state, int x, int y, HashMap<String, String> desirableloc){//how to identify leaf nodes
		if(domain.equalsIgnoreCase("blocks")){//x y not applicable. put -1, -1 where called
			return stoppableBlocks(state);
		}else if(domain.equalsIgnoreCase("easyipc")){
			return stoppableGrid(state, x, y); //desirable state position, always top-right
		}else if(domain.equalsIgnoreCase("ferry")){
			return stoppableFerry(state, desirableloc);
		}else if(domain.equalsIgnoreCase("navigator")){  //desirable state position, always top-right
			return stoppableNavigator(state,x,y);
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

	//generic stopping condition for grid domains: robot wants to go to top-right corner. enumerate all possible paths to get to top-right
	//1-critical spot in the grid, which will be on some possible paths
	private boolean stoppableGrid(ArrayList<String> state, int x, int y){ //NOTE: change grid_size value for larger problem instances
		String curpos = "";
		for (String string : state) {
			if(string.contains("AT-ROBOT")){
				String temp = string.substring(1, string.length()-1);
				curpos = temp.substring(temp.indexOf("PLACE_"));
				break;
			}
		}
		int yr = Integer.parseInt(curpos.substring(curpos.length()-1,curpos.length()));
		int xr = Integer.parseInt(curpos.substring(curpos.length()-3,curpos.length()-2));
		if((yr==y && xr==x)){ //if top-right corner stop
			return true;
		}
		return false;
	}

	private boolean stoppableNavigator(ArrayList<String> state, int x, int y){ //NOTE: change grid_size value for larger problem instances
		String curpos = "";
		for (String string : state) {
			if(string.contains("AT")){
				String temp = string.substring(1, string.length()-1);
				curpos = temp.substring(temp.indexOf("PLACE_"));
				break;
			}
		}
		int[] ar = getCoordinatesFromPlaceString(curpos);
		if((ar[1]==y && ar[0]==x)){ //if top-right corner stop
			return true;
		}
		return false;
	}

	private int[] getCoordinatesFromPlaceString(String s) { //needs a string like PLACE_5_10, place_4_8
		int z=0;
		int [] xy = new int [2];
		for (int i = 0; i < s.length(); i++) {
			if(s.charAt(i)=='_') {
				xy[z++]=i;
			}
		}
		int xx = Integer.parseInt(s.substring(xy[0]+1,xy[1]));
		int yy = Integer.parseInt(s.substring(xy[1]+1,s.length()));
		return new int[] {xx,yy};
	}

	//general rule for identifying leaf nodes. 
	//if all (or specific) cars are in desirable places. not on ship and ship at desirable place
	public boolean stoppableFerry(ArrayList<String> state, HashMap<String, String> userwantloc){
		//find (at C* L*) predicates. compare if for all C, L value has changed to desirable locations
		HashMap<String,String> carpos = new HashMap<String, String>(); 
		for (String st : state) {//two ways of finding current object's location, object is directly at loc
			if(st.contains("AT") && !st.contains("AT-FERRY")) {
				String parts [] = st.substring(1,st.length()-1).split(" ");
				String c = parts[1]; String l=parts[2];
				carpos.put(c,l);

			}
		}
		Iterator<Map.Entry<String, String>> itrcarpos = carpos.entrySet().iterator();
		while(itrcarpos.hasNext()){
			Entry<String, String> e = itrcarpos.next();
			String carposloc = e.getValue();
			String userloc = userwantloc.get(e.getKey());
			if(!carposloc.equalsIgnoreCase(userloc)) {
				return false;
			}
		}
		return true;
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
			if(domain.equalsIgnoreCase("blocks")) {
				recursiveAddEdge(currentState, cons.get(i), graph, seen, -1, -1, null);
			}else if(domain.equalsIgnoreCase("easyipc")) {
				int [] xy = getGridEdge(ds.getDesirable());
				recursiveAddEdge(currentState, cons.get(i), graph, seen, xy[0], xy[1], null);
			}else if(domain.equalsIgnoreCase("navigator")) {
				int [] xy = getNavigatorEdge(ds.getDesirable());
				recursiveAddEdge(currentState, cons.get(i), graph, seen, xy[0], xy[1], null);
			}else if(domain.equalsIgnoreCase("ferry")) {
				HashMap<String, String> des = getDesirableLocationsFerry(ds.getDesirable());
				recursiveAddEdge(currentState, cons.get(i), graph, seen, -1, -1, des);
			}
		}
		graph.markVerticesContainingCriticalState(cs, domain);
		graph.markVerticesContainingDesirableState(ds, domain);
		return graph; //this graph is bidirectional
	}

	private HashMap<String, String> getDesirableLocationsFerry(ArrayList<String> des) {
		HashMap<String, String> desloc = new HashMap<>();
		for (String st : des) {
			if(st.contains("AT") && !st.contains("AT-FERRY")) {
				String parts [] = st.substring(1,st.length()-1).split(" ");
				String c = parts[1]; String l=parts[2];
				desloc.put(c,l);
			}
		}
		return desloc;
	}

	private int [] getGridEdge(ArrayList<String> state) {
		String pos = "";
		for (String s : state) {
			if(s.contains("AT-ROBOT")) {
				pos = s;
				break;
			}
		}
		String cord = pos.substring(pos.indexOf("PLACE_"));
		int ya = Integer.parseInt(cord.substring(cord.length()-2,cord.length()-1));
		int xa = Integer.parseInt(cord.substring(cord.length()-4,cord.length()-3));
		return new int[] {xa, ya};
	}

	private int [] getNavigatorEdge(ArrayList<String> state) {
		String pos = "";
		for (String s : state) {
			if(s.contains("AT")) {
				pos = s;
				break;
			}
		}
		int xy [] = getCoordinatesFromPlaceString(pos.substring(1, pos.length()-1));
		return new int[] {xy[0], xy[1]};
	}

	public ArrayList<State> getStatesAfterObservations(Observation ob, State in, boolean tracegenerator){//README:: true if running trace generator, false otherwise
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
		for(int i=0; i<bi.size(); i++){ //remove any actions that changes the state to anything in seen (only contains initial state). (2)
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

	public void recursiveAddEdge(ArrayList<String> currentState, ConnectivityGraph con, StateGraph graph, ArrayList<State> seen, 
			int x, int y, HashMap<String, String> deslocs){
		if(stoppable(currentState, x, y, deslocs)){
			return;
		}else{
			ArrayList<String> actions = con.findApplicableActionsInState(currentState);
			ArrayList<String> cleaned = null;
			if(domain.equalsIgnoreCase("blocks") || domain.equalsIgnoreCase("easyipc") || domain.equalsIgnoreCase("navigator") 
					|| domain.equalsIgnoreCase("ferry") )//reversible domains. i.e. you can go back to previous state
				//README::: Treat each path from root as an independent path. When cleaning you only need to clean up actions that will take you back up the tree toward root. don't have to consider if state on path A is also on path B
				cleaned = cleanActions(actions, currentState, graph, seen, con); //actions should be cleaned by removing connections to states that are already seen on the current path. 
			else if(domain.equalsIgnoreCase("pag") )//sequential domains
				cleaned = cleanActionsSequential(actions, currentState, graph);
			for (String action : cleaned) {
				////README:::: seen [] only has the initial state. if you add newstate, other branches in the graph lose possible actions. The branches in the graph must be independent. children's possible actions must only depend on their immediate parents' state and not on other paths in the tree
				ArrayList<String> newState = addGraphEdgeForAction(action, currentState, con, graph); 
				recursiveAddEdge(newState, con, graph, seen, x, y, deslocs);
			}
		}
	}

	public void graphToDOT(StateGraph g, int namesuffix, int foldersuffix, boolean writeDOTFile){
		if(writeDOTFile){//name format = /home/sachini/BLOCKS/scenarios/2/dot/graph_ag_noreverse_1_4.dot
			GraphDOT dot = new GraphDOT(g, agent.domain); 
			dot.generateDOT(agent.dotFilePrefix+agent.dotFileSuffix+foldersuffix+"_"+namesuffix+dotFileExt);
		}
	}

	public void graphToDOTNoUndo(StateGraph g){//not used
		DesirableState state = new DesirableState(agent.desirableStateFile);
		state.readStatesFromFile();
		CriticalState cs = new CriticalState(agent.criticalStateFile);
		cs.readCriticalState();
		GraphDOT dot = new GraphDOT(g, agent.domain);
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
