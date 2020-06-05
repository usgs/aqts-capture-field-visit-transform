package gov.usgs.wma.waterdata;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileUrlResource;
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

	@DatabaseSetup("classpath:/testData/staticData/")
	@DatabaseSetup("classpath:/testResult/cleanseOutput/")
	@ExpectedDatabase(value="classpath:/testResult/happyPath/", assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	@Test
	public void doUpsertDiscreteGroundWaterDataTest() {
		assertEquals(
				TransformFieldVisitIT.DISCRETE_GROUND_WATER_ROWS_INSERTED,
				fieldVisitDao.doUpsertDiscreteGroundWaterData(TransformFieldVisitIT.JSON_DATA_ID_1));

		// Upserting the same data twice should not throw an exception, but the data will not be upserted
		// unless the last_modified date is more current than what we already have in the destination table
		fieldVisitDao.doUpsertDiscreteGroundWaterData(TransformFieldVisitIT.JSON_DATA_ID_1);
	}

	@DatabaseSetup("classpath:/testData/dataToBeUpdated/")
	@ExpectedDatabase(value="classpath:/testResult/happyPath/", assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	@Test
	public void doUpsertDiscreteGroundWaterDataWithUpdatesTest() {
		assertEquals(
				TransformFieldVisitIT.DISCRETE_GROUND_WATER_ROWS_UPSERTED,
				fieldVisitDao.doUpsertDiscreteGroundWaterData(TransformFieldVisitIT.JSON_DATA_ID_1));
	}

	@DatabaseSetup("classpath:/testData/staticData/")
	@DatabaseSetup("classpath:/testResult/cleanseOutput/")
	@ExpectedDatabase(value="classpath:/testResult/cleanseOutput/", assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	@Test
	public void notFoundTest() {
		assertEquals(
				TransformFieldVisitIT.DISCRETE_GROUND_WATER_NO_ROWS_INSERTED,
				fieldVisitDao.doUpsertDiscreteGroundWaterData(TransformFieldVisitIT.JSON_DATA_ID_2));
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
