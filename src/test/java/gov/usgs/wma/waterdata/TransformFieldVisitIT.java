package gov.usgs.wma.waterdata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

	public static final Integer DISCRETE_GROUND_WATER_ROWS_INSERTED = 9;
	public static final Integer DISCRETE_GROUND_WATER_NO_ROWS_INSERTED = 0;
	public static final Long JSON_DATA_ID_1 = 1L;
	public static final Long JSON_DATA_ID_2 = 2L;

	@BeforeEach
	public void beforeEach() {
		request = new RequestObject();
	}

	@DatabaseSetup("classpath:/testData/")
	@DatabaseSetup("classpath:/testResult/cleanseOutput/")
	@ExpectedDatabase(value="classpath:/testResult/happyPath/", assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	@Test
	public void processFieldVisitDataTest() {
		request.setId(JSON_DATA_ID_1);
		request.setType(TransformFieldVisit.FIELD_VISIT_DATA);
		ResultObject result = transformFieldVisit.processFieldVisit(request);
		assertNotNull(result);
		assertEquals(TransformFieldVisit.SUCCESS, result.getTransformStatus());
		assertEquals(DISCRETE_GROUND_WATER_ROWS_INSERTED, result.getRecordsInserted());

		// Processing the same data twice should not throw an exception, the old rows will be replaced.
		transformFieldVisit.processFieldVisit(request);
	}

	@DatabaseSetup("classpath:/testData/")
	@DatabaseSetup("classpath:/testResult/cleanseOutput/")
	@ExpectedDatabase(value="classpath:/testResult/cleanseOutput/", assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	@Test
	public void notFoundTest() {
		request.setId(JSON_DATA_ID_2);
		request.setType(TransformFieldVisit.FIELD_VISIT_DATA);
		ResultObject result = transformFieldVisit.processFieldVisit(request);
		assertNotNull(result);
		assertEquals(TransformFieldVisit.NO_RECORDS_FOUND, result.getTransformStatus());
		assertEquals(DISCRETE_GROUND_WATER_NO_ROWS_INSERTED, result.getRecordsInserted());
	}
}
