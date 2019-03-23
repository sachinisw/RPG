package rg;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Hypotheses {
	public ArrayList<String> hyps;
	
	public Hypotheses() {
		hyps = new ArrayList<>();
	}
	
	public void readHyps(String infile) {
		Scanner sc;
		try {
			sc = new Scanner(new File(infile));
			while(sc.hasNextLine()) {
				hyps.add(sc.nextLine().trim());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public String toString() {
		return Arrays.toString(hyps.toArray());
	}
}
