package rg;

public class Run {

	public static void main(String[] args) {
		String hypin = "/home/sachini/domains/R&G/hyps";
		String obsin = "/home/sachini/domains/R&G/obs";
		Hypotheses hyp = new Hypotheses();
		Observations obs = new Observations();
		hyp.readHyps(hypin);
		obs.readObs(obsin);
		System.out.println(hyp);
		System.out.println(obs);
	}

}
