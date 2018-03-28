package com.test.cv.dao.jpa;

import java.util.Map;
import java.util.function.Supplier;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public abstract class JPABaseDAO implements AutoCloseable {

	private final EntityManagerFactory entityManagerFactory;
	final EntityManager entityManager;
	private final boolean openedInConstructor;

	JPABaseDAO(String persistenceUnitName) {
		this(Persistence.createEntityManagerFactory(persistenceUnitName), true);
	}

	JPABaseDAO(String persistenceUnitName, Map<String, String> properties) {
		this(Persistence.createEntityManagerFactory(persistenceUnitName, properties), true);
	}
	
	JPABaseDAO(EntityManagerFactory entityManagerFactory) {
		this(entityManagerFactory, false);
	}
	
	private JPABaseDAO(EntityManagerFactory entityManagerFactory, boolean openedInConstructor) {
		this.entityManagerFactory = entityManagerFactory;
		this.entityManager = entityManagerFactory.createEntityManager();
		this.openedInConstructor = openedInConstructor;
	}


	@Override
	public void close() throws Exception {
		entityManager.close();
		
		if (openedInConstructor) {
			entityManagerFactory.close();
		}
	}

	final <R> R performInTransaction(Supplier<R> s) {
		final R result;
		entityManager.getTransaction().begin();
		
		boolean ok = false;
		try {
			result = s.get();
			entityManager.getTransaction().commit();
			ok = true;
		}
		finally {
			if (!ok) {
				if (entityManager.getTransaction().isActive()) {
					entityManager.getTransaction().rollback();
				}
			}
		}
		
		return result;
	}
}
