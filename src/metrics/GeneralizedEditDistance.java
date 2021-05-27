package metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

//Sohrabi,Riabov, Udrea 2016, finding diverse high quality plans
public class GeneralizedEditDistance extends Distance{
	public int insertOps;
	public int deleteOps;
	public int replaceOps;
	final int[][] table;
	public ArrayList<String>refTokens; //plans/state sequences
	public ArrayList<String>inTokens;

	public GeneralizedEditDistance(ArrayList<String> a, ArrayList<String> b) {
		super(a,b);
		table = new int [incoming.size()+1][ref.size()+1];
		insertOps = deleteOps = replaceOps = 0;
		refTokens = new ArrayList<String>();
		inTokens = new ArrayList<String>();
	}

	public void preprocess(int type){ //return the tokenized string for a plan/state sequence
		if(type==1) {
			tokenizeActions();
			for (int i=0; i<ref.size(); i++) {
				refTokens.add(i,tokenmap.get(ref.get(i)));
			}
			for (int i=0; i<incoming.size(); i++) {
				inTokens.add(i,tokenmap.get(incoming.get(i)));
			}
		}else if(type==2) {
			tokenizeStates();
			for (int i=0; i<ref.size(); i++) { //adding a default value to allow for insertInto
				refTokens.add(i,String.valueOf(-111));
			}
			for (int i=0; i<incoming.size(); i++) {
				inTokens.add(i,String.valueOf(-111));
			}
			Iterator<String> itrt = tokenmap.keySet().iterator();
			while(itrt.hasNext()) { 
				String code = itrt.next();
				String idsparts[] = tokenmap.get(code).split(",");
				for (String s : idsparts) {
					if(!s.isEmpty()) {
						int pos = Integer.parseInt(s.substring(0,s.length()-1));
						if(s.contains("R")) {
							refTokens.set(pos,code);
						}else if(s.contains("I")) {
							inTokens.set(pos,code);
						}
					}
				}
			}
		}
	}

	public int[][] computeMinimumEditDistance(int type) {
		preprocess(type);
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

	//paper says cost=1-tokensimilarity(t1,t2)*w(t)  I dont know what w(t) means here. so assumed this to be 1.
	public double getGeneralizedEditSimilarity(int type) {
		computeMinimumEditDistance(type); //if type=2, its state edit distance.
		ArrayList<ReplacementPair> rp = countOperations();
		double insertCost = 1.0 * insertOps; //c_ins=1, weight of token=1 cost=c_ins * weight of token
		double deleteCost = 1.0 * deleteOps; //cost = w(t) weight of deleted token
		double replacementCost = 0.0;
		for (ReplacementPair replacementPair : rp) {
			replacementCost += (1.0 - getTokenSimilarity(replacementPair)) * 1.0; 
		}
		double totalCostForThePair = insertCost + deleteCost + replacementCost;
		double weightOfRef = (double)(ref.size() * 1.0);
		return 1.0 - (Math.min(totalCostForThePair/weightOfRef, 1.0));
	}

	//TODO:if tokens are the same sim=1, same parent=0.5, no relationship=0. potential improvement here. see paper for more detail
	public double getTokenSimilarity(ReplacementPair rp) {
		List<String> reflist = refTokens.subList(0, rp.refIndex+1); //parents of token being edited in ref plan [0..t]
		List<String> inlist = inTokens.subList(0, rp.inIndex+1); //parents of token being edited in Incoming plan [0..t']
		if(rp.refTok.equalsIgnoreCase(rp.inTok)) {
			return 1.0;
		}else if(rp.refIndex>=0 && rp.inIndex >=0) {
			if(hasRelationship(reflist, inlist)) { 		//find if reflist, inlist has a common parent (using hashcodes), or one is a parent of the other
				return 0.5;
			}else {
				return 0;
			}
		}else {
			return 0;
		}
	}
	
	private boolean hasRelationship(List<String> reflist, List<String> inlist) { 
		boolean parentInReflist = false, parentInInlist = false, hasCommonAncestor = false;
		for (int i=0; i<inlist.size()-1; i++) {
			if(reflist.subList(0,reflist.size()-1).contains(inlist.get(i))) {//list size-1 will check all parents of last entry.
				hasCommonAncestor = true;
			}
		}
		if(reflist.subList(0,reflist.size()-1).contains(inlist.get(inlist.size()-1))){ //edited in node is in the ref node parents list.
			parentInReflist = true;
		}
		if(inlist.subList(0,inlist.size()-1).contains(reflist.get(reflist.size()-1))){ //edited ref node is in the in node parents list.
			parentInInlist = true;
		}
		if(hasCommonAncestor || parentInReflist || parentInInlist) {
			return true;
		}
		return false;
	}

	public ArrayList<ReplacementPair> countOperations() {
		ArrayList<ReplacementPair> rp = new ArrayList<ReplacementPair>();
		int row = table.length-1;//incoming str
		int col = table[0].length-1;//reference str
		while(row>0 && col>0) {
			String rowid = inTokens.get(row-1); //col/row ids are hash codes //218962491
			String colid = refTokens.get(col-1); //218962491
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
					rp.add(new ReplacementPair(colid,col-1,rowid,row-1));//table has an extra col and a row
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
		public String refTok;
		public String inTok;
		public int refIndex;
		public int inIndex;

		public ReplacementPair(String t1, int refid, String t2, int inid) {
			refTok=t1;
			inTok=t2;
			refIndex = refid;
			inIndex = inid;
		}

		public String toString() {
			return refTok +"("+refIndex+")"+","+inTok+"("+inIndex+")";
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
				int[][] tbl = ed.computeMinimumEditDistance(1);
				for (int[] is : tbl) {
					System.out.println(Arrays.toString(is));
				}
	}
}
