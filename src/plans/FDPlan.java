package plans;


public class FDPlan extends Plan{
	private static final int UNIT_COST = 1;
	
	public FDPlan() {
		super();
	}
	
	public int getPlanCost() {
		return getActions().size()*UNIT_COST; //assumes unit cost
	}
	
}
