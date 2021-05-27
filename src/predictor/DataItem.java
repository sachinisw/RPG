package predictor;

public class DataItem {
	private int id;
	private String value;

	public DataItem(int i, String v){
		this.id = i;
		this.value = v;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return this.id + "," + this.value;
	}
}