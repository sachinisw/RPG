package predictor;

public class AccuracyCalculator {

	public static void main(String args[]){
		String pfile = "/home/sachini/domains/EASYIPC/scenarios/TEST0/inst2/data/predictions";
		String afile = "/home/sachini/domains/EASYIPC/scenarios/TEST0/inst2/data/instfull.arff";
		Prediction pred = new Prediction(pfile, afile);
		pred.readFiles();
		int[] cmat = pred.generateConfusionMatrix();
		System.out.println("TP="+cmat[0]);
		System.out.println("FN="+cmat[1]);
		System.out.println("FP="+cmat[2]);
		System.out.println("TN="+cmat[3]);
		System.out.println("sum="+cmat[0]+cmat[1]+cmat[2]+cmat[3]);
		double TPR = (double)cmat[0]/(double)(cmat[0]+cmat[1]);
		double TNR = (double)cmat[3]/(double)(cmat[3]+cmat[2]);
		double FPR = (double)cmat[2]/(double)(cmat[3]+cmat[2]);
		double FNR = (double)cmat[1]/(double)(cmat[1]+cmat[0]);
		System.out.println("TPR="+TPR);
		System.out.println("TNR="+TNR);
		System.out.println("FPR="+FPR);
		System.out.println("FNR="+FNR);
	}
}
