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
	public static final String NO_GROUND_WATER_LEVELS_FOUND = "no ground water levels for this id";
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
			LOG.debug("the result object location id: {} and number of rows inserted: {}",
					result.getLocationIdentifier(),
					result.getNumberGwLevelsInserted());
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
			// It's possible one could route the wrong type of data to this lambda via the state machine.
			LOG.debug(REQUEST_TYPE_NOT_A_FIELD_VISIT);
			result = badInput(REQUEST_TYPE_NOT_A_FIELD_VISIT);
		}
		return result;
	}

	protected ResultObject processFieldVisit(RequestObject request) {
		ResultObject result = loadDiscreteGroundWaterIntoTransformDb(request);
		result.setTransformStatus(SUCCESS);
		if (result.getNumberGwLevelsInserted() == 0) {
			// No groundwater levels for this site visit, proceed without error
			LOG.debug(NO_GROUND_WATER_LEVELS_FOUND);
		}
		return result;
	}

	@Transactional
	protected ResultObject loadDiscreteGroundWaterIntoTransformDb (RequestObject request) {
		// first delete existing records for a location from the discrete_ground_water table
		fieldVisitDao.doDeleteDiscreteGroundWaterData(request.getId());

		// next insert new/updated/existing records for a location into discrete_ground_water table
		return fieldVisitDao.doInsertDiscreteGroundWaterData(request);
	}

	protected ResultObject badInput(String errorReason) {
		ResultObject result = new ResultObject();
		result.setTransformStatus(errorReason);
		return result;
	}
}
