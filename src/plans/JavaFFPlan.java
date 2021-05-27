package plans;


public class JavaFFPlan extends Plan{
	private static final int UNIT_COST = 1;
	
	public JavaFFPlan() {
		super();
	}
	
	public int getPlanCost() {
		return getActions().size()*UNIT_COST; //assumes unit cost
	}
	
}
