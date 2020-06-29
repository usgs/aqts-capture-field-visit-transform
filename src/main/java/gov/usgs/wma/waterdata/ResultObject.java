package gov.usgs.wma.waterdata;

import java.util.ArrayList;
import java.util.List;

public class ResultObject {
	private String transformStatus;
	private List<FieldVisit> fieldVisitIdentifiers;
	
	public List<FieldVisit> getFieldVisitIdentifiers() {
		return null != fieldVisitIdentifiers ? fieldVisitIdentifiers : new ArrayList<>();
	}
	public void setFieldVisitIdentifiers(List<FieldVisit> fieldVisitIdentifiers) {
		this.fieldVisitIdentifiers = fieldVisitIdentifiers;
	}
	
	public String getTransformStatus() {
		return transformStatus;
	}
	public void setTransformStatus(String transformStatus) {
		this.transformStatus = transformStatus;
	}
}
