package decisiontree;

import java.util.ArrayList;

public class DataFile {
	private String filename;
	private ArrayList<String> datalines;
	
	public DataFile(String name){
		datalines = new ArrayList<String>();
		filename = name;
	}
	
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public ArrayList<String> getDataline() {
		return datalines;
	}
	public void setDataline(ArrayList<String> dataline) {
		this.datalines = dataline;
	}
}
