package plans;

public class SASPlan extends Plan{
	private static final int UNIT_COST = 1;

	public SASPlan() {
		super();
	}
	
	@Override
	public int getPlanCost() {
		return getActions().size()*UNIT_COST;
	}
}
