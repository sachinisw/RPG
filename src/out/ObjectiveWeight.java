package out;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

public class ObjectiveWeight {

	private String weightfile;
	private ArrayList<WeightGroup> weights;

	public ObjectiveWeight(String filename){
		this.weightfile = filename;
		this.weights = new ArrayList<WeightGroup>();
	}

	public void assignWeights(){
		LineIterator it = null;
		try {
			it = FileUtils.lineIterator(new File(weightfile), "UTF-8");
			while (it.hasNext()) {
		        String line = it.nextLine();
		        WeightGroup wg = new WeightGroup(Double.valueOf(line.split(",")[0]), Double.valueOf(line.split(",")[1]), Double.valueOf(line.split(",")[2]));
		        weights.add(wg);
		    }
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		    LineIterator.closeQuietly(it);
		}
	}

	public ArrayList<WeightGroup> getWeights(){
		return weights;
	}
	
	public void setWeights(ArrayList<WeightGroup> weights) {
		this.weights = weights;
	}
}
