package gov.usgs.wma.waterdata;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

@Component
public class FieldVisitDao {
	private static final Logger LOG = LoggerFactory.getLogger(FieldVisitDao.class);

	@Autowired
	protected JdbcTemplate jdbcTemplate;

	@Value("classpath:sql/insertDiscreteGroundWaterData.sql")
	private Resource insertDiscreteGroundWaterData;

	@Value("classpath:sql/deleteDiscreteGroundWaterData.sql")
	private Resource deleteDiscreteGroundWaterData;

	@Transactional
	public ResultObject doInsertDiscreteGroundWaterData(RequestObject request) {
		Map<String, Object> responseMap;

		responseMap =  jdbcTemplate.queryForMap(
				getSql(insertDiscreteGroundWaterData),
				request.getId(),
				request.getPartitionNumber(),
				request.getId(),
				request.getPartitionNumber());

		ResultObject result = new ResultObject();

		if (!responseMap.isEmpty()) {

			Object locationIdentifier = responseMap.get("location_identifier");
			Object monitoringLocationIdentifier = responseMap.get("monitoring_location_identifier");

			result.setLocationIdentifier(
					null == locationIdentifier ? null : String.valueOf(locationIdentifier)
			);

			result.setMonitoringLocationIdentifier(
					null == monitoringLocationIdentifier ? null : String.valueOf(monitoringLocationIdentifier)
			);

			result.setNumberGwLevelsInserted(Integer.parseInt(responseMap.get("records_inserted").toString()));
		} else {
			result.setNumberGwLevelsInserted(0);
		}
		return result;
	}

	@Transactional
	public int doDeleteDiscreteGroundWaterData(Long jsonDataId) {
		return jdbcTemplate.update(getSql(deleteDiscreteGroundWaterData), jsonDataId);
	}

	protected String getSql(Resource resource) {
		String sql = null;
		try {
			sql = new String(FileCopyUtils.copyToByteArray(resource.getInputStream()));
		} catch (IOException e) {
			LOG.error("Unable to get SQL statement", e);
			throw new RuntimeException(e);
		}
		return sql;
	}
}
