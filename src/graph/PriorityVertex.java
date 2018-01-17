package graph;

public class PriorityVertex{
	private StateVertex vertex;
	private int vertexId;
	private int distance;
	
	public PriorityVertex(StateVertex v, int i, int d){
		this.vertex = v;
		this.vertexId = i;
		this.distance = d;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this){
			return true;
		}
		if(! (obj instanceof PriorityVertex) ){
			return false;
		}
		PriorityVertex pv = (PriorityVertex) obj;
		return pv.vertexId==this.vertexId && pv.vertex.isEqual(this.vertex);
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}
	
	public int getDistance(){
		return this.distance;
	}
	
	public StateVertex getVertex(){
		return this.vertex;
	}
	
	public int getVertexId(){
		return this.vertexId;
	}
	
	public void getVertexId(int i){
		this.vertexId = i;
	}
	
	public void setVertex(StateVertex v){
		this.vertex = v;
	}
	
	public String toString(){
		return this.vertex.getName()+" id:"+this.vertexId+" dis:"+this.distance;
	}
}