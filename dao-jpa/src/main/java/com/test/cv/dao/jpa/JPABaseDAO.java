package com.test.cv.dao.jpa;

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
}
