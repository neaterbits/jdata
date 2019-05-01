package com.test.salesportal.dao.jpa;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.test.salesportal.dao.IItemDAO;
import com.test.salesportal.dao.jpa.JPAItemDAO;
import com.test.salesportal.dao.jpa.JPANames;
import com.test.salesportal.dao.test.ItemDAOTest;

public class JPAItemDAOTest extends ItemDAOTest {

	private final EntityManagerFactory emf = Persistence.createEntityManagerFactory(JPANames.PERSISTENCE_UNIT_DERBY);
	
	@Override
	protected IItemDAO getItemDAO() {
		return new JPAItemDAO(emf);
	}
}
