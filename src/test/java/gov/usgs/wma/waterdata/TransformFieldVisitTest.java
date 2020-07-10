package gov.usgs.wma.waterdata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;

@SpringBootTest(webEnvironment=WebEnvironment.NONE)
public class TransformFieldVisitTest {

	public static final String NOT_A_VALID_TYPE = "not a field visit request type";

	private TransformFieldVisit transformFieldVisit;
	private RequestObject request;
	@MockBean
	private FieldVisitDao fieldVisitDao;

	@BeforeEach
	public void beforeEach() {
		transformFieldVisit = new TransformFieldVisit(fieldVisitDao);
		request = new RequestObject();
		request.setId(TransformFieldVisitIT.JSON_DATA_ID_1);
		when(fieldVisitDao.doInsertDiscreteGroundWaterData(request))
				.thenReturn(Arrays.asList(
						new FieldVisit("46686b86-77c8-4fef-8d72-a994a6a267a5")
						,new FieldVisit("e251791c-4c7f-4a7c-9480-997f2eeb0b94")
						,new FieldVisit("8BDA141822744BA5E0530100007FD075")));
	}

	@Test
	public void processRequestInvalidRequestNullRequestTest() {
		ResultObject result = transformFieldVisit.processRequest(null);
		assertNotNull(result);
		assertEquals(TransformFieldVisit.REQUEST_OR_TYPE_NULL, result.getTransformStatus());

		assertThrows(RuntimeException.class, () -> {
			transformFieldVisit.apply(request);
		}, "should have thrown an exception but did not");
	}

	@Test
	public void processRequestInvalidRequestNullRequestTypeTest() {
		request.setType(null);
		ResultObject result = transformFieldVisit.processRequest(request);
		assertNotNull(result);
		assertEquals(TransformFieldVisit.REQUEST_OR_TYPE_NULL, result.getTransformStatus());

		assertThrows(RuntimeException.class, () -> {
			transformFieldVisit.apply(request);
		}, "should have thrown an exception but did not");
	}

	@Test
	public void processRequestInvalidRequestInvalidRequestTypeTest() {
		request.setType(NOT_A_VALID_TYPE);
		ResultObject result = transformFieldVisit.processRequest(request);
		assertNotNull(result);
		assertEquals(TransformFieldVisit.REQUEST_TYPE_NOT_A_FIELD_VISIT, result.getTransformStatus());

		assertThrows(RuntimeException.class, () -> {
			transformFieldVisit.apply(request);
		}, "should have thrown an exception but did not");
	}

	@Test
	public void processFieldVisitTest() {
		request.setId(TransformFieldVisitIT.JSON_DATA_ID_1);
		request.setType(TransformFieldVisit.FIELD_VISIT_DATA);
		ResultObject result = transformFieldVisit.processRequest(request);
		assertNotNull(result);
		assertEquals(TransformFieldVisit.SUCCESS, result.getTransformStatus());
		verify(fieldVisitDao).doInsertDiscreteGroundWaterData(request);
	}

	@Test
	public void notFoundTest() {
		request.setId(TransformFieldVisitIT.JSON_DATA_ID_2);
		request.setType(TransformFieldVisit.FIELD_VISIT_DATA);
		ResultObject result = transformFieldVisit.processRequest(request);
		assertNotNull(result);
		assertEquals(TransformFieldVisit.SUCCESS, result.getTransformStatus());
		verify(fieldVisitDao).doInsertDiscreteGroundWaterData(request);
	}
}
