package classifiers;

import java.util.logging.Level;
import java.util.logging.Logger;

import run.RunML;
import run.TestConfigsML;
import weka.classifiers.Classifier;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;

public class DecisionTree {

	private String trainedModel;
	private static final Logger LOGGER = Logger.getLogger(RunML.class.getName());

	public DecisionTree(final String mode) {
		if(mode.equalsIgnoreCase(TestConfigsML.graph_features)) {
			trainedModel = TestConfigsML.prefix_models+"ml_dt_att_selected.model";
		}else if(mode.equalsIgnoreCase(TestConfigsML.topk_features)) {
			trainedModel = TestConfigsML.prefix_models+"tk_dt_att_selected.model";
		}
	}

	public static void main(final String[] args) {
		//0	3	2	0	1	2	7	7	1	0.48	9
		final String[] features = {"3", "0", "2", "0", "1", "2", "7", "7", "1", "0.48", "9", "?"};
		String model_path = "";
		final DecisionTree reg = new DecisionTree(model_path);
		reg.predict(features);
	}

	public String predict(final String[] featurevals) {
		Object[] modelOb = null;
		try {        //read the model object file for the logistic regression classifier
			modelOb = SerializationHelper.readAll(trainedModel); //returned value has 2 elements (0-classifier, 1-attribute definitions
		} catch (final Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage() );
		}
		final Classifier cl = (Classifier) modelOb[0];
		final Instances header = (Instances) modelOb[1];
		final Instances test = createInstances(header, featurevals);
		test.setClassIndex(test.numAttributes() - 1);

		//predict class
		String prediction = "";
		for (int i = 0; i < test.numInstances(); i++) {
			try {
				final double pred = cl.classifyInstance(test.instance(i)); //gives the predicted index of the class attribute array {Y,N}
				prediction = test.classAttribute().value((int) pred);
			} catch (final Exception e) {
				prediction = e.getMessage();
			}
		}
		return prediction;
	}

	public Instances createInstances(Instances header, String[] input) {
		Instances copy = new Instances(header); //in weka instance refers to a line of data.
		final Instance row = new DenseInstance(input.length); //creates an array of (?) values
		for (int i = 0; i < input.length - 1; i++) { //fill all but last column (class column) with values
			row.setValue(header.attribute(i), Double.parseDouble(input[i]));
		}
		copy.add(row);
		return copy;
	}
}
