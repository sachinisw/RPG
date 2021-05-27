package stats;

import java.util.ArrayList;
import java.util.Arrays;

public class ObservationFile {
	private String filepath;
	private ArrayList<String> content;
	
	public ObservationFile(String path, ArrayList<String> c) {
		this.filepath = path;
		this.content = c;
	}
	
	public int countMissedYes() { //return the number of Y labels that have been marked with *. These are missed because of lm limit
		int count = 0;
		for (String s : content) {
			String label = s.split(":")[0].substring(1);
			if(s.startsWith("*") && label.equalsIgnoreCase("Y")) {
				count++;
			}
		}
		return count;
	}
	
	public int getReducedSize() {
		int count = 0;
		for (String s : content) {
			if(!s.startsWith("*")) {
				count++;
			}
		}
		return count;
	}
	public String toString() {
		return filepath + "---" + Arrays.toString(content.toArray());
	}
	public String getFilepath() {
		return filepath;
	}
	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}
	public ArrayList<String> getContent() {
		return content;
	}
	public void setContent(ArrayList<String> content) {
		this.content = content;
	}
	
}
