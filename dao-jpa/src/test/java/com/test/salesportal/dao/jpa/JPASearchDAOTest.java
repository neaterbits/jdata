package com.test.salesportal.dao.jpa;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.test.salesportal.dao.IItemUpdate;
import com.test.salesportal.dao.ISearchDAO;
import com.test.salesportal.dao.jpa.JPAItemDAO;
import com.test.salesportal.dao.jpa.JPANames;
import com.test.salesportal.dao.jpa.JPASearchDAO;
import com.test.salesportal.dao.test.SearchDAOTest;

public class JPASearchDAOTest extends SearchDAOTest {
	private final EntityManagerFactory emf = Persistence.createEntityManagerFactory(JPANames.PERSISTENCE_UNIT_DERBY);
	
	@Override
	protected IItemUpdate getItemDAO() {
		return new JPAItemDAO(emf);
	}

	@Override
	protected ISearchDAO getSearchDAO() {
		return new JPASearchDAO(emf, ITEM_TYPES);
	}
}
