package rg;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Observations implements Cloneable {
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
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		Observations clone = null;
		try{
			clone = (Observations) super.clone();//do a deep copy here. because array list is a shallow copy by default
			ArrayList<String> obsCopy = new ArrayList<>();
			for (String string : obs) {
				obsCopy.add(string);
			}
			clone.setObs(obsCopy);
		}catch (CloneNotSupportedException e){
			throw new RuntimeException(e);
		}
		return clone;
	} 

	public void removeLabels() {
		for (String s : obs) {
			String snew = s.substring(s.indexOf(":")+1);
			obs.set(obs.indexOf(s), snew);
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
