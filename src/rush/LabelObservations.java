package rush;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import run.CriticalState;

public class LabelObservations {
	//read obs/u obs/ua mark [move (bad object) ] lines as Y. others as N
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

	public static void label(int start) {
		for (int x=0; x<LabelConfigs.instanceCases; x++) { //blocks,navigator,easyipc, ferry -each instance has 20 problems
			String criticalfile = LabelConfigs.prefix+String.valueOf(x)+LabelConfigs.criticalStateFile;
			String obs = LabelConfigs.prefix+String.valueOf(x)+LabelConfigs.observationFiles;
			TreeSet<String> files = getObservationFiles(obs); //all obs/u and obs/ua files
			CriticalState cs = new CriticalState(criticalfile);
			cs.readCriticalState();
			applyLabel(getCriticalObject(cs), files);
		}
	}

	private static String getCriticalObject(CriticalState cs) {
		String c = "";
		Pattern pattern = Pattern.compile("C[0-9]");
		Matcher matcher = pattern.matcher(cs.getCriticalStatePredicates().get(0).split(",")[0]);
		while (matcher.find()) {
			c = matcher.group();
		}
		return c;
	}

	public static void applyLabel(String criticalOb, TreeSet<String> files) {
		int filecounteru = 0;
		int filecounterua = 0;
		for (String f : files) {
			ArrayList<String> updated = new ArrayList<String>();
			ArrayList<String> lines = readPlanFile(f);
			for (String l : lines) {
				if(l.startsWith("(")) {
					String s = l.substring(1, l.length()-1).toUpperCase();
					Pattern pattern = Pattern.compile("C[0-9]");
					Matcher matcher = pattern.matcher(s);
					String ob = "";
					while(matcher.find()) {
						ob = matcher.group();
					}
					if(ob.equalsIgnoreCase(criticalOb)) {
						s="Y:"+s;
					}else {
						s="N:"+s;
					}
					updated.add(s);
				}
			}
			String fpath [] = f.split("\\/");
			String src = fpath[fpath.length-2];
			String pathname = "";
			for (int i=0; i<fpath.length-3; i++) {
				pathname+=fpath[i]+"/";
			}
			if(src.equalsIgnoreCase("u")) {
				pathname = pathname+"finalob/u/"+filecounteru++;
			}else {
				pathname = pathname+"finalob/ua/"+filecounterua++;
			}
			writeToFile(updated, pathname);
		}
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
	
	public static ArrayList<String> readPlanFile(String filename){
		Scanner reader;
		ArrayList<String> lines = new ArrayList<String>();
		try {
			reader = new Scanner(new File(filename));
			while(reader.hasNextLine()){
				lines.add(reader.nextLine().trim());
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return lines;
	}
	
	public static void main(String[] args) {
		label(1);
	}
}
