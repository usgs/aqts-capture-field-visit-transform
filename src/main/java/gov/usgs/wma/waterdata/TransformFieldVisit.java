package gov.usgs.wma.waterdata;

import java.util.ArrayList;
import java.util.List;
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
			LOG.debug("the result object: {}", result.getFieldVisitIdentifiers().toString());
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
		ResultObject result = new ResultObject();
		List<FieldVisit> fieldVisitList = loadDiscreteGroundWaterIntoTransformDb(request);
		if (fieldVisitList.size() > 0) {
			result.setTransformStatus(SUCCESS);
		} else {
			// No groundwater levels for this site visit, proceed without error
			result.setTransformStatus(SUCCESS);
			LOG.debug(NO_GROUND_WATER_LEVELS_FOUND);
		}
		List<String> identifiers = new ArrayList<>();
		for (FieldVisit fv : fieldVisitList) {
			identifiers.add(fv.getFieldVisitIdentifier());
		}
		result.setFieldVisitIdentifiers(identifiers);
		return result;
	}

	@Transactional
	protected List<FieldVisit> loadDiscreteGroundWaterIntoTransformDb (RequestObject request) {
		fieldVisitDao.doDeleteDiscreteGroundWaterData(request.getId());
		return fieldVisitDao.doInsertDiscreteGroundWaterData(request);
	}

	protected ResultObject badInput(String errorReason) {
		ResultObject result = new ResultObject();
		result.setTransformStatus(errorReason);
		return result;
	}
}
