package metrics;

import java.util.ArrayList;
import java.util.Arrays;

//Sohrabi,Riabov, Udrea 2016, finding diverse high quality plans
public class GeneralizedEditDistance extends Distance{
	public int insertOps;
	public int deleteOps;
	public int replaceOps;
	final int[][] table;
	public ArrayList<String>refTokens;
	public ArrayList<String>inTokens;
	
	public GeneralizedEditDistance(ArrayList<String> a, ArrayList<String> b) {
		super(a,b);
		table = new int [incoming.size()+1][ref.size()+1];
		insertOps = deleteOps = replaceOps = 0;
		refTokens = new ArrayList<String>();
		inTokens = new ArrayList<String>();
	}

	public void preprocess(){ //return the tokenized string for a plan/state sequence
		tokenize();
		for (int i=0; i<ref.size(); i++) {
			refTokens.add(i,tokenmap.get(ref.get(i)));
		}
		for (int i=0; i<incoming.size(); i++) {
			inTokens.add(i,tokenmap.get(incoming.get(i)));
		}
	}
	
	public int[][] computeMinimumEditDistance() {
		preprocess();
		for(int j=0; j<table[0].length; j++) { //row=0 id is null. fill the row with number of additions each substring will need compared to null string
			table[0][j]=j;
		} 
		for(int i=0; i<table.length; i++) { //col=0 id is null. fill first col with number of deletes each substring will have to make to equal the null string
			table[i][0]=i;
		}
		for(int i=1; i<table.length; i++) {
			String rowid = inTokens.get(i-1);
			for(int j=1; j<table[i].length; j++) {
				String colid = refTokens.get(j-1);
				if(rowid.equalsIgnoreCase(colid)) {
					table[i][j] = table[i-1][j-1];
				}else {
					int [] nbr = new int[3];
					nbr[0] = table[i][j-1]; //left
					nbr[1] = table[i-1][j]; //up
					nbr[2] = table[i-1][j-1]; //diagonal
					Arrays.sort(nbr);
					table[i][j] = nbr[0]+1;
				}
			}
		}
		return table;
	}

	public int getGeneralizedEditSimilarity() {
		computeMinimumEditDistance();
		ArrayList<ReplacementPair> rp = countOperations();
		double insertCost = 1 * insertOps; //c_ins=1, weight of token=1 cost=c_ins * weight of token
		double deleteCost = 1 * deleteOps;
//		double replacementCost = (1 - sim) * insertOps;
		print();
		System.out.println(insertOps);
		System.out.println(deleteOps);
		System.out.println(replaceOps);
		System.out.println(table[table.length-1][table[0].length-1]);
		return 0;
	}
	
	public ArrayList<ReplacementPair> countOperations() {
		ArrayList<ReplacementPair> rp = new ArrayList<ReplacementPair>();
		int row = table.length-1;
		int col = table[0].length-1;
		while(row>0 && col>0) {
			String rowid = incoming.get(row-1);
			String colid = ref.get(col-1);
			int val = table[row][col];
			if(!rowid.equalsIgnoreCase(colid)) {
				if(val-1==table[row][col-1]) {
					deleteOps++;
					col--;
				}else if(val-1==table[row-1][col]) {
					insertOps++;
					row--;
				}else {
					replaceOps++;
					rp.add(new ReplacementPair(rowid,colid));
					row--;
					col--;
				}
			}else {
				row--;
				col--;
			}
		}
		return rp;
	}

	public void print() {
		System.out.println();
		for(int i=0; i<table.length; i++) {
			for(int j=0; j<table[i].length; j++) {
				System.out.print(table[i][j]+"\t");
			}
			System.out.println();
		}
	}
	
	class ReplacementPair{
		public String tok1;
		public String tok2;
		
		public ReplacementPair(String t1, String t2) {
			tok1=t1;
			tok2=t2;
		}
		
		public String toString() {
			return tok1 +","+tok2;
		}
	}
	
	public static void main(String[] args) {
		ArrayList<String> p1 = new ArrayList<String>();
		ArrayList<String> p2 = new ArrayList<String>();
		p1.add("S");
		p1.add("a");
		p1.add("t");
		p1.add("u");
		p1.add("r");
		p1.add("d");
		p1.add("a");
		p1.add("y");
		p2.add("S");
		p2.add("u");
		p2.add("n");
		p2.add("d");
		p2.add("a");
		p2.add("y");
		p2.add("s");
		GeneralizedEditDistance ed = new GeneralizedEditDistance(p1, p2);
		ed.getGeneralizedEditSimilarity();
	}
}
