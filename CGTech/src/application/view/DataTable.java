package application.view;

public class DataTable {
	private String id;
	private double fluteLength;
	private double volume;
	
	public DataTable(String id, double fluteLength, double volume) {
		super();
		this.id = id;
		this.fluteLength = fluteLength;
		this.volume = volume;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public double getFluteLength() {
		return fluteLength;
	}
	public void setFluteLength(double fluteLength) {
		this.fluteLength = fluteLength;
	}
	public double getVolume() {
		return volume;
	}
	public void setVolume(double volume) {
		this.volume = volume;
	}
	
	
}
