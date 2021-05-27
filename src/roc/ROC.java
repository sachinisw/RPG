package roc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;

public class ROC {
	private File dataFile; //sorted ascending, labelled, csv

	public ROC(String filename){
		this.setDataFile(new File(filename));
	}

	public ArrayList<String> readDataFile(){
		ArrayList<String> data = new ArrayList<String>();
		try {
			Scanner scanner = new Scanner (dataFile);
			scanner.nextLine(); //lose the header
			while(scanner.hasNextLine()){
				data.add(scanner.nextLine());
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return data;
	}

	public ArrayList<String> applyLabels(ArrayList<String> data){
		ArrayList<String> labeledData = new ArrayList<String>();
		for (String string : data) {
			String part = string.substring(0, string.length());
			String [] item = string.split(",");
			if(item[0].equalsIgnoreCase("PICK-UP T") || item[0].equalsIgnoreCase("STACK T M")){
				labeledData.add(part+=",N");
			}else{
				labeledData.add(part+=",Y");
			}
		}
		
		//write it to file for R
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("/home/sachini/BLOCKS/6-labeled.csv", "UTF-8");
			writer.write("O,C,R,D,CW,RW,DW,WC,WR,WD,FO,L"+"\n");
			for (String s : labeledData) {
				writer.write(s+"\n");
			}
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally{
			writer.close();
		}
		
		return labeledData;
	}
	
	public double getPositiveExamples(ArrayList<String> data){
		double P = 0.0;
		for (String string : data) {
			String [] item = string.split(",");
			if(item[item.length-1].equals("Y")){
				P++;
			}
		}
		return P;
	}

	public double getNegativeExamples(ArrayList<String> data){
		double N = 0.0;
		for (String string : data) {
			String [] item = string.split(",");
			if(item[item.length-1].equals("N")){
				N++;
			}
		}
		return N;
	}

	public ArrayList<Point> getROCPoints(ArrayList<String> data, double P, double N){
		ArrayList<Point> pt = new ArrayList<Point>();
		for(double x=0.0000; x<=1.00; x+=0.0001){
			int FP = 0, TP = 0;	
//			System.out.println("x="+x);
			for(int i=0; i<data.size(); i++){
				String [] item = data.get(i).split(",");
				double d = Double.parseDouble(item[item.length-2]);
//				System.out.println("d="+d);
				if(d>=x){
					if(item[item.length-1].equals("Y")){
						TP++;
					}else{
						FP++;
					}
				}
			}
			DecimalFormat df = new DecimalFormat("#.0000"); 
			double tp = Double.parseDouble(df.format((double)TP/P));
			double fp = Double.parseDouble(df.format((double)FP/N));
			pt.add(new Point(tp, fp));
		}
		return pt;
	}
	
	public void writeROCPointsToFile(String filename, ArrayList<Point> points){
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(filename, "UTF-8");
			writer.write("FPR,TPR\n");
			for (Point point : points) {
				writer.write(point.getFP()+","+point.getTP()+"\n");
			}
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally{
			writer.close();
		}
	}

	public File getDataFile() {
		return dataFile;
	}

	public void setDataFile(File dataFile) {
		this.dataFile = dataFile;
	}

	public static void main(String[] args) {
		String filename = "/home/sachini/BLOCKS/6.csv";
		String out = "/home/sachini/BLOCKS/6-out.csv";
		ROC roc = new ROC(filename);
		ArrayList<String> data = roc.readDataFile();
		ArrayList<String> labeledData = roc.applyLabels(data);
		double P = roc.getPositiveExamples(labeledData);
		double N = roc.getNegativeExamples(labeledData);
		System.out.println("Computing ROC points");
		ArrayList<Point> rocpt = roc.getROCPoints(labeledData, P, N);
		for (Point point : rocpt) {
			System.out.println(point);
		}
		roc.writeROCPointsToFile(out, rocpt);
		System.out.println("done");
	}
}
