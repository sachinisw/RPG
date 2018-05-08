package out;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class CSVGenerator {

	private String outputfile;
	private ArrayList<String> data;

	public CSVGenerator(String filename, ArrayList<String> dat){
		outputfile = filename;
		data = dat;
	}

	public void writeOutput(){
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(outputfile, "UTF-8");
			writer.write("o,C,R,D,WC,WR,WD,WC(o),WR(o),W(1-D(o)),F(o) = WC(o) + WR(o) + W(1-D(o))");
			writer.println();

			for(int i=0; i<data.size(); i++){			
				writer.write(data.get(i));
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
