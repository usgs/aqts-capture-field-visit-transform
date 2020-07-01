package gov.usgs.wma.waterdata;

import java.util.ArrayList;
import java.util.List;

public class ResultObject {
	private String transformStatus;
	private List<String> fieldVisitIdentifiers;
	
	public List<String> getFieldVisitIdentifiers() {
		return null != fieldVisitIdentifiers ? fieldVisitIdentifiers : new ArrayList<String>();
	}
	public void setFieldVisitIdentifiers(List<String> fieldVisitIdentifiers) {
		this.fieldVisitIdentifiers = fieldVisitIdentifiers;
	}
	
	public String getTransformStatus() {
		return transformStatus;
	}
	public void setTransformStatus(String transformStatus) {
		this.transformStatus = transformStatus;
	}
}
