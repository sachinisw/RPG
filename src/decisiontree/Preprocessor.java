package decisiontree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class Preprocessor {
	private static final Logger LOGGER = Logger.getLogger(Preprocessor.class.getName());

	public ArrayList<String> getDataFiles(String filedir){		
		ArrayList<String> dataFilePaths = new ArrayList<String>(); 
		try {
			File dir = new File(filedir);
			List<File> files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
			for (File fileItem : files) {
				dataFilePaths.add(fileItem.getCanonicalPath());
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return dataFilePaths;	
	}

	public ArrayList<DataFile> readDataFiles(ArrayList<String> files){
		ArrayList<DataFile> data = new ArrayList<DataFile>();
		for (String file : files) {
			DataFile df;
			try {
				df = new DataFile(file);
				Scanner scanner = new Scanner (new File(file));
				scanner.nextLine(); //lose the header
				while(scanner.hasNextLine()){
					df.getDataline().add(scanner.nextLine());
				}
				scanner.close();
				data.add(df);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return data;
	}

	public ArrayList<DataFile> preprocess(ArrayList<DataFile> df){
		ArrayList<DataFile> binned = new ArrayList<>();
		double[] minmax = findObjectiveFunctionValueMinMax(df);
		for (DataFile file : df) {
			ArrayList<String> data = file.getDataline();
			DataFile binnedFile = new DataFile(file.getFilename()); 
			for (String string : data) { //obs in current file
				String parts [] = string.split(",");
				//README:: change indices here for len-1 if new features are added. Make it (len-x-1)
				BinnedDataLine bdl = new BinnedDataLine(parts[0], parts[parts.length-1], Double.parseDouble(parts[1]), 
						Double.parseDouble(parts[2]), Double.parseDouble(parts[3]), 
						Integer.parseInt(parts[5]), Integer.parseInt(parts[6]), 
						Double.parseDouble(parts[7]), Double.parseDouble(parts[4]), minmax[0], minmax[1]);
				bdl.assignToBins(Double.parseDouble(parts[4]));
				binnedFile.getDataline().add(bdl.toString());
			}
			binned.add(binnedFile);
		}
		return binned;
	}

	public double[] findObjectiveFunctionValueMinMax(ArrayList<DataFile> df){
		ArrayList<Double> fvals = new ArrayList<Double>();
		for (DataFile file : df) {
			ArrayList<String> data = file.getDataline();
			for (String string : data) { //obs in current file
				String parts [] = string.split(",");
				fvals.add(Double.parseDouble(parts[parts.length-6])); //README:: if new feature is added, parts.length-7
			}
		}
		Collections.sort(fvals);
		return new double[]{fvals.get(0), fvals.get(fvals.size()-1)};
	}

	public void writeToFile(String outpath, DataFile binned, double[] minmax){
		PrintWriter writer = null;
		try {
			String name = binned.getFilename().split("/")[binned.getFilename().split("/").length-1];
			String filename = name.substring(0, name.indexOf("."));
			writer = new PrintWriter(outpath+filename+".csv", "UTF-8");
			double[] bins = bin(minmax);
			String header = "ob,c,r,d,fo,0.0<=x<"+bins[0]+",";
			for (int i=0; i<bins.length-1; i++) {	//write header for bins
				header += bins[i] + "<=x<"  + bins[i+1]+",";
			}
			writer.write(header.substring(0,header.length()-1)+ ",dToCritical" + ",dToDesirable" + ",remainingLandmarks" + ",hasLM" +",Label" +"\n"); //lose the trailing comma and add class label header
			for (int i=0; i<binned.getDataline().size(); i++) {		//write values
				writer.write(binned.getDataline().get(i)+"\n");
			}
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally{
			writer.close();
		}
	}

	public void writeToFileFull(String outpath, ArrayList<DataFile> binned, double[] minmax){
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(outpath, "UTF-8");
			double[] bins = bin(minmax);
			String header = "ob,c,r,d,fo,0.0<=x<"+bins[0]+",";
			for (int i=0; i<bins.length-1; i++) {	//write header for bins
				header += bins[i] + "<=x<"  + bins[i+1]+",";
			}
			writer.write(header.substring(0,header.length()-1)+ ",dToCritical" + ",dToDesirable" + ",hasLM" +",Label" +"\n"); //lose the trailing comma and add class label header
			for (DataFile f : binned) {
				for (int i=0; i<f.getDataline().size(); i++) {		//write values
					writer.write(f.getDataline().get(i)+"\n");
				}
			}
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally{
			writer.close();
		}
	}

	public double[] bin(double[] minmax){
		double min= minmax[0], max=minmax[1];
		int size = 1;
		for(double i=min; i<=max; i+=0.10){ 
			size++;
		}
		double [] bins = new double[size];
		DecimalFormat df = new DecimalFormat("#.00"); 
		for (int i=0; i<bins.length; i++) {
			bins[i] = Double.parseDouble(df.format(min));
			min+=0.10;
		}
		return bins;
	}

	public ArrayList<DataFile> preprocessTrainingData(String datadir, String out, String outFull) {
		ArrayList<DataFile> dataFile = readDataFiles(getDataFiles(datadir));
		double [] minmax = findObjectiveFunctionValueMinMax(dataFile);
		ArrayList<DataFile> binned = preprocess(dataFile);
		for (DataFile outfile : binned) {
			writeToFile(out, outfile, minmax);
		}
		writeToFileFull(outFull, binned, minmax);//put all in one file.
		return binned;
	}

	//aggeregate data with only ob R D dCri dDes remLM hasLM Label
	//PICK-UP R	0.33,0.04,0.08,1.29,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,5,5,11,0.36,N
	public void aggregateTrainingData(String inroot, String aggfile, int cases, String outpath) {
		ArrayList<String> lines = new ArrayList<>();
		String header = "";
		PrintWriter writer = null;
		for(int i=0; i<cases; i++) {
			String infile = inroot+i+aggfile;
			try {
				Scanner scanner = new Scanner (new File(infile));
				if(i==0) {
					String s[] = scanner.nextLine().split(",");
					header = s[0]+","+s[2]+","+s[3]+","+s[s.length-4]+","+s[s.length-3]
							+","+s[s.length-2]+","+s[s.length-1]; //read header once
				}else {
					scanner.nextLine();//lose the header
				}
				while(scanner.hasNextLine()){
					String t [] = scanner.nextLine().split(",");
					String line = t[0]+","+t[2]+","+t[3]+","+t[t.length-4]+","+t[t.length-3]
							+","+t[t.length-2]+","+t[t.length-1];
					lines.add(line);
				}
				scanner.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		try {
			writer = new PrintWriter(outpath, "UTF-8");
			writer.write(header+"\n");
			for (String l : lines) {
				writer.write(l);
				writer.write("\n");
			}
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally{
			writer.close();
		}
	}

	public ArrayList<DataFile> preprocessTestingData(String datadir, String out, String outFull, int testtype) {
		ArrayList<DataFile> dataFile = readDataFiles(getDataFiles(datadir));
		ArrayList<DataFile> cleaned = new ArrayList<DataFile>();
		ArrayList<DataFile> binned = null;
		if(testtype==1) {//full trace
			for (DataFile df : dataFile) {
				String parts [] = df.getFilename().split("/");
				if(!parts[parts.length-1].contains("lm.csv")) {
					cleaned.add(df);
				}
			}
		} else if(testtype==2) { //limited trace 50
			for (DataFile df : dataFile) {
				String parts [] = df.getFilename().split("/");
				if(parts[parts.length-1].contains("lm50.csv")) {
					cleaned.add(df);
				}
			}
		}else if(testtype==3) { //limited trace 75
			for (DataFile df : dataFile) {
				String parts [] = df.getFilename().split("/");
				if(parts[parts.length-1].contains("lm75.csv")) {
					cleaned.add(df);
				}
			}
		}
		double [] minmax = findObjectiveFunctionValueMinMax(cleaned);
		binned = preprocess(cleaned);
		for (DataFile outfile : binned) {
			writeToFile(out, outfile, minmax);
		}
		writeToFileFull(outFull, binned, minmax);//put all in one file.
		return binned;
	}

	//generate instance specific csv with ob-c-r-d-fo-dc-dd-lm-haslm-label only.
	//train the model WITHOUT bin columns. bin columns change from problem to problem. cant use that kind of a model to predict unseen data
	//README: change indices in values variable when new feature is added.
	public static void writeInstanceSpecificOutput(ArrayList<ArrayList<DataFile>> df, String out) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(out, "UTF-8");
			String header = "ob,c,r,d,fo,dToCritical,dToDesirable,hasLM,Label" +"\n";
			writer.write(header);
			for (ArrayList<DataFile> curinst : df) {
				for (DataFile file : curinst) {
					for (int i=0; i<file.getDataline().size(); i++) {		//write values
						String parts[] = file.getDataline().get(i).split(",");
						String values = parts[0]+","+parts[1]+","+parts[2]+","+parts[3]+","+parts[4]+
								","+parts[parts.length-4]+","+parts[parts.length-3]+","+parts[parts.length-2]+","+parts[parts.length-1];
						writer.write(values+"\n");
					}
				}
			}
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally{
			writer.close();
			LOGGER.log(Level.INFO, "File: " + out  + " CSV written");
		}
	}

	//Creates input.csv for the decision tree
	//README:  Remove bin columns from weka preprocessor
	public static void main(String[] args) {
		int scenario = 0, cases = 20;
		int mode = 1; // -1=debug 0-train, 1-test TODO: CHANGE HERE FIRST
		String domain = "EASYIPC";//"FERRY";//"NAVIGATOR";//"BLOCKS"; //"EASYIPC";
		int instances  = 1;
		int casePerInstance = 20; //change to 20
		Preprocessor pre = new Preprocessor();
		if(mode==-1) {
			LOGGER.log(Level.CONFIG, "Preprocessing for DEBUG mode. DOMAIN======"+ domain);
			String out = "/home/sachini/domains/"+domain+"/scenarios/"+scenario+"/data/inputdecisiontree/"; //contains binned F(o) for each observation + CRD
			String outFull = "/home/sachini/domains/"+domain+"/scenarios/"+scenario+"/data/inputdecisiontree/full.csv"; //contains binned F(o) for all observations
			String inputfilepath = "/home/sachini/domains/"+domain+"/scenarios/"+scenario+"/data/decision/"; //contains unweighed F(o) for each observation
			pre.preprocessTrainingData(inputfilepath, out, outFull);
			String aggroot = "/home/sachini/domains/"+domain+"/scenarios/"+scenario+"/";
			String aggfile = "/data/inputdecisiontree/full.csv";
			String outpath = "/home/sachini/domains/"+domain+"/scenarios/"+scenario+"/data/aggregate.csv";
			pre.aggregateTrainingData(aggroot, aggfile, cases, outpath);
		}else if(mode==0) { //from 20 training problems, produce CSV for WEKA to train the model
			for(int currentCase=0; currentCase<cases; currentCase++) {
				LOGGER.log(Level.CONFIG, "Preprocessing for TRAINING mode DOMAIN======"+ domain);
				LOGGER.log(Level.CONFIG, "CASE = "+ currentCase);
				String out = "/home/sachini/domains/"+domain+"/scenarios/"+scenario+"/train/cases/"+currentCase+"/data/inputdecisiontree/"; //contains binned F(o) for each observation + CRD
				String outFull = "/home/sachini/domains/"+domain+"/scenarios/"+scenario+"/train/cases/"+currentCase+"/data/inputdecisiontree/full.csv"; //contains binned F(o) for all observations
				String inputfilepath = "/home/sachini/domains/"+domain+"/scenarios/"+scenario+"/train/cases/"+currentCase+"/data/decision/"; //contains unweighed F(o) for each observation
				pre.preprocessTrainingData(inputfilepath, out, outFull);
			}
			String aggroot = "/home/sachini/domains/"+domain+"/scenarios/"+scenario+"/train/cases/";
			String aggfile = "/data/inputdecisiontree/full.csv";
			String outpath = "/home/sachini/domains/"+domain+"/scenarios/"+scenario+"/train/cases/data/aggregate.csv";
			pre.aggregateTrainingData(aggroot, aggfile, cases, outpath);
		}else if (mode==1){
			LOGGER.log(Level.CONFIG, "Preprocessing for TESTING mode DOMAIN======"+ domain);
			for (int instance = 1; instance <= instances; instance++) {
				String prefix = "/home/sachini/domains/"+domain+"/scenarios/TEST"+scenario+"/inst";
				String instout_full=prefix+String.valueOf(instance)+"/data/instfull.csv";
				String instout_lm50=prefix+String.valueOf(instance)+"/data/instlm50.csv";
				String instout_lm75=prefix+String.valueOf(instance)+"/data/instlm75.csv";
				ArrayList<ArrayList<DataFile>> inst_full = new ArrayList<>();
				ArrayList<ArrayList<DataFile>> inst_lm50 = new ArrayList<>();
				ArrayList<ArrayList<DataFile>> inst_lm75 = new ArrayList<>();
				for(int instcase = 0; instcase<casePerInstance; instcase++) {
					String inputfilepath = prefix+String.valueOf(instance)+"/scenarios/"+String.valueOf(instcase)+"/data/decision/"; 
					String out = prefix+String.valueOf(instance)+"/scenarios/"+String.valueOf(instcase)+"/data/inputdecisiontree/"; 
					String outFull = prefix+String.valueOf(instance)+"/scenarios/"+String.valueOf(instcase)+"/data/inputdecisiontree/full.csv";
					String outFull_lm50 = prefix+String.valueOf(instance)+"/scenarios/"+String.valueOf(instcase)+"/data/inputdecisiontree/full_50lm.csv";
					String outFull_lm75 = prefix+String.valueOf(instance)+"/scenarios/"+String.valueOf(instcase)+"/data/inputdecisiontree/full_75lm.csv";
					ArrayList<DataFile> df = pre.preprocessTestingData(inputfilepath, out, outFull, 1);
					ArrayList<DataFile> dlm50 = pre.preprocessTestingData(inputfilepath, out, outFull_lm50, 2);
					ArrayList<DataFile> dlm75 = pre.preprocessTestingData(inputfilepath, out, outFull_lm75, 3);
					inst_full.add(df);
					inst_lm50.add(dlm50);
					inst_lm75.add(dlm75);
				}
				writeInstanceSpecificOutput(inst_full, instout_full);
				writeInstanceSpecificOutput(inst_lm50, instout_lm50);
				writeInstanceSpecificOutput(inst_lm75, instout_lm75);
				LOGGER.log(Level.INFO, "Instance: " + instance  + " complete");
			}
		}
		LOGGER.log(Level.INFO, "Preprocessing done");
	}
}
