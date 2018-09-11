package run;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

//reads a critical state from file. Critical State = attacker's goal.
public class CriticalState {
	private ArrayList<String> critical;
	private String criticalStateFile;
	
	public CriticalState(String filename){
		critical = new ArrayList<>();
		criticalStateFile = filename;
	}
	
	public void readCriticalState(){
		Scanner reader;
		try {
			reader = new Scanner(new File(criticalStateFile));
			while(reader.hasNextLine()){
				critical.add(reader.nextLine().trim());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}

	public ArrayList<String> getCriticalState() {
		return critical;
	}

	public void setCritical(ArrayList<String> critical) {
		this.critical = critical;
	}
	
	public String toString(){
		return Arrays.toString(critical.toArray());
	}

}
