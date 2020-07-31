package gov.usgs.wma.waterdata;

public class ResultObject {
	private String transformStatus;
	private String locationIdentifier;
	private int numberGwLevelsInserted;
	
	public String getTransformStatus() {
		return transformStatus;
	}
	public void setTransformStatus(String transformStatus) {
		this.transformStatus = transformStatus;
	}
	public String getLocationIdentifier() {
		return locationIdentifier;
	}
	public void setLocationIdentifier(String locationIdentifier) {
		this.locationIdentifier = locationIdentifier;
	}
	public int getNumberGwLevelsInserted() {
		return numberGwLevelsInserted;
	}
	public void setNumberGwLevelsInserted(int numberGwLevelsInserted) {
		this.numberGwLevelsInserted = numberGwLevelsInserted;
	}
}
