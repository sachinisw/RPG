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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import con.ConnectivityGraph;
import graph.GraphDOT;
import graph.StateGraph;
import rpg.PlanningGraph;

public class StateGenerator {
	public final static String ffPath = "/home/sachini/BLOCKS/Metric-FF-new/ff";
	public final static String domainFile = "/home/sachini/BLOCKS/domain.pddl";
	public final static String stateFile = "/home/sachini/BLOCKS/states3.txt";
	public final static String problemFile = "/home/sachini/BLOCKS/problem_3.pddl";
	public final static String outputPath = "/home/sachini/BLOCKS/outs/"; //clean this directory before running. if not graphs will be wrong
	public final static String initFile = "/home/sachini/BLOCKS/inits3.txt";
	public final static String dotFile = "/home/sachini/BLOCKS/graph3_ad_noreverse.dot";

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
		//stop expanding if only one block is onTable and hand is empty. can make this more descriptive later.
		int onTableCount = 0;
		int handEmptyCount = 0;
		for(int i=0; i<state.size(); i++){
			if(state.get(i).contains("ONTABLE")){
				onTableCount++;
			}
		}
		for(int i=0; i<state.size(); i++){
			if(state.get(i).contains("HANDEMPTY")){
				handEmptyCount++;
			}
		}
		if(onTableCount==1 && handEmptyCount==1){
			return true;
		}
		return false;
	}

	public StateGraph enumerateStates(){
		InitialState init = new InitialState();
		init.readInitsFromFile(initFile);

		runPlanner();
		//ArrayList<PlanningGraph> rpgs = readRPG();
		ArrayList<ConnectivityGraph> cons = readConnectivityGraphs();

		StateGraph graph = new StateGraph();
		graph.addVertex(init.getInit());

		for(int i=0; i<cons.size(); i++){
			ArrayList<String> currentState = init.getInit();
			recursiveAddEdge(currentState, cons.get(i), graph);
		}
		System.out.println("-----------------------------------");
		System.out.println(graph.toString());
		System.out.println(graph.printEdges());
		return graph;
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
	
	public ArrayList<String> cleanActions(ArrayList<String> actions, ArrayList<String> currentState,StateGraph graph){
		ArrayList<String> cleaned  = new ArrayList<String>();
		for(int i=0; i<actions.size(); i++){
			if(!graph.isEdgeBidirectional(actions.get(i), currentState)){
				cleaned.add(actions.get(i));
			}
		}
		return cleaned;
	}

	public void recursiveAddEdge(ArrayList<String> currentState, ConnectivityGraph con, StateGraph graph){
		if(stoppable(currentState)){
			return;
		}else{
			ArrayList<String> actions = con.findApplicableActionsInState(currentState);
			ArrayList<String> cleaned = cleanActions(actions, currentState, graph); //actions should be cleaned by removing bidirectional connections. that are already visited
			System.out.println("ac->"+actions);
			System.out.println("cl->"+cleaned);
			for (String action : cleaned) {
				System.out.println(action);
				ArrayList<String> newState = addGraphEdgeForAction(action, currentState, con, graph);
				recursiveAddEdge(newState, con, graph);
			}
		}
	}

	public void graphToDOT(StateGraph g){
		State state = new State();
		state.readStatesFromFile(stateFile);
		GraphDOT dot = new GraphDOT(g, state);
		dot.generateDOT(dotFile);
	}

	public void graphToDOTNoUndo(StateGraph g){
		State state = new State();
		state.readStatesFromFile(stateFile);
		GraphDOT dot = new GraphDOT(g, state);
		dot.generateDOTNoUndo(dotFile);
	}
	
	private boolean isNowEqualsPrevious(ArrayList<String> now, ArrayList<String> prev){
		Collections.sort(now);
		Collections.sort(prev);
		return now.equals(prev);
	}

}
