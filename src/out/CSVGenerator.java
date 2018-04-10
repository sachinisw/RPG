package out;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class CSVGenerator {

	private String outputfile;
	private ArrayList<DataLine> data;
	
	public CSVGenerator(String filename, ArrayList<DataLine> dat){
		outputfile = filename;
		data = dat;
	}

	public void writeOutput(){
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(outputfile, "UTF-8");
			writer.write("OB,C,R,D");
			writer.println();

			for(int i=0; i<data.size(); i++){			
				writer.write(data.get(i).toString());
				writer.println();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}finally{
			writer.close();
		}
	}
	
	public String getOutputfile() {
		return outputfile;
	}

	public void setOutputfile(String outputfile) {
		this.outputfile = outputfile;
	}
}
