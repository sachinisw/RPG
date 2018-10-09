package graph;

public class Layer {
	private StateVertex vertexFrom;
	private int levelNumber;
	
	public Layer (StateVertex p, int l){
		this.vertexFrom = p;
		this.levelNumber = l;
	}
	
	public StateVertex getVertexFrom() {
		return vertexFrom;
	}
	public void setVertexFrom(StateVertex parent) {
		this.vertexFrom = parent;
	}

	public int getLevelNumber() {
		return levelNumber;
	}

	public void setLevelNumber(int levelNumber) {
		this.levelNumber = levelNumber;
	}
	
	public String toString(){
		return "level="+this.levelNumber; //returns the level number only. parent toString() would be too large to put in the node circle in DOT
	}
	
}
