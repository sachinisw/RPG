package run;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

//reads a user's desirable state from file. Desirable State = user's goal.
public class DesirableState {
	private String desirableStateFile;
	private ArrayList<String> desirable;
	
	public DesirableState(String filename){
		desirableStateFile = filename;
		desirable = new ArrayList<String>();
	}
	
	public void readStatesFromFile(){
		Scanner reader = null ;
		try {
			reader = new Scanner(new File(this.desirableStateFile)); 
			while(reader.hasNextLine()){
				String s = reader.nextLine();
				if(s.split(":")[0].equalsIgnoreCase("desirable")){
					String line = s.split(":")[1].trim();
					for(int i=0; i<line.split(",").length; i++){
						this.desirable.add(line.split(",")[i]);
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}finally{
			reader.close();
		}
	}

	public ArrayList<String> getDesirable() {
		return desirable;
	}

	public void setDesirable(ArrayList<String> desirable) {
		this.desirable = desirable;
	}
}
