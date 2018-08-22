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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class Preprocessor {
	private String datafilePath;

	public Preprocessor(String path){
		this.datafilePath = path;
	}

	public ArrayList<String> getDataFiles(){		
		ArrayList<String> dataFilePaths = new ArrayList<String>();
		try {
			File dir = new File(this.datafilePath);
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
				BinnedDataLine bdl = new BinnedDataLine(parts[0], parts[parts.length-1], Integer.parseInt(parts[parts.length-4]), Integer.parseInt(parts[parts.length-3]), Integer.parseInt(parts[parts.length-2]), Double.parseDouble(parts[4]), minmax[0], minmax[1]);//change indices here if new features are added
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
				fvals.add(Double.parseDouble(parts[parts.length-5])); //if new feature is added, parts.length-6
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
			String header = "ob,fo,0.0<=x<"+bins[0]+",";
			for (int i=0; i<bins.length-1; i++) {	//write header for bins
				header += bins[i] + "<=x<"  + bins[i+1]+",";
			}
			writer.write(header.substring(0,header.length()-1)+ ",dToCritical" + ",dToDesirable" + ",remainingLandmarks" +",Label" +"\n"); //lose the trailing comma and add class label header
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
			String header = "ob,fo,0.0<=x<"+bins[0]+",";
			for (int i=0; i<bins.length-1; i++) {	//write header for bins
				header += bins[i] + "<=x<"  + bins[i+1]+",";
			}
			writer.write(header.substring(0,header.length()-1)+ ",dToCritical" + ",dToDesirable" + ",remainingLandmarks" +",Label" +"\n"); //lose the trailing comma and add class label header
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
	
	public String getDatafilePath() {
		return datafilePath;
	}

	public void setDatafilePath(String datafilePath) {
		this.datafilePath = datafilePath;
	}
	
	public static void main(String[] args) {
		int scenario = 21;
		String path = "/home/sachini/BLOCKS/scenarios/"+scenario+"/data/decision/"; //contains unweighed F(o) for each observation
		String out = "/home/sachini/BLOCKS/scenarios/"+scenario+"/data/inputdecisiontree/"; //contains binned F(o) for each observation
		String outFull = "/home/sachini/BLOCKS/scenarios/"+scenario+"/data/inputdecisiontree/full.csv"; //contains binned F(o) for all observations
		Preprocessor pre = new Preprocessor(path);
		ArrayList<DataFile> dataFile = pre.readDataFiles(pre.getDataFiles());
		double [] minmax = pre.findObjectiveFunctionValueMinMax(dataFile);
		ArrayList<DataFile> binned = pre.preprocess(dataFile);
		for (DataFile outfile : binned) {
			pre.writeToFile(out, outfile, minmax);
		}
		pre.writeToFileFull(outFull, binned, minmax);//put all in one file.
		System.out.println("Scenario "+ scenario+ " done");
	}
}
