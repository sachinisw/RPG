package predictor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Preprocess {
	/**
	 * create copies of the InstFull.arff, Inst50lm.arff, Inst75lm.arff files with class label field changed to ?
	 * provide the resulting file to the learned model as a test set and get the prediction
	 * compare the prediction file and original .arff file and record the prediction accuracy
	 */
	private static final Logger LOGGER = Logger.getLogger(Preprocess.class.getName());

	public static ArrayList<String> produceARFFCopy(String filename) {
		Scanner scf;
		ArrayList<String> modified = new ArrayList<String>();
		try {
			scf = new Scanner (new File(filename));
			while(scf.hasNext()) {
				String line = scf.nextLine();
				if(!line.startsWith("@") && !line.isEmpty()) {
					StringBuilder sb = new StringBuilder(line);
					sb.setCharAt(sb.length()-1, '?');
					modified.add(sb.toString());
				}else {
					modified.add(line);
				}
			}
			scf.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return modified;
	}

	public static void writeStructureToFile(ArrayList<String> in, String out) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(out, "UTF-8");
			for (String s : in) {
				writer.write(s+"\n");
			}
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally{
			writer.close();
			LOGGER.log(Level.INFO, "Prediction Input File: " + out  + " ARFF written");
		}
	}

	public static void preparePredictionFile() {
		int scenario = 0;
		String domain = "NAVIGATOR";//"FERRY";//"NAVIGATOR";//"BLOCKS"; //"EASYIPC";
		int instances  = 3;
		for (int instance = 1; instance <= instances; instance++) {
			String prefix = "/home/sachini/domains/"+domain+"/scenarios/TEST"+scenario+"/inst";
			String inst_full=prefix+String.valueOf(instance)+"/data/instfull.arff";
			String inst_lm50=prefix+String.valueOf(instance)+"/data/instlm50.arff";
			String inst_lm75=prefix+String.valueOf(instance)+"/data/instlm75.arff";
			ArrayList<String> full = produceARFFCopy(inst_full);
			ArrayList<String> lm50 = produceARFFCopy(inst_lm50);
			ArrayList<String> lm75 = produceARFFCopy(inst_lm75);
			writeStructureToFile(full, prefix+String.valueOf(instance)+"/data/instfullpred.arff");
			writeStructureToFile(lm50, prefix+String.valueOf(instance)+"/data/instlm50pred.arff");
			writeStructureToFile(lm75, prefix+String.valueOf(instance)+"/data/instlm75pred.arff");
			LOGGER.log(Level.INFO, "Prediction Preprocessing Instance: " + instance  + " complete");
		}
	}

	public static void main(String[] args) {
		preparePredictionFile(); //run this first in prediction
	}
}
