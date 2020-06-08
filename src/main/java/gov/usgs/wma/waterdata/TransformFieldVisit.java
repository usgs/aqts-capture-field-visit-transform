package gov.usgs.wma.waterdata;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransformFieldVisit implements Function<RequestObject, ResultObject> {
	private static final Logger LOG = LoggerFactory.getLogger(TransformFieldVisit.class);

	private FieldVisitDao fieldVisitDao;

	public static final String FIELD_VISIT_DATA = "fieldVisitData";
	public static final String SUCCESS = "success";
	public static final String NO_RECORDS_FOUND = "no records found";
	public static final String REQUEST_TYPE_NOT_A_FIELD_VISIT = "request type was not a field visit";
	public static final String REQUEST_OR_TYPE_NULL = "request or type was null";

	@Autowired
	public TransformFieldVisit(FieldVisitDao fieldVisitDao) {
		this.fieldVisitDao = fieldVisitDao;
	}

	@Override
	public ResultObject apply(RequestObject request) {
		ResultObject result = processRequest(request);
		String transformStatus = result.getTransformStatus();
		if (SUCCESS.equalsIgnoreCase(transformStatus)) {
			return result;
		} else {
			throw new RuntimeException(transformStatus);
		}
	}

	protected ResultObject processRequest(RequestObject request) {
		ResultObject result = null;
		if (null != request && null != request.getType()) {
			LOG.debug("id {}, type {}", request.getId(), request.getType());
			result = processRequestType(request);
		} else {
			LOG.debug(REQUEST_OR_TYPE_NULL);
			result = badInput(REQUEST_OR_TYPE_NULL);
		}
		return result;
	}

	protected ResultObject processRequestType(RequestObject request) {
		ResultObject result = null;
		if (FIELD_VISIT_DATA.equalsIgnoreCase(request.getType())) {
			result = processFieldVisit(request);
		} else {
			// It's possible one could route the wrong type to this lambda via the state machine.
			LOG.debug(REQUEST_TYPE_NOT_A_FIELD_VISIT);
			result = badInput(REQUEST_TYPE_NOT_A_FIELD_VISIT);
		}
		return result;
	}

	protected ResultObject processFieldVisit(RequestObject request) {
		ResultObject result = new ResultObject();
		Integer recordsInserted = loadDiscreteGroundWaterIntoTransformDb(request);
		if (recordsInserted > 0) {
			result.setTransformStatus(SUCCESS);
		} else {
			// the query did not yield new records, nor did it throw a duplicate key exception
			result.setTransformStatus(NO_RECORDS_FOUND);
			LOG.debug(NO_RECORDS_FOUND);
		}
		result.setRecordsInserted(recordsInserted);
		return result;
	}

	@Transactional
	protected Integer loadDiscreteGroundWaterIntoTransformDb (RequestObject request) {
		Integer recordsInserted = 0;
		fieldVisitDao.doDeleteDiscreteGroundWaterData(request.getId());
		recordsInserted = fieldVisitDao.doInsertDiscreteGroundWaterData(request.getId());
		return recordsInserted;
	}

	protected ResultObject badInput(String errorReason) {
		ResultObject result = new ResultObject();
		result.setTransformStatus(errorReason);
		return result;
	}
}
