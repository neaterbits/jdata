package com.test.salesportal.dao.jpa;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.test.salesportal.dao.IOperationsDAO.OperationStorageId;
import com.test.salesportal.model.operations.Operation;

import static org.assertj.core.api.Assertions.assertThat;

import junit.framework.TestCase;

public class JPAOperationsDAOTest extends TestCase {

	private static final String USER_ID = "test123";
	
	public void testStoreAndComplete() throws Exception {
		
		try (JPAOperationsDAO dao = new JPAOperationsDAO(JPANames.PERSISTENCE_UNIT_DERBY)) {

			final byte [] item = "theItem".getBytes();
			
			final Date createTime = new Date();
			
			final Operation operation = new Operation(createTime, item, USER_ID);
			
			final OperationStorageId operationStorageId = dao.startOperation(operation);

			assertThat(operationStorageId).isNotNull();
			
			final Operation readBackOperation = dao.getOperation(operationStorageId.getOperationId());
			
			assertThat(readBackOperation).isNotNull();
			assertThat(readBackOperation.getId()).isEqualTo(Long.parseLong(operationStorageId.getOperationId()));
			assertThat(readBackOperation.getAquireId()).isEqualTo(operationStorageId.getAquireId());
			assertThat(readBackOperation.getCreatedTime()).isEqualTo(createTime);
			assertThat(readBackOperation.getUserId()).isEqualTo(USER_ID);
			
			assertThat(readBackOperation.getData()).isEqualTo(item);
			
			final Map<OperationStorageId, Operation> nonCompleted = dao.aquireNonCompletedOperations(
					1000,
					System.currentTimeMillis() - createTime.getTime());
			assertThat(nonCompleted.isEmpty());

			final boolean operationFound = dao.completeOperation(operationStorageId);
			
			assertThat(operationFound).isTrue();
			
			assertThat(dao.getOperation(operationStorageId.getOperationId())).isNull();
		}
	}

	public void testGetNoneCompleteForOneUser() throws Exception {
		
		try (JPAOperationsDAO dao = new JPAOperationsDAO(JPANames.PERSISTENCE_UNIT_DERBY)) {

			final byte [] item = "theItem".getBytes();
			final Date createTime = new Date();
			final Operation operation = new Operation(createTime, item, USER_ID);
			final OperationStorageId operationStorageId = dao.startOperation(operation);
			assertThat(operationStorageId).isNotNull();

			final byte [] anotherItem = "anotherItem".getBytes();
			final Date anotherCreateTime = new Date();
			final Operation anotherOperation = new Operation(anotherCreateTime, anotherItem, "anotherUser");
			final OperationStorageId anotherOperationStorageId = dao.startOperation(anotherOperation);
			assertThat(anotherOperationStorageId).isNotNull();
			
			final Map<OperationStorageId, Operation> nonCompleted = dao.aquireNonCompletedOperations(
					1000,
					0L,
					"anotherUser");
			
			assertThat(nonCompleted.size()).isEqualTo(1);

			final Map.Entry<OperationStorageId, Operation> nonCompleteEntry = nonCompleted.entrySet().iterator().next();
			
			assertThat(nonCompleteEntry.getKey().getOperationId()).isEqualTo(anotherOperationStorageId.getOperationId());
			assertThat(nonCompleteEntry.getKey().getAquireId()).isNotEqualTo(anotherOperationStorageId.getAquireId());

			assertThat(nonCompleteEntry.getKey().getOperationId()).isEqualTo(anotherOperationStorageId.getOperationId());
			assertThat(nonCompleteEntry.getKey().getOperationId()).isNotEqualTo(operationStorageId.getOperationId());
			assertThat(dao.getOperation(nonCompleteEntry.getKey().getOperationId()).getAquireId())
				.isNotEqualTo(anotherOperationStorageId.getAquireId());

			boolean operationFound = dao.completeOperation(anotherOperationStorageId);
			assertThat(operationFound).isFalse(); // aquired by other

			assertThat(dao.getOperation(anotherOperationStorageId.getOperationId())).isNotNull();
			
			System.out.println("## prev aquired " + anotherOperationStorageId.getAquireId());
			
			// Complete by last that has aquired
			operationFound = dao.completeOperation(nonCompleteEntry.getKey());
			assertThat(operationFound).isTrue();
			
			final Operation completed = dao.getOperation(nonCompleteEntry.getKey().getOperationId());
			assertThat(completed.getAquireId()).isNull();
			assertThat(completed.isCompleted()).isTrue();
			
			dao.deleteOperationFromLog(anotherOperationStorageId.getOperationId());
			assertThat(dao.getOperation(anotherOperationStorageId.getOperationId())).isNull();
		}
	}
	
	public void testGetCompletedOperationsNewerThanSortedOnModelVersionAsc() throws Exception {
		
		try (JPAOperationsDAO dao = new JPAOperationsDAO(JPANames.PERSISTENCE_UNIT_DERBY)) {

			final byte [] item = "theItem".getBytes();
			final Date createTime = new Date();
			final Operation operation = new Operation(createTime, item, USER_ID);
			final OperationStorageId operationStorageId = dao.startOperation(operation);
			assertThat(operationStorageId).isNotNull();
			
			assertThat(dao.getCompletedOperationsNewerThanSortedOnModelVersionAsc(-1L)).isEmpty();
			
			dao.completeOperation(operationStorageId);

			List<Operation> newerOperations = dao.getCompletedOperationsNewerThanSortedOnModelVersionAsc(-1L);
			assertThat(newerOperations.size()).isEqualTo(1);
			assertThat(newerOperations.get(0).getId()).isEqualTo(operation.getId());
			
			final byte [] anotherItem = "anotherItem".getBytes();
			final Date anotherCreateTime = new Date();
			final Operation anotherOperation = new Operation(anotherCreateTime, anotherItem, "anotherUser");
			final OperationStorageId anotherOperationStorageId = dao.startOperation(anotherOperation);
			assertThat(anotherOperationStorageId).isNotNull();
			
			assertThat(dao.getCompletedOperationsNewerThanSortedOnModelVersionAsc(operation.getId())).isEmpty();
			
			dao.completeOperation(anotherOperationStorageId);

			newerOperations = dao.getCompletedOperationsNewerThanSortedOnModelVersionAsc(operation.getId());
			assertThat(newerOperations.size()).isEqualTo(1);
			assertThat(newerOperations.get(0).getId()).isEqualTo(anotherOperation.getId());
		}
	}
}
