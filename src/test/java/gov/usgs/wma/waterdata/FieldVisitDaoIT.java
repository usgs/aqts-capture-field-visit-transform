package gov.usgs.wma.waterdata;

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

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

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

	@DatabaseSetup("classpath:/testData/")
	@DatabaseSetup("classpath:/testResult/cleanseOutput/")
	@ExpectedDatabase(value="classpath:/testResult/happyPath/", assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	@Test
	public void doInsertDiscreteGroundWaterDataTest() {
		assertThat(fieldVisitDao.doInsertDiscreteGroundWaterData(TransformFieldVisitIT.JSON_DATA_ID_1), containsInAnyOrder(
				new FieldVisit("46686b86-77c8-4fef-8d72-a994a6a267a5"),
				new FieldVisit("e251791c-4c7f-4a7c-9480-997f2eeb0b94"),
				new FieldVisit("8BDA141822744BA5E0530100007FD075")
		));

		// Inserting the same data twice without deleting it first throws a duplicate key exception on the constraint
		assertThrows(DuplicateKeyException.class, () -> {
			fieldVisitDao.doInsertDiscreteGroundWaterData(TransformFieldVisitIT.JSON_DATA_ID_1);
		}, "should have thrown a duplicate key exception but did not");
	}

	@DatabaseSetup("classpath:/testResult/happyPath/")
	@ExpectedDatabase(value="classpath:/testResult/cleanseOutput/", assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	@Test
	public void doDeleteDiscreteGroundWaterDataTest() {
		fieldVisitDao.doDeleteDiscreteGroundWaterData(TransformFieldVisitIT.JSON_DATA_ID_1);
	}

	@DatabaseSetup("classpath:/testData/")
	@DatabaseSetup("classpath:/testResult/cleanseOutput/")
	@ExpectedDatabase(value="classpath:/testResult/cleanseOutput/", assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	@Test
	public void notFoundTest() {
		assertEquals(fieldVisitDao.doInsertDiscreteGroundWaterData(TransformFieldVisitIT.JSON_DATA_ID_2), Arrays.asList());
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
