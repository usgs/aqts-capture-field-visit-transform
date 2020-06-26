package gov.usgs.wma.waterdata;

import java.util.ArrayList;
import java.util.List;

public class ResultObject {
	private String transformStatus;
	private List<FieldVisit> fieldVisitList;
	
	public List<FieldVisit> getFieldVisitList() {
		return null != fieldVisitList ? fieldVisitList : new ArrayList<>();
	}
	public void setFieldVisitList(List<FieldVisit> fieldVisitList) {
		this.fieldVisitList = fieldVisitList;
	}
	
	public String getTransformStatus() {
		return transformStatus;
	}
	public void setTransformStatus(String transformStatus) {
		this.transformStatus = transformStatus;
	}
}
