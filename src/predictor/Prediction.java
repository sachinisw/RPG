package predictor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;

public class Prediction {
	private String predictionFile;
	private String actualFile;
	private ArrayList<String> predictionLines;
	private ArrayList<String> actualLines;

	public Prediction(String pfile, String afile) {
		predictionFile = pfile;
		actualFile = afile;
		predictionLines = new ArrayList<String>();
		actualLines = new ArrayList<String>();
	}

	public void readFiles() {
		try {
			Scanner scp = new Scanner(new File(predictionFile));
			Scanner sca = new Scanner(new File(actualFile));
			scp.nextLine(); //lose the first line
			while (scp.hasNext()) {
				String line = scp.nextLine();
				predictionLines.add(line);
			}
			scp.close();
			while (sca.hasNext()) {
				String line = sca.nextLine();
				if (!line.startsWith("@") && !line.isEmpty()) {
					actualLines.add(line);
				}
			}
			sca.close();
		} catch (IOException io) {
			System.out.println(io.getMessage());
		}
	}

	private ArrayList<DataItem> createDataItemsForPredictions(){
		ArrayList<DataItem> predicted = new ArrayList<>();
		for (int i=0; i<this.predictionLines.size(); i++){
			String line = predictionLines.get(i);
			String id = line.split(",")[0];
			String val = line.split(",")[2].split(":")[1];
			DataItem di = new DataItem(Integer.parseInt(id), val);
			predicted.add(di);
		}
		return predicted;
	}

	private ArrayList<DataItem> createDataItemsForActuals(){
		ArrayList<DataItem> actual = new ArrayList<>();
		for (int i=0; i<this.actualLines.size(); i++){
			String line = this.actualLines.get(i);
			String lineparts [] = line.split(",");
			String val = lineparts[lineparts.length-1]; //actual label is the last element in the array
			DataItem di = new DataItem(i+1, val);
			actual.add(di);
		}
		return actual;
	}

	public int[]generateConfusionMatrix(){
		ArrayList<DataItem> dip = createDataItemsForPredictions();
		ArrayList<DataItem> dia = createDataItemsForActuals();
		int TN = 0, FP = 0, TP = 0, FN = 0; //TN=N predicted as N,  FN=actual Y predicted as N,  TP=Y as Y, FP=actual N predicted as Y
		for (int i = 0; i < dia.size(); i++) {
			DataItem ac = dia.get(i);
			DataItem pred = dip.get(i);
			if(ac.getValue().equalsIgnoreCase("N")){ //if actual is N, it will be either a TN or FP(false alarm)
				if(pred.getValue().equalsIgnoreCase("N")){
					TN++;
				}else{
					FP++;
				}
			}else if(ac.getValue().equalsIgnoreCase("Y")){ //if actual is Y, it will be either a TP or FN(miss)
				if(pred.getValue().equalsIgnoreCase("Y")){
					TP++;
				}else{
					FN++;
				}
			}
		}
		int [] confusionMatrix = {TP,FN,FP,TN};
		return confusionMatrix;
	}

	public String getPredictionFile() {
		return predictionFile;
	}

	public void setPredictionFile(String predictionFile) {
		this.predictionFile = predictionFile;
	}

	public String getActualFile() {
		return actualFile;
	}

	public void setActualFile(String actualFile) {
		this.actualFile = actualFile;
	}

	public ArrayList<String> getPredictionLines() {
		return predictionLines;
	}

	public void setPredictionLines(ArrayList<String> predictionLines) {
		this.predictionLines = predictionLines;
	}

	public ArrayList<String> getActualLines() {
		return actualLines;
	}

	public void setActualLines(ArrayList<String> actualLines) {
		this.actualLines = actualLines;
	}

}
