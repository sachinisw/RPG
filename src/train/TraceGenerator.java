package train;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import actors.Agent;
import actors.Decider;
import graph.ActionEdge;
import graph.StateGraph;
import graph.StateVertex;
import run.InitialState;
import run.Observation;
import run.State;
import run.StateGenerator;

/**
 * Generate observation traces for training the decision tree
 * @author sachini
 *
 */
public class TraceGenerator {
	private static final Logger LOGGER = Logger.getLogger(TraceGenerator.class.getName());
	public final static int filterLimit = 80;
	public final static double selectProbability = 0.80;

	//this can be any observation sequence. For this one I only want the initial state graphs for attacker and user.
	public static Observation setObservations(String obsPath){
		Observation obs = new Observation();
		obs.readObservationFile(obsPath);
		return obs;
	}

	//For this one I only want the initial state graphs for attacker
	public static ArrayList<StateGraph> process(String domain, InitialState init, StateGenerator gen){
		ArrayList<StateGraph> graphs = new ArrayList<>();
		ArrayList<State> statesSeen = new ArrayList<State>();
		statesSeen.add(init);
		StateGraph graphAgent = gen.enumerateStates(init, statesSeen);
		StateGraph treeAgent = graphAgent.convertToTree(gen.getInitVertex(graphAgent, init), domain);
		gen.applyUniformProbabilitiesToStates(treeAgent, init);
		graphs.add(treeAgent);
		gen.graphToDOT(treeAgent, 1, 1, true); // remove after debug
		return graphs; //No DOT files generated for traces
	}

	public static ArrayList<StateGraph> generateStateGraph(Agent agent, String domain, InitialState init){
		StateGenerator gen = new StateGenerator(agent, domain);
		ArrayList<StateGraph> graphs = process(domain, init, gen); //graph for attacker
		return graphs;
	}
	//generates the trace with flagged observations for the classifier
	public static ArrayList<ArrayList<String>> generateTrace(StateGraph decider, String domain){ 
		ArrayList<ArrayList<String>> trace = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<StateVertex>> likelypaths = decider.getLikelyPathsForUser(decider.getDesirable().getDesirableStatePredicates(), domain);
		ArrayList<ArrayList<StateVertex>> undesirable = decider.getUndesirablePaths(likelypaths, domain);
		for(int i=0; i<likelypaths.size(); i++){
			ArrayList<StateVertex> path = likelypaths.get(i);
			ArrayList<String> trc = new ArrayList<String>();
			if(domain.equalsIgnoreCase("blocks")) { //active attacker e.g. block-words
				if(path.get(path.size()-1).containsPartialStateBlockWords(decider.getDesirable().getDesirableStatePredicates())) {
					if(path.get(path.size()-1).containsPartialStateBlockWords(decider.getCritical().getCriticalStatePredicates())) { 
						//this good path will also trigger bad state.
						for(int j=0; j<path.size()-1; j++){
							ArrayList<ActionEdge> actions = decider.findEdgeForStateTransition(path.get(j), path.get(j+1));
							for (ActionEdge actionEdge : actions) {
								if(actionEdge.getTo().containsPartialStateBlockWords(decider.getCritical().getCriticalStatePredicates())) {
									if(actionEdge.getTo().isWordConsecutive(decider.getCritical().getCriticalStatePredicates())) { 	//and when critical is spelled it's adjacent
										trc.add("Y:"+actionEdge.getAction());
									}else {
										trc.add("N:"+actionEdge.getAction());
									}
								}else {
									trc.add("N:"+actionEdge.getAction());
								}
							}
						}
					}else {	//this is a safe good path. observations need not be flagged. put N
						for(int j=0; j<path.size()-1; j++){
							ArrayList<ActionEdge> actions = decider.findEdgeForStateTransition(path.get(j), path.get(j+1));
							for (ActionEdge actionEdge : actions) {
								trc.add("N:"+actionEdge.getAction());
							}
						}
					}
				}
			}else {//passive attacker
				for(int j=0; j<path.size()-1; j++){
					ArrayList<ActionEdge> actions = decider.findEdgeForStateTransition(path.get(j), path.get(j+1));
					System.out.println("cur action "+j+Arrays.toString(actions.toArray()));
					for (ActionEdge actionEdge : actions) { //TODO: README > may need to hand label observations. should be able to do it since we have just a few for blockwords
						if(edgeInUndesirablePath(actionEdge, undesirable)){  //passive attacker
							//find trouble action. causing critical state. must be flagged //Y for steps until critical state N for steps after critical state
							if(edgeTriggersCriticalState(actionEdge, decider.getCritical().getCriticalStatePredicates())){ 
								trc.add("N:"+actionEdge.getAction());
							}else{
								trc.add("Y:"+actionEdge.getAction());
							}
						}else{
							trc.add("N:"+actionEdge.getAction());
						}
					}
				}
			}
			trace.add(trc);
		}
		return trace;
	}

	public static ArrayList<ArrayList<String>> selectTracesRandomly(ArrayList<ArrayList<String>> unfiltered){
		ArrayList<ArrayList<String>> trace = new ArrayList<ArrayList<String>>();
		if(unfiltered.size() > filterLimit){
			for (int i = 0; i < unfiltered.size(); i++) { //select all Y
				ArrayList<String> tr = unfiltered.get(i);
				for (String s : tr) {
					if(s.contains("Y:")){
						trace.add(tr);
						break;
					}
				}
			}
			for (int i = 0; i < unfiltered.size() && trace.size()<=100; i++) {
				ArrayList<String> tr = unfiltered.get(i);
				if(listContainsAllNo(tr) && selectCurrentTrace(tr)){ //filter and select all N traces
					trace.add(tr);
				}
			}
		}else{
			trace.addAll(unfiltered);
		}
		return trace;
	}

	private static boolean listContainsAllNo(ArrayList<String> list){
		int count = 0;
		for (String string : list) {
			if(string.contains("N:")){
				count++;
			}
		}
		return count==list.size();
	}

	private static double randDouble(int min, int max) {
		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}

	private static boolean selectCurrentTrace(ArrayList<String> tr){
		if(randDouble(0, 1)> selectProbability){
			return true;
		}
		return false;
	}

	private static boolean edgeInUndesirablePath(ActionEdge e, ArrayList<ArrayList<StateVertex>> undesirable){
		for (ArrayList<StateVertex> undesirablePath : undesirable) {
			for (StateVertex undesirableState : undesirablePath) {
				if(e.getTo().isEqual(undesirableState)){
					return true;
				}
			}
		}
		return false;
	}

	private static boolean edgeTriggersCriticalState(ActionEdge e, ArrayList<String> criticalstate) {
		if(e.getTo().containsState(criticalstate)){//one type of critical state
			return true;
		}
		return false;
	}

	public static void writeTracesToFile(ArrayList<ArrayList<String>> traceset, String tracepath){
		PrintWriter writer = null;
		LOGGER.log(Level.INFO, "Trace file count:- "+ traceset.size());
		for (int i = 0; i < traceset.size(); i++) {
			ArrayList<String> tr = traceset.get(i);
			try{
				writer = new PrintWriter(tracepath+"/"+i, "UTF-8");
				for (String string : tr) {
					writer.write(string);
					writer.println();
				}
			}catch (FileNotFoundException | UnsupportedEncodingException  e) {
				e.printStackTrace();
			}finally{
				writer.close();
			}
		}
	}

	public static void generateSampleObservationTrace(){
		//generating trace for a sample intervention problem. For errors and bug fixing
		for(int trainInstance=0; trainInstance<=22; trainInstance++){
			if(trainInstance==0){
				String domainFile = SampleConfigs.prefix+trainInstance+SampleConfigs.domainFile;
				String desirableStateFile = SampleConfigs.prefix+trainInstance+SampleConfigs.desirableStateFile;
				String a_problemFile = SampleConfigs.prefix+trainInstance+SampleConfigs.a_problemFile;
				String a_outputPath = SampleConfigs.prefix+trainInstance+SampleConfigs.a_outputPath;
				String criticalStateFile = SampleConfigs.prefix+trainInstance+SampleConfigs.criticalStateFile;
				String a_initFile = SampleConfigs.prefix+trainInstance+SampleConfigs.a_initFile;
				String a_dotFilePrefix = SampleConfigs.prefix+trainInstance;
				String tracepath = SampleConfigs.traces+trainInstance;
				String domain = SampleConfigs.domain;
				Decider decider = new Decider(domain, domainFile, desirableStateFile, a_problemFile, a_outputPath, criticalStateFile, a_initFile, a_dotFilePrefix, SampleConfigs.a_dotFile);
				LOGGER.log(Level.INFO, "Generating State Tree for ["+ SampleConfigs.domain +"] instance --> "+trainInstance);
				ArrayList<StateGraph> attackerState = generateStateGraph(decider, domain, decider.getInitialState());//generate graph for attacker and user
				LOGGER.log(Level.INFO, "Writing traces to files");
				writeTracesToFile(generateTrace(attackerState.get(0), domain), tracepath); //i can give the same dot file path beacause I am generating the graph for initial state only
				LOGGER.log(Level.INFO, "----Sample Traces done---");
			}
		}
	}

	public static void generateTrainingObservationTrace() { //generating observations for actual set of training data.
		PlanningProblemGenerator.generateProblemsFromTemplate();
		for(int currentCase=0; currentCase<TraceConfigs.trainingcases; currentCase++) {
			String domainFile = TraceConfigs.prefix+TraceConfigs.cases+currentCase+TraceConfigs.domainFile;
			String desirableStateFile = TraceConfigs.prefix+TraceConfigs.cases+currentCase+TraceConfigs.desirableStateFile;
			String a_problemFile = TraceConfigs.prefix+TraceConfigs.cases+currentCase+TraceConfigs.a_problemFile;
			String a_outputPath = TraceConfigs.prefix+TraceConfigs.cases+currentCase+TraceConfigs.out+TraceConfigs.a_outputPath;
			String criticalStateFile = TraceConfigs.prefix+TraceConfigs.cases+currentCase+TraceConfigs.criticalStateFile;
			String a_initFile = TraceConfigs.prefix+TraceConfigs.cases+currentCase+TraceConfigs.a_initFile;
			String a_dotFilePrefix = TraceConfigs.prefix+TraceConfigs.cases+currentCase+TraceConfigs.dot;
			String tracepath = TraceConfigs.prefix+TraceConfigs.cases+currentCase+TraceConfigs.obs;
			String domain = TraceConfigs.domain;
			Decider decider = new Decider(domain, domainFile, desirableStateFile, a_problemFile, a_outputPath, criticalStateFile, a_initFile, a_dotFilePrefix, TraceConfigs.a_dotFile);
			LOGGER.log(Level.INFO, "Generating State Tree for ["+ TraceConfigs.domain +"] case --> "+ currentCase);
			ArrayList<StateGraph> attackerState = generateStateGraph(decider, domain, decider.getInitialState());//generate graph for attacker and user
			LOGGER.log(Level.INFO, "Writing traces to files");
			writeTracesToFile(generateTrace(attackerState.get(0), domain), tracepath); //i can give the same dot file path beacause I am generating the graph for initial state only
		}
		LOGGER.log(Level.INFO, "----Training Traces done---");
	}

	public static void generateTestingObservationTrace(String domain, String domainFile, String desirablestatefile, String a_problemfile, 
			String criticalstatefile, String outputpath, String init, String dotpre, String dotsuf, String obsout) { //generating observations for actual set of testing data.
		Decider decider = new Decider(domain, domainFile, desirablestatefile, a_problemfile, outputpath, criticalstatefile, init, dotpre, dotsuf);
		ArrayList<StateGraph> attackerState = generateStateGraph(decider, domain, decider.getInitialState());//generate state graph to enumerate decision space for the deciding agent
		writeTracesToFile(generateTrace(attackerState.get(0), domain), obsout); //i can give the same dot file path beacause I am generating the graph for initial state only
	}

	public static void main(String[] args) { 
		int mode=1; //TODO: README edit here first 		//0-generate obs for sample intervention scenario for debugging, 1-generate obs for 20 intervention problems for training
		if(mode==0) { //Debug  only
			generateSampleObservationTrace();
		}else {
			generateTrainingObservationTrace();
		}
	}
}
