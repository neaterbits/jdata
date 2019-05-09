package com.test.salesportal.dao;

import java.util.List;

import com.test.salesportal.model.operations.Operation;

public interface IOperationsRetrieval {

	/**
	 * Return all completed operations that can be applied to local cache.
	 * 
	 * @param modelVersion model version to get operations newer than.
	 * 
	 * @return list of completed operations in ascending order
	 */
	
	List<Operation> getCompletedOperationsNewerThanSortedOnModelVersionAsc(long modelVersion);

}
