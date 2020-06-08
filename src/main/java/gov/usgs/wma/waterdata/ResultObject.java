package gov.usgs.wma.waterdata;

public class ResultObject {
	private String transformStatus;
	private Integer recordsInserted;
	public String getTransformStatus() {
		return transformStatus;
	}
	public void setTransformStatus(String transformStatus) {
		this.transformStatus = transformStatus;
	}
	public Integer getRecordsInserted() {
		return recordsInserted;
	}
	public void setRecordsInserted(Integer recordsInserted) {
		this.recordsInserted = recordsInserted;
	}
}
