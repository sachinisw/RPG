package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class CountLines {
	public static void main(String[] args) {
		String lineFile = "/home/sachini/BLOCKS/all.txt";
		ArrayList<String> leafs = new ArrayList<String>();
		
		try {
			Scanner scan = new Scanner(new File(lineFile));
			while(scan.hasNextLine()){
				String line = scan.nextLine();
				System.out.println(Arrays.toString(line.split(":")));
				
				if(line.split(":")[1].trim().isEmpty()){
					leafs.add(line);
				}
			}
			scan.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println(leafs.size());
		
//		String filename = "/home/sachini/BLOCKS/graph4.dot";
//		Set<String> noDup = new LinkedHashSet<>(); //unordered. no duplicate data structure
//		try {
//			Scanner scan = new Scanner(new File(filename));
//			while(scan.hasNextLine()){
//				noDup.add(scan.nextLine());
//			}
//			scan.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
//		
//		try {
//			PrintWriter writer = new PrintWriter(new File("/home/sachini/BLOCKS/graph4"+"_nodup.dot"));
//			Iterator<String> itr = noDup.iterator();
//			while(itr.hasNext()){
//				writer.write(itr.next());
//				writer.write("\n");
//			}
//			writer.flush();
//			writer.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
	}
}
