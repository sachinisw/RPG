package run;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Observation {
	private ArrayList<String> observations;//for training data. contains true class labels

	public Observation(){
		observations = new ArrayList<String>();
	}
	
	public void readObservationFile(String filename){
		Scanner reader = null ;
		try {
			reader = new Scanner(new File(filename));
			while(reader.hasNextLine()){
				observations.add(reader.nextLine());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}finally{
			reader.close();
		}
	}
	
	public ArrayList<String> getObservations() {
		return observations;
	}

	public void setObservations(ArrayList<String> observations) {
		this.observations = observations;
	}
}
