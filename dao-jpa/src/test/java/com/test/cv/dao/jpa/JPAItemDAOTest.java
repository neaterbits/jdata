package com.test.cv.dao.jpa;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.test.cv.dao.IItemDAO;
import com.test.cv.dao.test.ItemDAOTest;

public class JPAItemDAOTest extends ItemDAOTest {

	private final EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpa-derby");
	
	@Override
	protected IItemDAO getItemDAO() {
		return new JPAItemDAO(emf);
	}
}
