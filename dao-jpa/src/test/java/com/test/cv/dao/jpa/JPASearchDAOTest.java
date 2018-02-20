package com.test.cv.dao.jpa;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.test.cv.dao.IItemDAO;
import com.test.cv.dao.ISearchDAO;
import com.test.cv.dao.test.SearchDAOTest;

public class JPASearchDAOTest extends SearchDAOTest {
	private final EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpa-derby");
	
	@Override
	protected IItemDAO getItemDAO() {
		return new JPAItemDAO(emf);
	}

	@Override
	protected ISearchDAO getSearchDAO() {
		return new JPASearchDAO(emf);
	}
}
