package decisiontree;

import java.text.DecimalFormat;
import java.util.Arrays;

public class Test {
	private static boolean [] allocate(double val){
		int id1 = 0;
		boolean [] temp = new boolean [10];
		double [] binVals = new double [10];
		DecimalFormat df = new DecimalFormat("#.00"); 
		double cx = Double.parseDouble(df.format(val));
		System.out.println(cx);
		for(double i=0.10; i<=1.00; i+=0.10){ //assign a value to cbins
			double x = Double.parseDouble(df.format(i));
			binVals[id1++]= x;
		}

		double lo = 0.0;
		double hi = 0.0;
		for (int x=0; x<binVals.length-1; x++) {
			if(x==0){
				lo = Double.parseDouble(df.format(0.00));
				hi = Double.parseDouble(df.format(binVals[x]));
			}else{
				lo = Double.parseDouble(df.format(binVals[x]));
				hi = Double.parseDouble(df.format(binVals[x+1]));
			}

			if(Double.compare(lo, cx)<=0  && Double.compare(cx, hi)<=0){
				System.out.println("lo="+lo+" hi="+hi);
				temp[x]= true;
			}
		}
		return temp;
	}

	public static void assignToBins(double c, double r, double d){
		boolean [] arrC = allocate(c); 
		boolean [] arrR = allocate(r);
		boolean [] arrD = allocate(d);
		System.out.println(Arrays.toString(arrC));
		System.out.println(Arrays.toString(arrR));
		System.out.println(Arrays.toString(arrD));

	}

	public static double[] bin(){
		double min= 0.75, max=2.05;
		int size = 1;
		System.out.println(Math.ceil((max-min)/0.10));
		for(double i=min; i<=max; i+=0.10){ //doesn't have a bin for max val. need to add a buffer bucket for that.
			size++;
		}
		double [] bins = new double[size];
		DecimalFormat df = new DecimalFormat("#.00"); 
		for (int i=0; i<bins.length; i++) {
			bins[i] = Double.parseDouble(df.format(min));
			min+=0.10;
		}
		System.out.println("bins len="+bins.length);
		System.out.println(Arrays.toString(bins));
		
		String header = "";
		for (int i=0; i<bins.length-1; i++) {	//write header for bins
			header += bins[i] + " <= x <="  + bins[i+1] +",";
		}
		System.out.println("--------");
		System.out.println(header);
		return bins;
	}

	public static void main(String[] args) {
		//assignToBins(0.3, 0.03, 1.0);
		double[] binVals = bin();
		double hi, lo = 0.0;
		double t [] = new double []{0.91,0.75,1.0,2.0};
		int out [] = new int [binVals.length];
		DecimalFormat df = new DecimalFormat("#.00"); 

		for(int y=0; y<t.length; y++){
			double cx = Double.parseDouble(df.format(t[y]));
			for (int x=0; x<binVals.length; x++) {
				if(x==0){
					lo = Double.parseDouble(df.format(0.00));
					hi = Double.parseDouble(df.format(binVals[x]));
				}else{
					lo = Double.parseDouble(df.format(binVals[x-1]));
					hi = Double.parseDouble(df.format(binVals[x]));
				}
//				System.out.println("hi="+hi + " lo="+lo +" cx="+cx);
//				System.out.println("Double.compare(lo, cx)<=0 "+ Double.compare(lo, cx) );
//				System.out.println("Double.compare(cx, hi)<=0 " + Double.compare(cx, hi) );
				if(Double.compare(lo, cx)<=0  && Double.compare(cx, hi)<=0){
					System.out.println(cx +" put in bin   hi="+hi + " lo="+lo);
					out[x]= 1;
					break;
				}
			}
			System.out.println("val="+ cx+ Arrays.toString(out));
		}

	}

}
