package run;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class InitialState {
	private ArrayList<String> init;
	
	public InitialState(){
		init = new ArrayList<String>();
	}

	public void readInitsFromFile(String filename){
		Scanner reader = null ;
		try {
			reader = new Scanner(new File(filename));
			while(reader.hasNextLine()){
				init.add(reader.nextLine());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}finally{
			reader.close();
		}
	}
	
	public ArrayList<String> getInit() {
		return init;
	}

	public void setInit(ArrayList<String> init) {
		this.init = init;
	}
	
	public String toString(){
		return Arrays.toString(init.toArray());
	}
}
