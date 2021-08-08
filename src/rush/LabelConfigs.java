package rush;

public class LabelConfigs {
	public static final int trainedscenario = 0;
	public static final int instances = 3; //from start arg. run test for this many instances
	public static final int instanceCases = 20;
	public static final String domain = "RUSHHOUR"; //EASYIPC //BLOCKS //NAVIGATOR //FERRY //RUSHHOUR
	public static final String prefix = "/home/sachini/domains/"+domain+"/scenarios/"+trainedscenario+"/train/cases/";
	public static final String instancedir="inst";
	public static final String instscenario="/scenarios/";
	public static final String observationFiles = "/obplan/";
	public static final String criticalStateFile = "/critical.txt";
}
