package metrics;

import java.util.Objects;

public class CausalLink implements Comparable<CausalLink>{
	private String proposition;
	private String acAdding;
	private String acNeeding;

	public CausalLink(String p, String ap, String ac) {
		proposition = p;
		acNeeding = ac;
		acAdding = ap;
	}

	public boolean equals(Object cl) {
		if (cl == null) {
			return false;
		}
		if (!CausalLink.class.isAssignableFrom(cl.getClass())) {
			return false;
		}
		final CausalLink other = (CausalLink) cl;
		if(compareProposition(other) && 
				acAdding.equalsIgnoreCase(other.getActionAdding()) && acNeeding.equalsIgnoreCase(other.getActionNeeding())) {
			return true;
		}
		return false;
	}

	public boolean compareProposition(CausalLink in) {
		String thisparts [ ]= proposition.split(",");
		String inparts [] = in.getProposition().split(",");
		if(thisparts.length==inparts.length) {
			for (String s : inparts) {
				boolean found = false;
				for (String t : thisparts) {
					if(s.equalsIgnoreCase(t)) {
						found = true;
						break;
					}
				}
				if(!found) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
		
	public String toString() {
		return acAdding + " ->"+ proposition +" - " + acNeeding;
	}
	public String getProposition() {
		return proposition;
	}

	public void setProposition(String proposition) {
		this.proposition = proposition;
	}

	public String getActionAdding() {
		return acAdding;
	}

	public void setActionAdding(String actionProducing) {
		this.acAdding = actionProducing;
	}

	public String getActionNeeding() {
		return acNeeding;
	}

	public void setActionNeeding(String actionConsuming) {
		this.acNeeding = actionConsuming;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(proposition, acAdding, acNeeding);
	}

	@Override
	public int compareTo(CausalLink o) {
		return o.hashCode()-hashCode();
	}
}
