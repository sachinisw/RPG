package metrics;

import java.util.ArrayList;
import java.util.Arrays;

public class EditDistance {
	public ArrayList<String> p1;
	public ArrayList<String> p2;
	public int insertOps;
	public int deleteOps;
	public int replaceOps;
	int[][] table;

	public EditDistance(ArrayList<String> a, ArrayList<String> b) {
		p1 = a;
		p2 = b;
		table = new int [p2.size()+1][p1.size()+1];
		insertOps = deleteOps = replaceOps = 0;
	}

	public int[][] getMinimumEditDistance() {
		for(int j=0; j<table[0].length; j++) { //row=0 id is null. fill the row with number of additions each substring will need compared to null string
			table[0][j]=j;
		} 
		for(int i=0; i<table.length; i++) { //col=0 id is null. fill first col with number of deletes each substring will have to make to equal the null string
			table[i][0]=i;
		}
		for(int i=1; i<table.length; i++) {
			String rowid = p2.get(i-1);
			for(int j=1; j<table[i].length; j++) {
				String colid = p1.get(j-1);
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

	public void countOperations() {
		print();
		int row = table.length-1;
		int col = table[0].length-1;
		while(row>0 && col>0) {
			String rowid = p2.get(row-1);
			String colid = p1.get(col-1);
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
					row--;
					col--;
				}
			}else {
				row--;
				col--;
			}
			
		}
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
		EditDistance ed = new EditDistance(p1, p2);
		ed.getMinimumEditDistance();
		ed.countOperations();
	}
}
