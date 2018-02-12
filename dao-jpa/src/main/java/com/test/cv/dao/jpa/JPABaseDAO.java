package com.test.cv.dao.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public abstract class JPABaseDAO implements AutoCloseable {

	private final EntityManagerFactory entityManagerFactory;
	final EntityManager entityManager;

	JPABaseDAO(String persistenceUnitName) {
		this.entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnitName);
		this.entityManager = entityManagerFactory.createEntityManager();
	}

	@Override
	public void close() throws Exception {
		entityManager.close();
		entityManagerFactory.close();
	}
}
