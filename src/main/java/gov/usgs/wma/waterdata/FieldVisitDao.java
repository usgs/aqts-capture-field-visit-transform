package gov.usgs.wma.waterdata;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.dao.EmptyResultDataAccessException;
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
	public List<FieldVisit> doInsertDiscreteGroundWaterData(RequestObject request) {
		List<FieldVisit> fieldVisitList = Arrays.asList();
		try {
			fieldVisitList =  jdbcTemplate.query(
					getSql(insertDiscreteGroundWaterData),
					new FieldVisitRowMapper(),
					request.getId(),
					request.getPartitionNumber());
		} catch (EmptyResultDataAccessException e) {
			LOG.info("Couldn't find id: {} partition number: {} - {} ", request.getId(), request.getPartitionNumber(), e.getLocalizedMessage());
		}
		return fieldVisitList;
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
