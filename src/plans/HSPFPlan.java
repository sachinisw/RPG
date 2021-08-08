package plans;

public class HSPFPlan extends Plan{
	
	public HSPFPlan() {
		super();
	}
	
	public int getPlanCost() {
		return this.getActions().size(); //assumes unit cost
	}
	
}
