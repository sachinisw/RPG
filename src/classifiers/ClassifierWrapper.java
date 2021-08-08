package classifiers;

import run.TestConfigsML;

public class ClassifierWrapper {
	
	public static String getPrediction(String featureType, String classifierType, String[] featureset) {
		String predicted_class = "";
		if(featureType.equalsIgnoreCase(TestConfigsML.graph_features)) {
			if(classifierType.equalsIgnoreCase(TestConfigsML.decisionTree)) {
				DecisionTree dt = new DecisionTree(TestConfigsML.graph_features);
				predicted_class = dt.predict(featureset);
			}else if(classifierType.equalsIgnoreCase(TestConfigsML.knn)) {
				Knn knn = new Knn(TestConfigsML.graph_features);
				predicted_class = knn.predict(featureset);
			}else if(classifierType.equalsIgnoreCase(TestConfigsML.regression)) {
				Regression reg = new Regression(TestConfigsML.graph_features);
				predicted_class = reg.predict(featureset);
			}else if(classifierType.equalsIgnoreCase(TestConfigsML.naiveBayes)) {
				NaiveBayes nb = new NaiveBayes(TestConfigsML.graph_features);
				predicted_class = nb.predict(featureset);
			}
		}else if(featureType.equalsIgnoreCase(TestConfigsML.topk_features)) {
			if(classifierType.equalsIgnoreCase(TestConfigsML.decisionTree)) {
				DecisionTree dt = new DecisionTree(TestConfigsML.topk_features);
				predicted_class = dt.predict(featureset);
			}else if(classifierType.equalsIgnoreCase(TestConfigsML.knn)) {
				Knn knn = new Knn(TestConfigsML.topk_features);
				predicted_class = knn.predict(featureset);
			}else if(classifierType.equalsIgnoreCase(TestConfigsML.regression)) {
				Regression reg = new Regression(TestConfigsML.topk_features);
				predicted_class = reg.predict(featureset);
			}else if(classifierType.equalsIgnoreCase(TestConfigsML.naiveBayes)) {
				NaiveBayes nb = new NaiveBayes(TestConfigsML.topk_features);
				predicted_class = nb.predict(featureset);
			}
		}
		
		return predicted_class;
	}

}
