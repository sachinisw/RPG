package stats;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class TraceStats {

	public static TreeSet<String> getObservationFilePaths(String obsfiles){
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

	public static ArrayList<String> readFile(String path){
		ArrayList<String> content = new ArrayList<String>();
		try {
			Scanner sc = new Scanner(new File(path));
			while(sc.hasNextLine()) {
				content.add(sc.nextLine());
			}
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return content;
	}

	public static void getReducedStats() {
		int scenario = 0;
		int instances  = 3;
		int cases = 20;
		int testdatafilelimit = 10;
		String domain = "FERRY";//"FERRY";//"NAVIGATOR";//"BLOCKS"; //"EASYIPC";
		String prefix = "/home/sachini/domains/"+domain+"/scenarios/TEST"+scenario+"/inst";
		String statout = "data/reducedstat.txt";
		String scenariodir = "scenarios/";
		String obs50 = "obslm50/", obs75 = "obslm75/";
		for(int i=1; i<=instances; i++) { 
			String dirroot = prefix+String.valueOf(i)+"/"+scenariodir;
			String output = prefix+String.valueOf(i)+"/"+statout; 
			int count50 = 0, count75 = 0;
			for (int j = 0; j < cases; j++) {
				int filecount50 = 0, filecount75 = 0;
				String obslm50path = dirroot+String.valueOf(j)+"/"+obs50;
				String obslm75path = dirroot+String.valueOf(j)+"/"+obs75;
				TreeSet<String> paths50 = getObservationFilePaths(obslm50path);
				TreeSet<String> paths75 = getObservationFilePaths(obslm75path);
				for (String q : paths50) {
					if(filecount50==testdatafilelimit) {
						break;
					}
					ArrayList<String> content50 = readFile(q);
					ObservationFile ob50 = new ObservationFile(q, content50);
					count50+=ob50.countMissedYes();
					filecount50++;
//					System.out.println(ob50);
				}
				for (String p : paths75) {
					if(filecount75==testdatafilelimit) {
						break;
					}
					ArrayList<String> content75 = readFile(p);
					ObservationFile ob75 = new ObservationFile(p, content75);
					count75+=ob75.countMissedYes();
					filecount75++;
//					System.out.println(ob75);
				}
			}
			String s = "50lm\t\t75lm\n"+count50+"\t\t"+count75;
			writeLineToFile(s, output);
		}
	}

	public static void writeLineToFile(String line, String out) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(out, "UTF-8");
			writer.write(line+"\n");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally{
			writer.close();
		}
	}
	public static void main(String[] args) {
		getReducedStats();
		System.err.println("done");
	}
}
