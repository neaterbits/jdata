package com.test.salesportal.dao.jpa;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.test.salesportal.common.UUIDGenerator;
import com.test.salesportal.dao.IOperationsDAO;
import com.test.salesportal.model.operations.Operation;

public class JPAOperationsDAO extends JPABaseDAO implements IOperationsDAO {

	public JPAOperationsDAO(String persistenceUnitName) {
		super(persistenceUnitName);
	}
	
	private static String generateAquireId() {
		return UUIDGenerator.generateUUID();
	}

	@Override
	public OperationStorageId startOperation(Operation operation) {

		final String aquireId = generateAquireId();
		
		operation.setAquireId(aquireId);
		operation.setStoreTime(new Date()); // TODO let database set this
		
		final long operationId = performInTransaction(() -> {
			entityManager.persist(operation);
			
			return operation.getId();
		});

		return new OperationStorageId(
				String.valueOf(operationId),
				aquireId);
	}

	@Override
	public Map<OperationStorageId, Operation> aquireNonCompletedOperations(int maxCount, long olderThanMillis) {
		
		return aquireNonCompletedOperationsImpl(maxCount, olderThanMillis, null);
	}

	@Override
	public Map<OperationStorageId, Operation> aquireNonCompletedOperations(int maxCount, long olderThanMillis, String userId) {
		
		return aquireNonCompletedOperationsImpl(maxCount, olderThanMillis, userId);
	}
	
	private Map<OperationStorageId, Operation> aquireNonCompletedOperationsImpl(
			int maxCount,
			long olderThanMillis,
			String userId) {
		
		// TODO JPQL does not support date arithmetics
		// so must use client side, which require clocks to be in sync
		
		
		final Date time = new Date(System.currentTimeMillis() - olderThanMillis);
		
		final TypedQuery<Operation> query = entityManager.createQuery(
					"from Operation operation"
				  + " where (operation.storeTime is null"
				  + "  or operation.storeTime < :time)"
				  + (userId != null ? " and operation.userId = :userId" : "")
				  + " order by operation.createdTime asc", Operation.class);
		
		
		query.setParameter("time", time);
		if (userId != null) {
			query.setParameter("userId", userId);
		}
		
		query.setMaxResults(maxCount);
		
		final List<Operation> operations = query.getResultList();
		
		// May result in less than maxCount
		// but means this is taken by other nodes or threads for reapplying

		final Map<OperationStorageId, Operation> toReapply = new HashMap<>(operations.size());

		if (!operations.isEmpty()) {
			performInTransaction(() -> {
				for (Operation operation : operations) {
					
					final Date currentTime = new Date();

					final String uuid = generateAquireId();

					final int updatedRows = entityManager.createQuery(
							"update Operation operation"
						  + " set operation.storeTime = :time,"
						  + "     operation.aquireId = :aquireId"
						  + " where operation.id = :id")
							
					.setParameter("time", currentTime)
					.setParameter("aquireId", uuid)
					.setParameter("id", operation.getId())
					.executeUpdate();
					
					if (updatedRows > 0) {
						
						final OperationStorageId operationStorageId = new OperationStorageId(
								String.valueOf(operation.getId()),
								uuid);
						
						toReapply.put(operationStorageId, operation);
					}
				}
				
				return null;
			});
		}
		
		return toReapply;
	}

	@Override
	public boolean completeOperation(OperationStorageId operationStorageId) {

		final boolean updated = performInTransaction(() -> {
			final int numUpdated = entityManager.createQuery(
					  "update Operation operation"
					+ " set operation.completed = TRUE,"
					+ "     operation.aquireId = NULL"
					+ " where operation.id = :id"
					+ "   and operation.aquireId = :aquireId")
			.setParameter("id", parseOperationId(operationStorageId.getOperationId()))
			.setParameter("aquireId", operationStorageId.getAquireId())
			.executeUpdate();
			
			return numUpdated != 0;
		});
		
		return updated;
	}
	
	
	
	@Override
	public boolean deleteOperationFromLog(String operationId) {
		return performInTransaction(() -> {
			final int numUpdated = entityManager.createQuery(
					  "delete from Operation operation"
					+ " where operation.id = :id")
			.setParameter("id", parseOperationId(operationId))
			.executeUpdate();
			
			return numUpdated != 0;
		});
	}

	@Override
	public List<Operation> getCompletedOperationsNewerThanSortedOnModelVersionAsc(long modelVersion) {
		return entityManager.createQuery(
				  "from Operation operation"
				+ " where operation.id > :modelVersion"
				+ "  and operation.completed = true"
				+ " order by operation.id asc", Operation.class)
				
				.setParameter("modelVersion", modelVersion)
				.getResultList();
	}
	
	long getNumOperations() {
		return entityManager.createQuery(" select count(operation.id) from Operation operation", Long.class)
				.getSingleResult();
	}
	
	// For testing
	Operation getOperation(String operationId) {
		
		final Operation operation = 
				// performInTransaction(() ->
				
				getSingleOrNull(entityManager.createQuery(
				  "from Operation operation"
				+ " where operation.id = :id", Operation.class)
				.setParameter("id", parseOperationId(operationId)));
				// .setHint(QueryHints.CACHE_USAGE, CacheUsage.DoNotCheckCache)

		if (operation != null) {
			// Avoid caching
			entityManager.refresh(operation);
		}

		return operation;
	}
	
	private static <T> T getSingleOrNull(Query query) {
		final T result;
		
		@SuppressWarnings("unchecked")
		final List<T> list = (List<T>)query.getResultList();
		
		switch (list.size()) {
		case 0:
			result = null;
			break;
			
		case 1:
			System.out.println("## row 0");
			result = list.get(0);
			
			break;
			
	
		default:
			throw new IllegalStateException();
		}
		
		return result;
	}

	private static long parseOperationId(String operationId) {
		return Long.parseLong(operationId);
	}
}
