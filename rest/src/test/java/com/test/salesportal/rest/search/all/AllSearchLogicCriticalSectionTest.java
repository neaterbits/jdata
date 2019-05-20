package com.test.salesportal.rest.search.all;

import java.util.Collections;
import java.util.List;

import org.mockito.Mockito;

import com.test.salesportal.common.UUIDGenerator;
import com.test.salesportal.dao.IOperationsRetrieval;
import com.test.salesportal.dao.ISearchCursor;
import com.test.salesportal.dao.ISearchDAO;
import com.test.salesportal.dao.SearchException;
import com.test.salesportal.model.items.ItemAttribute;
import com.test.salesportal.model.operations.Operation;
import com.test.salesportal.model.items.sports.Snowboard;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests critical section of cache access
 */
public class AllSearchLogicCriticalSectionTest extends BaseAllSearchLogicTest {

	private static class OperationsRetrievalImpl implements IOperationsRetrieval {
		private int numberOfInvocations;

		@Override
		public synchronized List<Operation> getCompletedOperationsNewerThanSortedOnModelVersionAsc(long modelVersion) {

			++ numberOfInvocations;
			
			return Collections.emptyList();
		}
		
		synchronized int getNumberOfInvocations() {
			return numberOfInvocations;
		}
	}
	
	private static class CriticalSectionCallbackImpl implements ITestCriticalSectionCallback {
		
		private int numberOfStartedInvocations;
		private int numberOfContinuedInvocations;
		private boolean blockNextInvocation;
		
		@Override
		public synchronized void invoke() {
			++ numberOfStartedInvocations;
			
			if (blockNextInvocation) {
				try {
					this.wait();
				} catch (InterruptedException ex) {
					throw new IllegalStateException(ex);
				}
			}
			
			this.blockNextInvocation = false;
			
			++ numberOfContinuedInvocations;
		}
		
		synchronized void blockNextInvocation() {
			if (blockNextInvocation) {
				throw new IllegalStateException();
			}
			
			this.blockNextInvocation = true;
		}
		
		synchronized void continueInvocation() {
			this.notify();
		}
		
		synchronized int getNumberOfStartedInvocations() {
			return numberOfStartedInvocations;
		}

		synchronized int getNumberOfContinuedInvocations() {
			return numberOfContinuedInvocations;
		}
	}
	
	public void testDBAccessIsOutsideOfLocking() throws SearchException, InterruptedException {

		final OperationsRetrievalImpl operationsRetrieval = new OperationsRetrievalImpl();
		final CriticalSectionCallbackImpl criticalSectionCallback = new CriticalSectionCallbackImpl();
		
		final AllSearchLogic logic = new AllSearchLogic(operationsRetrieval, OPERATION_DATA_MARSHALLER, ITEM_TYPES, criticalSectionCallback);

		final ItemAttribute modelVersionAttribute = snowboardType.getAttributes().getByName("modelVersion");
		final ItemAttribute makeAttribute = snowboardType.getAttributes().getByName("make");
		
		final ISearchDAO searchDAO = Mockito.mock(ISearchDAO.class);
		final ISearchCursor searchCursor = Mockito.mock(ISearchCursor.class);

		final Snowboard snowboard = new Snowboard();

		snowboard.setModelVersion(0L);
		snowboard.setIdString(UUIDGenerator.generateUUID());
		snowboard.setTitle("Snowboard title");
		snowboard.setMake("Burton");
		snowboard.setProductionYear(2015);
		
		makeMockitoStubsForSearchResult(searchDAO, searchCursor, snowboard, modelVersionAttribute, makeAttribute);
		
		AllSearchResult result = searchSnowboard(logic, searchDAO, false);
		
		assertThat(result).isNotNull();

		// Perform in critical section
		assertThat(criticalSectionCallback.getNumberOfStartedInvocations()).isEqualTo(1);
		assertThat(criticalSectionCallback.getNumberOfContinuedInvocations()).isEqualTo(1);
		
		final Runnable runnable = () -> {
			searchSnowboard(logic, searchDAO, false);
		};
		
		final Thread thread = new Thread(runnable);

		criticalSectionCallback.blockNextInvocation();

		assertThat(operationsRetrieval.getNumberOfInvocations()).isEqualTo(0);

		// Search in cache
		thread.start();
		Thread.sleep(100);
		
		assertThat(operationsRetrieval.getNumberOfInvocations()).isEqualTo(1);
		
		assertThat(criticalSectionCallback.getNumberOfStartedInvocations()).isEqualTo(2);
		assertThat(criticalSectionCallback.getNumberOfContinuedInvocations()).isEqualTo(1);

		final Thread anotherThread = new Thread(runnable);
		
		anotherThread.start();

		Thread.sleep(100);
		
		// Thread should access DB outside of locking
		assertThat(operationsRetrieval.getNumberOfInvocations()).isEqualTo(2);
		
		// anotherThread should wait on synchronization so no update to counters
		assertThat(criticalSectionCallback.getNumberOfStartedInvocations()).isEqualTo(2);
		assertThat(criticalSectionCallback.getNumberOfContinuedInvocations()).isEqualTo(1);

		// Allow thread to continue invocation
		criticalSectionCallback.continueInvocation();

		thread.join();
		
		anotherThread.join();

		// Both threads completed
		assertThat(criticalSectionCallback.getNumberOfStartedInvocations()).isEqualTo(3);
		assertThat(criticalSectionCallback.getNumberOfContinuedInvocations()).isEqualTo(3);
	}

	public void testDoesNotReCacheOperations() {
		// If caching via two threads, make sure second thread does not reapply operations to cache
		assertThat(true).isFalse();
	}
}
