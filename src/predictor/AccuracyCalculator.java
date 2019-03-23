package predictor;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AccuracyCalculator {
	private static final Logger LOGGER = Logger.getLogger(AccuracyCalculator.class.getName());

	public static void producePredictionAccuracyFile() {
		int scenario = 0;
		String domain = "NAVIGATOR";//"FERRY";//"NAVIGATOR";//"BLOCKS"; //"EASYIPC";
		int instances  = 3;
		for (int instance = 1; instance <= instances; instance++) {
			String out = "";
			String prefix = "/home/sachini/domains/"+domain+"/scenarios/TEST"+scenario+"/inst";
			String outfile = prefix+String.valueOf(instance)+"/data/acc.txt";
			String inst_fulla=prefix+String.valueOf(instance)+"/data/instfull.arff";
			String inst_lm50a=prefix+String.valueOf(instance)+"/data/instlm50.arff";
			String inst_lm75a=prefix+String.valueOf(instance)+"/data/instlm75.arff";
			String inst_fullp=prefix+String.valueOf(instance)+"/data/predictions_full.csv";
			String inst_lm50p=prefix+String.valueOf(instance)+"/data/predictions_lm50.csv";
			String inst_lm75p=prefix+String.valueOf(instance)+"/data/predictions_lm75.csv";
			String f = computeAccuracy(inst_fullp, inst_fulla, "full");
			String l50 = computeAccuracy(inst_lm50p, inst_lm50a, "lm50");
			String l75 = computeAccuracy(inst_lm75p, inst_lm75a, "lm75");
			out += f+l50+l75;
			writeLineToFile(out, outfile);
		}
	}

	public static void writeLineToFile(String s, String out) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(out, "UTF-8");
				writer.write(s+"\n");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally{
			writer.close();
			LOGGER.log(Level.INFO, "Accuracy data : " + out  + " written");
		}
	}

	public static String computeAccuracy(String pfile, String afile, String mode) {
		Prediction pred = new Prediction(pfile, afile);
		pred.readFiles();
		int[] cmat = pred.generateConfusionMatrix();
		int sum = cmat[0]+cmat[1]+cmat[2]+cmat[3];
		double TPR = (double)cmat[0]/(double)(cmat[0]+cmat[1]);
		double TNR = (double)cmat[3]/(double)(cmat[3]+cmat[2]);
		double FPR = (double)cmat[2]/(double)(cmat[3]+cmat[2]);
		double FNR = (double)cmat[1]/(double)(cmat[1]+cmat[0]);
		DecimalFormat fm = new DecimalFormat("#.###");
		StringBuilder sb = new StringBuilder();
		sb.append("\nMode:"+ mode +"\nTotal Observations = "+String.valueOf(sum)+ "\n"+
				"TP\tTN\tFP\tFN\n"+
				String.valueOf(fm.format(TPR))+"\t"+String.valueOf(fm.format(TNR))+ "\t"+
				String.valueOf(fm.format(FPR))+"\t"+String.valueOf(fm.format(FNR)));
		return sb.toString();
	}
	public static void main(String args[]){
		producePredictionAccuracyFile(); //run after prediction_xxx files are manually created and weka output is copied to them
	}
}
