package out;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class CSVGenerator {

	private String outputfile;
	private ArrayList<String> data;
	private int type;

	public CSVGenerator(String filename, ArrayList<String> dat, int t){ //0 for deicison tree, 1 for weighted
		outputfile = filename;
		data = dat;
		type = t;
	}

	public void writeOutput(){
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(outputfile, "UTF-8");
			if(type==1){
				writer.write("o,C,R,D,WC,WR,WD,WCo,WRo,W1-Do,Fo,Label");//Fo = WCo + WRo + W1-Do
			}else if(type==0){
				writer.write("o,C,R,D,Fo,DistToCritical,DistToDesirable,RemainingUndesirableLM,Label");
			}
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

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
}
