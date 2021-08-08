package rg;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Hypotheses {
	private ArrayList<String> hyps;

	public Hypotheses() {
		hyps = new ArrayList<>();
	}

	public void readHyps(String infile) {
		Scanner sc;
		String critical = "";
		try {
			sc = new Scanner(new File(infile));
			while(sc.hasNextLine()) {
				String line = sc.nextLine().trim();
				if(line.contains("desirable:")) {
					hyps.add(line.replace(",",""));
				}else {
					critical+=line;
				}
			}
			if(!critical.isEmpty()) {
				hyps.add(critical.replace(",",""));
			}
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<String> getHyps() {
		return hyps;
	}

	public void setHyps(ArrayList<String> hyps) {
		this.hyps = hyps;
	}

	public String toString() {
		return Arrays.toString(hyps.toArray());
	}
}
