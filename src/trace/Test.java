package trace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class Test {
	public static void main(String[] args) {
		ArrayList<ArrayList<String>> fin = new ArrayList<>();
		ArrayList<String> s1 = new ArrayList<>();
		ArrayList<String> s2 = new ArrayList<>();
		ArrayList<String> s3 = new ArrayList<>();
		s1.add("N:PICK-UP T");
		s1.add("N:STACK T I");
		s1.add("N:PICK-UP P");
		s1.add("N:STACK P O");
		
		s2.add("Y:PICK-UP X");
		s2.add("Y:STACK T X");
		s2.add("N:PICK-UP Y");
		s2.add("N:STACK P O");
		
		s3.add("N:PICK-UP R");
		s3.add("N:STACK M I");
		s3.add("Y:PICK-UP M");
		s3.add("N:STACK P Y");
		fin.add(s3);
		fin.add(s2);
		fin.add(s1);

		ArrayList<ArrayList<String>> fin2 = new ArrayList<>();

		for (int i = 0; i < fin.size(); i++) {
			ArrayList<String> tr = fin.get(i);
			for (String s : tr) {
				if(s.contains("Y:")){
					fin2.add(tr);
					break;
				}
			}
		}
		for (ArrayList<String> arrayList : fin2) {
			System.out.println(Arrays.toString(arrayList.toArray()));
		}
		double num = ThreadLocalRandom.current().nextDouble(0, 1);
		System.out.println(num);
	}

}
