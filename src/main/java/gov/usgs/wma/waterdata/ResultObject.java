package gov.usgs.wma.waterdata;

public class ResultObject {
	private String transformStatus;
	private Integer recordsInsertedOrUpdated;
	public String getTransformStatus() {
		return transformStatus;
	}
	public void setTransformStatus(String transformStatus) {
		this.transformStatus = transformStatus;
	}
	public Integer getRecordsInsertedOrUpdated() {
		return recordsInsertedOrUpdated;
	}
	public void setRecordsInsertedOrUpdated(Integer recordsInsertedOrUpdated) {
		this.recordsInsertedOrUpdated = recordsInsertedOrUpdated;
	}
}
