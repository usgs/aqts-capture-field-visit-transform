package gov.usgs.wma.waterdata;

public class ResultObject {
	private String transformStatus;
	private String locationIdentifier;
	private String monitoringLocationIdentifier;
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
	public String getMonitoringLocationIdentifier() {
		return monitoringLocationIdentifier;
	}
	public void setMonitoringLocationIdentifier(String monitoringLocationIdentifier) {
		this.monitoringLocationIdentifier = monitoringLocationIdentifier;
	}
	public int getNumberGwLevelsInserted() {
		return numberGwLevelsInserted;
	}
	public void setNumberGwLevelsInserted(int numberGwLevelsInserted) {
		this.numberGwLevelsInserted = numberGwLevelsInserted;
	}
}
