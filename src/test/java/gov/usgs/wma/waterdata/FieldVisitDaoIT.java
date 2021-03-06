package gov.usgs.wma.waterdata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileUrlResource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(webEnvironment=WebEnvironment.NONE,
		classes={DBTestConfig.class, FieldVisitDao.class})
@ActiveProfiles("it")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class,
		TransactionalTestExecutionListener.class,
		TransactionDbUnitTestExecutionListener.class })
@DbUnitConfiguration(dataSetLoader=FileSensingDataSetLoader.class)
@AutoConfigureTestDatabase(replace=Replace.NONE)
@Transactional(propagation=Propagation.NOT_SUPPORTED)
@Import({DBTestConfig.class})
@DirtiesContext
public class FieldVisitDaoIT {

	@Autowired
	private FieldVisitDao fieldVisitDao;
	private RequestObject request;

	@BeforeEach
	public void beforeEach() {
		request = new RequestObject();
		request.setId(TransformFieldVisitIT.JSON_DATA_ID_1);
		request.setPartitionNumber(TransformFieldVisitIT.PARTITION_NUMBER);
	}

	@DatabaseSetup("classpath:/testData/")
	@DatabaseSetup("classpath:/testResult/cleanseOutput/")
	@ExpectedDatabase(value="classpath:/testResult/happyPath/", assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	@Test
	public void doInsertDiscreteGroundWaterDataTest() {
		ResultObject result = fieldVisitDao.doInsertDiscreteGroundWaterData(request);
		assertEquals(TransformFieldVisitIT.LOCATION_IDENTIFIER_1, result.getLocationIdentifier());
		assertEquals(TransformFieldVisitIT.MONITORING_LOCATION_IDENTIFIER_1, result.getMonitoringLocationIdentifier());
		assertEquals(12, result.getNumberGwLevelsInserted());

        // Inserting the same data twice without deleting it first throws a duplicate key exception on the constraint
		assertThrows(DuplicateKeyException.class, () -> {
			fieldVisitDao.doInsertDiscreteGroundWaterData(request);
		}, "should have thrown a duplicate key exception but did not");
	}

	@DatabaseSetup("classpath:/testData/")
	@DatabaseSetup("classpath:/testResult/happyPath/")
	@ExpectedDatabase(value="classpath:/testResult/newRowsInserted/", assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	@Test
	public void doInsertDiscreteGroundWaterDataNewRowsTest() {
		request.setId(TransformFieldVisitIT.JSON_DATA_ID_4);
		ResultObject result = fieldVisitDao.doInsertDiscreteGroundWaterData(request);
		assertEquals(TransformFieldVisitIT.LOCATION_IDENTIFIER_2, result.getLocationIdentifier());
		assertEquals(TransformFieldVisitIT.MONITORING_LOCATION_IDENTIFIER_2, result.getMonitoringLocationIdentifier());
		assertEquals(12, result.getNumberGwLevelsInserted());

		// Inserting the same data twice without deleting it first throws a duplicate key exception on the constraint
		assertThrows(DuplicateKeyException.class, () -> {
			fieldVisitDao.doInsertDiscreteGroundWaterData(request);
		}, "should have thrown a duplicate key exception but did not");
	}

	@DatabaseSetup("classpath:/testData/")
	@DatabaseSetup("classpath:/testResult/cleanseOutput/")
	@ExpectedDatabase(value="classpath:/testResult/twoVisitTimes/", assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	@Test
	public void doInsertDiscreteGroundWaterDataTwoVisitTimesTest() {
		request.setId((long) 34046611);
		request.setPartitionNumber(11);
		ResultObject result = fieldVisitDao.doInsertDiscreteGroundWaterData(request);
		assertEquals(TransformFieldVisitIT.LOCATION_IDENTIFIER_TWO_VISIT_TIMES, result.getLocationIdentifier());
		assertEquals(TransformFieldVisitIT.MONITORING_LOCATION_IDENTIFIER_TWO_VISIT_TIMES, result.getMonitoringLocationIdentifier());
		assertEquals(43, result.getNumberGwLevelsInserted());

		// Inserting the same data twice without deleting it first throws a duplicate key exception on the constraint
		assertThrows(DuplicateKeyException.class, () -> {
			fieldVisitDao.doInsertDiscreteGroundWaterData(request);
		}, "should have thrown a duplicate key exception but did not");
	}

	@DatabaseSetup("classpath:/testResult/happyPath/")
	@ExpectedDatabase(value="classpath:/testResult/cleanseOutput/", assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	@Test
	public void doDeleteDiscreteGroundWaterDataTest() {
		fieldVisitDao.doDeleteDiscreteGroundWaterData(request);
	}

	@DatabaseSetup("classpath:/testResult/newRowsInserted/")
	@ExpectedDatabase(value="classpath:/testResult/happyPath/", assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	@Test
	public void doDeleteDiscreteGroundWaterDataWithDifferentIdTest() {
		// make sure we only delete rows associated with the json_data_id being tested, leaving the rest of the data
		// in place
		request.setId(TransformFieldVisitIT.JSON_DATA_ID_4);
		fieldVisitDao.doDeleteDiscreteGroundWaterData(request);
	}

	@DatabaseSetup("classpath:/testData/")
	@DatabaseSetup("classpath:/testResult/cleanseOutput/")
	@ExpectedDatabase(value="classpath:/testResult/cleanseOutput/", assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	@Test
	public void notFoundTest() {
		request.setId(TransformFieldVisitIT.JSON_DATA_ID_2);
		ResultObject result = fieldVisitDao.doInsertDiscreteGroundWaterData(request);
		assertEquals(0, result.getNumberGwLevelsInserted());
		assertNull(result.getLocationIdentifier());
		assertNull(result.getMonitoringLocationIdentifier());
	}

	@Test
	public void badResource() {
		try {
			fieldVisitDao.getSql(new FileUrlResource("classpath:sql/missing.sql"));
			fail("Should have gotten a RuntimeException");
		} catch (Exception e) {
			assertTrue(e instanceof RuntimeException);
			assertEquals("java.io.FileNotFoundException: classpath:sql/missing.sql (No such file or directory)",
					e.getMessage());
		}
	}
}
