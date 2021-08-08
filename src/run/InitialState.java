package run;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class InitialState extends State{
	
	public InitialState(){
		super();
	}

	public void readInitsFromFile(String filename){
		Scanner reader = null ;
		try {
			reader = new Scanner(new File(filename));
			while(reader.hasNextLine()){
				statePredicates.add(reader.nextLine().trim());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}finally{
			reader.close();
		}
	}
}
