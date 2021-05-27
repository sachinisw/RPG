package run.stat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import run.TestConfigsML;

//for each test scenario (instances 1, 2, 3) compute probability of an attack.
//from these attacks, compute TPR, TNR
public class Probability {
	
	public static DTreeInputCount readDecisionTreeInputFile(String filename){//for this observation file, return the number of observations + the Y values
		Scanner reader = null ;
		int countY = 0, countTotal = 0;
		try {
			reader = new Scanner(new File(filename));
			reader.nextLine();//read off the header
			while(reader.hasNextLine()){
				String lineparts[] = reader.nextLine().split(",");
				if(lineparts[lineparts.length-1].equalsIgnoreCase("Y")) {
					countY++;
				}
				countTotal++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}finally{
			reader.close();
		}
		return new DTreeInputCount(countY, countTotal);
	}
	
	public static TreeSet<String> getDataFiles(String obsfiles){
		TreeSet<String> obFiles = new TreeSet<String>();
		try {
			File dir = new File(obsfiles);
			List<File> files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
			for (File fileItem : files) {
				if(!fileItem.getCanonicalPath().contains("lm") && !fileItem.getCanonicalPath().contains("full")) {
					obFiles.add(fileItem.getCanonicalPath());
				}
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
		return obFiles;	
	}
	
	public static void computeProbabilities(int start) {
		for (int instance=start; instance<=TestConfigsML.instances; instance++) { //blocks-3, navigator-3 easyipc-3, ferry-3 instances
			ArrayList<ArrayList<DTreeInputCount>> allobsininstance = new ArrayList<>();
			ArrayList<Double> probabilities = new ArrayList<Double>();
			for (int x=0; x<TestConfigsML.instanceCases; x++) { //blocks,navigator,easyipc, ferry -each instance has 20 problems
				String ds_csv = TestConfigsML.prefix+TestConfigsML.instancedir+String.valueOf(instance)+TestConfigsML.instscenario+String.valueOf(x)+TestConfigsML.decisiontreeinput;
				ArrayList<DTreeInputCount> currentCase = new ArrayList<>();
				TreeSet<String> datafiles = getDataFiles(ds_csv);
				for (String string : datafiles) {
					DTreeInputCount data = readDecisionTreeInputFile(string);
					currentCase.add(data);
				}
				allobsininstance.add(currentCase);
				probabilities.add(computeCurrentCaseAttackProbability(currentCase));
			}//for the 20 cases in the current instance, Y and Total counts
			System.out.println(probabilities);
		}
	}
	
	public static double computeCurrentCaseAttackProbability(ArrayList<DTreeInputCount> currentCase) {
		double totalattacks = 0.0, totalWaysToReachGoal = currentCase.size();
		for (DTreeInputCount input : currentCase) {
			if(input.countY>0) {
				totalattacks++;
			}
		}
		return totalattacks/totalWaysToReachGoal;
	}
	
	public static void main(String[] args) {
		computeProbabilities(1);
	}
}
