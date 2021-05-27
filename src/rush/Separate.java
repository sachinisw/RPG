package rush;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class Separate {
	///for the rush hour domain after all training data has been produced, run this program.
	//for each case, take randomly 100 as training and 50 as testing.

	public static TreeSet<String> getObservationFiles(String obsfiles){
		TreeSet<String> obFiles = new TreeSet<String>();
		try {
			File dir = new File(obsfiles);
			List<File> files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
			for (File fileItem : files) {
				obFiles.add(fileItem.getCanonicalPath());
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
		return obFiles;	
	}

	public static void separateData() { //move to RUSHHOUR/trainobs/ RUSHHOUR/testobs
		String trainout = "/home/sachini/domains/RUSHHOUR/scenarios/0/trainobs/";
		String testout = "/home/sachini/domains/RUSHHOUR/scenarios/0/testobs/";
		int testcount = 0, traincount = 0;
		for (int i=0; i<20; i++) {
			String inputpath = "/home/sachini/domains/RUSHHOUR/scenarios/0/train/cases/"+i+"/data/decision/";
			TreeSet<String> files = getObservationFiles(inputpath);
			Random randomGenerator = new Random();
			int [] numbers = new int [50];
			int index = 0;
			while(index<50) {
				int num = randomGenerator.nextInt(files.size()-1);
				if(!containsNumber(numbers, num)) {
					numbers[index++] = num;
				}
			}
			for (String s : files) {
				String[] parts = s.split("/");
				String name = parts[parts.length-1].split("_")[0];
				ArrayList<String> lines = readFile(s);
				if(containsNumber(numbers, Integer.parseInt(name))) {
					writeToFile(lines, testout+testcount+"_tk.csv");
					testcount++;
				}else {
					writeToFile(lines, trainout+traincount+"_tk.csv");
					traincount++;
				}
			}
		}
	}

	public static ArrayList<String> readFile(String path) {
		ArrayList<String> lines = new ArrayList<String>();
		try {
			Scanner scanner = new Scanner (new File(path));
			while(scanner.hasNextLine()){
				lines.add(scanner.nextLine());
			}
			scanner.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return lines;
	}

	public static void writeToFile(ArrayList<String> text, String filename) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(filename, "UTF-8");
			for(int i=0; i<text.size(); i++){			
				writer.write(text.get(i)+"\n");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}finally{
			writer.close();
		}
	}

	private static boolean containsNumber(int [] arr, int n) {
		for (int i : arr) {
			if (i==n) {
				return true;
			}
		}
		return false;
	}

	public static void main(String[] args) {
		separateData();
	}
}
