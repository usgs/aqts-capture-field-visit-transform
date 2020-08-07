package gov.usgs.wma.waterdata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
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

@SpringBootTest(webEnvironment=WebEnvironment.NONE,
		classes={DBTestConfig.class, FieldVisitDao.class, TransformFieldVisit.class})
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
public class TransformFieldVisitIT {

	@Autowired
	private TransformFieldVisit transformFieldVisit;
	private RequestObject request;

	public static final Long JSON_DATA_ID_1 = 1L;
	public static final Long JSON_DATA_ID_2 = 2L;
	public static final Long JSON_DATA_ID_3 = 3L;
	public static final Integer PARTITION_NUMBER = 7;
	public static final String LOCATION_IDENTIFIER_1 = "393215104490001";
	public static final String MONITORING_LOCATION_IDENTIFIER_1 = "USGS-393215104490001";

	@BeforeEach
	public void beforeEach() {
		request = new RequestObject();
		request.setId(JSON_DATA_ID_1);
		request.setType(TransformFieldVisit.FIELD_VISIT_DATA);
		request.setPartitionNumber(TransformFieldVisitIT.PARTITION_NUMBER);
	}

	@DatabaseSetup("classpath:/testData/")
	@DatabaseSetup("classpath:/testResult/cleanseOutput/")
	@ExpectedDatabase(value="classpath:/testResult/happyPath/", assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	@Test
	public void processFieldVisitDataNewDataTest() {
		ResultObject result = transformFieldVisit.processFieldVisit(request);
		assertNotNull(result);
		assertEquals(TransformFieldVisit.SUCCESS, result.getTransformStatus());
		assertEquals(LOCATION_IDENTIFIER_1, result.getLocationIdentifier());
		assertEquals(TransformFieldVisitIT.MONITORING_LOCATION_IDENTIFIER_1, result.getMonitoringLocationIdentifier());
		assertEquals(3, result.getNumberGwLevelsInserted());

		// Processing the same data twice should not throw an exception
		// the old rows will be replaced, this is a delete + add
		transformFieldVisit.processFieldVisit(request);
	}

	@DatabaseSetup("classpath:/testData/")
	@DatabaseSetup("classpath:/testResult/happyPath/")
	@ExpectedDatabase(value="classpath:/testResult/happyPath/", assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	@Test
	public void processFieldVisitDataOldDataToBeReplacedTest() {
		ResultObject result = transformFieldVisit.processFieldVisit(request);
		assertNotNull(result);
		assertEquals(TransformFieldVisit.SUCCESS, result.getTransformStatus());
		assertEquals(LOCATION_IDENTIFIER_1, result.getLocationIdentifier());
		assertEquals(TransformFieldVisitIT.MONITORING_LOCATION_IDENTIFIER_1, result.getMonitoringLocationIdentifier());
		assertEquals(3, result.getNumberGwLevelsInserted());
	}

	@DatabaseSetup("classpath:/testData/")
	@DatabaseSetup("classpath:/testResult/cleanseOutput/")
	@ExpectedDatabase(value="classpath:/testResult/cleanseOutput/", assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	@Test
	public void notFoundTest() {
		request.setId(JSON_DATA_ID_2);
		ResultObject result = transformFieldVisit.processFieldVisit(request);
		assertNotNull(result);
		assertEquals(TransformFieldVisit.SUCCESS, result.getTransformStatus());
		assertNull(result.getLocationIdentifier());
		assertNull(result.getMonitoringLocationIdentifier());
		assertEquals(0, result.getNumberGwLevelsInserted());
	}

	@DatabaseSetup("classpath:/testData/")
	@DatabaseSetup("classpath:/testResult/cleanseOutput/")
	@ExpectedDatabase(value="classpath:/testResult/cleanseOutput/", assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	@Test
	public void noGroundWaterLevelsFoundTest() {
		request.setId(JSON_DATA_ID_3);
		ResultObject result = transformFieldVisit.processFieldVisit(request);
		assertNotNull(result);
		assertEquals(TransformFieldVisit.SUCCESS, result.getTransformStatus());
		// we should almost always be able to return the location identifier.
		assertEquals("123456789",result.getLocationIdentifier());
		assertEquals("USGS-123456789", result.getMonitoringLocationIdentifier());
		assertEquals(0, result.getNumberGwLevelsInserted());
	}
}
