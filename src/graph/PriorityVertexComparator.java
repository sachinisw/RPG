package graph;

import java.util.Comparator;

public class PriorityVertexComparator implements Comparator<PriorityVertex>{
	@Override
	public int compare(PriorityVertex o1, PriorityVertex o2) {
		return o1.getDistance()-o2.getDistance();
	}
}
