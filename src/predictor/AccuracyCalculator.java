package predictor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class AccuracyCalculator {
	private static final Logger LOGGER = Logger.getLogger(AccuracyCalculator.class.getName());

	public static TreeSet<String> getPredictionsFiles(String pred, int code){
		TreeSet<String> predictionFiles = new TreeSet<String>();
		try {
			File dir = new File(pred);
			List<File> files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
			for (File fileItem : files) {
				if(fileItem.getCanonicalPath().contains("predictions_") && !fileItem.getCanonicalPath().contains("old")
						&& !fileItem.getCanonicalPath().contains("acc")) {
					if(code==0) { //full space
						if(fileItem.getCanonicalPath().contains("_full")) {
							predictionFiles.add(fileItem.getCanonicalPath());
						}
					}else { //sample space
						if(fileItem.getCanonicalPath().contains("_tk")) {
							predictionFiles.add(fileItem.getCanonicalPath());
						}
					}
				}
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
		return predictionFiles;	
	}

	public static void producePredictionAccuracyFile(String domain) {
		int scenario = 0;
		int instances  = 3;
		if(!domain.equalsIgnoreCase("RUSHHOUR")) {
			for (int instance = 1; instance <= instances; instance++) {
				String prefix = "/home/sachini/domains/"+domain+"/scenarios/TEST"+scenario+"/inst";
				String outfile = prefix+String.valueOf(instance)+"/data/";
				String inst_fulla=prefix+String.valueOf(instance)+"/data/instfull.arff"; //******actuals
				String inst_tka=prefix+String.valueOf(instance)+"/data/tk_instfull.arff";   //******** actuals
				//			String inst_lm50a=prefix+String.valueOf(instance)+"/data/instlm50.arff"; //actuals
				//			String inst_lm75a=prefix+String.valueOf(instance)+"/data/instlm75.arff"; //actuals
				//String inst_fullp=prefix+String.valueOf(instance)+"/data/predictions_full.csv"; //********predicted by weka
				String predictionfilepath=prefix+String.valueOf(instance+"/data/");
				TreeSet<String> full_predictionFiles = getPredictionsFiles(predictionfilepath,0); //prediction files for the four classifiers
				TreeSet<String> tk_predictionFiles = getPredictionsFiles(predictionfilepath,1);
				for (String path : full_predictionFiles) {
					String pathparts [] = path.split("/");
					String nameparts [] = pathparts[pathparts.length-1].split("_");
					if(nameparts[nameparts.length-1].contains("full")) {
						String f = computeAccuracy(path, inst_fulla, "full");
						String name = pathparts[pathparts.length-1];
						writeLineToFile(f, outfile+"acc_"+name.substring(0,name.indexOf(".")));
					}
				}
				for (String path : tk_predictionFiles) {
					String pathparts [] = path.split("/");
					String nameparts [] = pathparts[pathparts.length-1].split("_");
					if(nameparts[nameparts.length-1].contains("tk")) {
						String tk = computeAccuracy(path, inst_tka, "tk");
						String name = pathparts[pathparts.length-1];
						writeLineToFile(tk, outfile+"acc_"+name.substring(0,name.indexOf(".")));
					}
				}
			}
		}else if(domain.equalsIgnoreCase("RUSHHOUR")) {
			String prefix = "/home/sachini/domains/"+domain+"/scenarios/0/testobs/";
			String outfile = prefix;
			String inst_fulla=prefix+"tk_instfull.arff"; //actuals
			String predictionfilepath=prefix;
			TreeSet<String> tk_predictionFiles = getPredictionsFiles(predictionfilepath,1);//predicted by weka
			for (String path : tk_predictionFiles) {
				String pathparts [] = path.split("/");
				String nameparts [] = pathparts[pathparts.length-1].split("_");
				if(nameparts[nameparts.length-1].contains("tk")) {
					String tk = computeAccuracy(path, inst_fulla, "tk");
					String name = pathparts[pathparts.length-1];
					writeLineToFile(tk, outfile+"acc_"+name.substring(0,name.indexOf(".")));
				}
			}
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

	public static String computeMCC(double TP, double TN, double FP, double FN) {
		//MCC = ( (TP*TN) - (FP*FN) ) / SQRT((TP+FP)*(TP+FN)*(TN+FP)*(TN+FN))
		BigDecimal btp = new BigDecimal(TP);
		BigDecimal btn = new BigDecimal(TN);
		BigDecimal bfp = new BigDecimal(FP);
		BigDecimal bfn = new BigDecimal(FN);
		BigDecimal top = btp.multiply(btn).subtract(bfp.multiply(bfn)) ;
		BigDecimal a = (btp.add(bfp));
		BigDecimal b = (btp.add(bfn));
		BigDecimal c = (btn.add(bfp));
		BigDecimal d = (btn.add(bfn));
		BigDecimal bot = a.multiply(b.multiply(c).multiply(d));
		if(bot.compareTo(new BigDecimal(0))==0) {
			return "-"; //division by zero
		}else {
			BigDecimal mcc = top.divide(bot.sqrt(new MathContext(10)),3,RoundingMode.UP);
			return mcc.toString();
		}
	}
	
	public static String computeAccuracy(String pfile, String afile, String mode) {
		Prediction pred = new Prediction(pfile, afile);
		pred.readFiles();
		int[] cmat = pred.generateConfusionMatrix();
		int sum = cmat[0]+cmat[1]+cmat[2]+cmat[3];
		double TPR = (double)cmat[0]/(double)(cmat[0]+cmat[1]); //tp/tp+fn
		double TNR = (double)cmat[3]/(double)(cmat[3]+cmat[2]); //tn/tn+fp
		double FPR = (double)cmat[2]/(double)(cmat[3]+cmat[2]);
		double FNR = (double)cmat[1]/(double)(cmat[1]+cmat[0]);
		double precision = (double)cmat[0]/(double)(cmat[0]+cmat[2]); //tp/tp+fp
		double recall = (double)cmat[0]/(double)(cmat[0]+cmat[1]);   //tp/tp+fn
		double f1 = 2.0 * ( (precision*recall) / (precision+recall));
		String MCC = computeMCC(cmat[0], cmat[3], cmat[2], cmat[1]);		
		DecimalFormat fm = new DecimalFormat("#.###");
		StringBuilder sb = new StringBuilder();
		sb.append("\nMode:"+ mode +"\nTotal Observations = "+String.valueOf(sum)+ "\n"+
				"TPR\tTNR\tFPR\tFNR\n"+
				String.valueOf(fm.format(TPR))+"\t"+String.valueOf(fm.format(TNR))+ "\t"+
				String.valueOf(fm.format(FPR))+"\t"+String.valueOf(fm.format(FNR)));
		sb.append("\nprecision=" + precision + " recall=" + recall + " F1="+f1 +"\n");
		sb.append("\nCOUNTS\n"+"TP\tTN\tFP\tFN\n");
		sb.append(cmat[0]+"\t"+cmat[3]+"\t"+cmat[2]+"\t"+cmat[1]);
		sb.append("\n\nMCC\n\n\n");
		sb.append(MCC);
		return sb.toString();
	}
	public static void main(String args[]){
		//run second, after running preprocess.java.
		//read predictions_***.csv and inst**.arff files. produces acc.txt with TNR,TPR,FPR,FNR values.
		String domain = "RUSHHOUR";//"FERRY";//"NAVIGATOR";//"BLOCKS"; //"EASYIPC"; //RUSHHOUR //TODO: change here first
		producePredictionAccuracyFile(domain); 
	}
}
