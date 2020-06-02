package gov.usgs.wma.waterdata;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransformFieldVisit implements Function<RequestObject, ResultObject> {
	private static final Logger LOG = LoggerFactory.getLogger(TransformFieldVisit.class);

	private FieldVisitDao fieldVisitDao;

	public static final String FIELD_VISIT_DATA = "fieldVisitData";
	public static final String BAD_INPUT = "badInput";
	public static final String SUCCESS = "success";

	@Autowired
	public TransformFieldVisit(FieldVisitDao fieldVisitDao) {
		this.fieldVisitDao = fieldVisitDao;
	}

	@Override
	public ResultObject apply(RequestObject request) {
		ResultObject result = processRequest(request);
		String transformStatus = result.getTransformStatus();
		if (SUCCESS != transformStatus) {
			throw new RuntimeException(transformStatus);
		} else {
			return result;
		}
	}

	protected ResultObject processRequest(RequestObject request) {
		if (null != request && null != request.getType()) {
			LOG.debug("id {}, type {}", request.getId(), request.getType());
			if (FIELD_VISIT_DATA.equals(request.getType())) {
				return processFieldVisit(request);
			} else {
				// It's possible one could route the wrong type to this lambda via the state machine.
				LOG.debug("request type was not a field visit");
				return badInput(request);
			}
		} else {
			LOG.debug("request or type was null");
			return badInput(request);
		}
	}

	protected ResultObject processFieldVisit(RequestObject request) {
		ResultObject result = new ResultObject();
		Integer recordsInserted = fieldVisitDao.doInsertDiscreteGroundWaterData(request.getId());
		result.setRecordsInserted(recordsInserted);
		result.setTransformStatus(SUCCESS);
		return result;
	}

	protected ResultObject badInput(RequestObject request) {
		ResultObject result = new ResultObject();
		result.setTransformStatus(BAD_INPUT);
		return result;
	}

}

