package run;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class State {
	private ArrayList<String> undesirable;
	private ArrayList<String> desirable;
	
	public State(){
		undesirable = new ArrayList<String>();
		desirable = new ArrayList<String>();
	}
	
	public void readStatesFromFile(String filename){
		Scanner reader = null ;
		try {
			reader = new Scanner(new File(filename));
			while(reader.hasNextLine()){
				String s = reader.nextLine();
				if(s.split(":")[0].equalsIgnoreCase("desirable")){
					String line = s.split(":")[1].trim();
					for(int i=0; i<line.split(",").length; i++){
						this.desirable.add(line.split(",")[i]);
					}
				}else if(s.split(":")[0].equalsIgnoreCase("undesirable")){
					String line = s.split(":")[1].trim();
					for(int i=0; i<line.split(",").length; i++){
						this.undesirable.add(line.split(",")[i]);
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}finally{
			reader.close();
		}
	}

	public ArrayList<String> getUndesirable() {
		return undesirable;
	}

	public void setUndesirable(ArrayList<String> undesirable) {
		this.undesirable = undesirable;
	}

	public ArrayList<String> getDesirable() {
		return desirable;
	}

	public void setDesirable(ArrayList<String> desirable) {
		this.desirable = desirable;
	}
}
