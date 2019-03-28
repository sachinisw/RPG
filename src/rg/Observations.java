package rg;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Observations {
	private ArrayList<String> obs;

	public Observations() {
		obs = new ArrayList<>();
	}

	public void readObs(String infile) {
		Scanner sc;
		try {
			sc = new Scanner(new File(infile));
			while(sc.hasNextLine()) {
				obs.add(sc.nextLine().trim());
			}
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public String toString() {
		return Arrays.toString(obs.toArray());
	}

	public ArrayList<String> getObs() {
		return obs;
	}

	public void setObs(ArrayList<String> obs) {
		this.obs = obs;
	}
}
