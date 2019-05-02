package com.test.salesportal.dao;

import java.util.Map;

import com.test.salesportal.model.operations.Operation;

/**
 * DAO interface for journal of operations performed that are transactional,
 * eg. uploading information to S3 and indexing.
 * 
 * Storage operations should be idempotent so that one can redo operations
 * from the journal in case of shutdown.
 */
public interface IOperationsDAO {

	public static class OperationStorageId {

		/**
		 * Unique identifier for the operation in persistent store
		 */
		private final String operationId;
		
		/**
		 * Unique identifier for storage operation so that
		 * in a failover scenario one can check that no other node is also re-attempting this operation.
		 */
		
		private final String aquireId;
		
		public OperationStorageId(String operationId, String aquireId) {

			if (operationId == null) {
				throw new IllegalArgumentException("operationId == null");
			}
			
			if (aquireId == null) {
				throw new IllegalArgumentException("aquireId == null");
			}
			
			this.operationId = operationId;
			this.aquireId = aquireId;
		}

		public String getOperationId() {
			return operationId;
		}

		public String getAquireId() {
			return aquireId;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((aquireId == null) ? 0 : aquireId.hashCode());
			result = prime * result + ((operationId == null) ? 0 : operationId.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			OperationStorageId other = (OperationStorageId) obj;
			if (aquireId == null) {
				if (other.aquireId != null)
					return false;
			} else if (!aquireId.equals(other.aquireId))
				return false;
			if (operationId == null) {
				if (other.operationId != null)
					return false;
			} else if (!operationId.equals(other.operationId))
				return false;
			return true;
		}
	}
	
	/**
	 * Start an operation, store it so can know this operation is not complete yet.
	 * This for later cross-node completion if operation has failed.
	 * 
	 * @param operation
	 * 
	 * @return ID for the operation
	 */
	
	OperationStorageId startOperation(Operation operation);
	
	/**
	 * Aquire a set of non-completed operations (eg. from node restart/crash) for execution
	 * 
	 * @param maxCount
	 * 
	 * @return map of operations to be reapplied
	 */

	default Map<OperationStorageId, Operation> aquireNonCompletedOperations(int maxCount) {
		return aquireNonCompletedOperations(maxCount, 5 * 60 * 1000L);
	}
	
	Map<OperationStorageId, Operation> aquireNonCompletedOperations(int maxCount, long olderThanMillis);

	Map<OperationStorageId, Operation> aquireNonCompletedOperations(int maxCount, long olderThanMillis, String userId);

	/**
	 * Complete initial or non-completed operation re-application.
	 * 
	 * @param operationStorageId obtained OperationStorageId
	 */
	boolean completeOperation(OperationStorageId operationStorageId);
}
