package rg;

//STRIPS action definition in a domain model
public class Action implements Cloneable{
	private String header;
	private AcParameters params;
	private AcPrecondition preconds;
	private AcEffect effects;
	
	public Action(String name) {
		header = "(:action " + name+ "\n";
		params = new AcParameters();
		preconds = new AcPrecondition();
		effects = new AcEffect();
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		Action clone = null;
		try{
			clone = (Action) super.clone();//Call clone() for each object type. String is immutable. so copy's header can be changed.
			clone.setParams((AcParameters) params.clone());
			clone.setPreconds((AcPrecondition) preconds.clone());
			clone.setEffects((AcEffect) effects.clone());
		}catch (CloneNotSupportedException e){
			throw new RuntimeException(e);
		}
		return clone;
	}
	
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (!Action.class.isAssignableFrom(o.getClass())) {
			return false;
		}
		final Action other = (Action) o;
		return (other.header.equalsIgnoreCase(header));
	}
	
	public String toString() {
		return header + params.toString() + preconds.toString()
		+ effects.toString() +")\n";
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public AcParameters getParams() {
		return params;
	}

	public void setParams(AcParameters params) {
		this.params = params;
	}

	public AcPrecondition getPreconds() {
		return preconds;
	}

	public void setPreconds(AcPrecondition preconds) {
		this.preconds = preconds;
	}

	public AcEffect getEffects() {
		return effects;
	}
	
	public void setEffects(AcEffect effects) {
		this.effects = effects;
	}
}
