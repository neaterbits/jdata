package com.test.cv.dao.jpa;

import java.util.Date;

import javax.persistence.EntityManagerFactory;

import com.test.cv.dao.LoginCode;
import com.test.cv.dao.LoginDAO;
import com.test.cv.model.login.LoginStatus;
import com.test.cv.model.login.User;

public class JPALoginDAO extends JPABaseDAO implements LoginDAO {

	public JPALoginDAO(EntityManagerFactory entityManagerFactory) {
		super(entityManagerFactory);
	}

	public JPALoginDAO(String persistenceUnitName) {
		super(persistenceUnitName);
	}
	
	private User getUser(String phoneNo) {
		return entityManager.find(User.class, phoneNo);
	}

	
	@Override
	public LoginStatus getOrAddUser(String phoneNo) {

		final LoginStatus status;
		entityManager.getTransaction().begin();
		
		boolean ok = false;
		try {
			User user = getUser(phoneNo);

			if (user == null) {
				user = new User();
				
				user.setPhoneNo(phoneNo);

				entityManager.persist(user);
				
				status = LoginStatus.UNKNOWN_PHONENO;
			}
			else {
				status = user.getStatus();
			}
			entityManager.getTransaction().commit();
			ok = true;
		}
		finally {
			if (!ok) {
				entityManager.getTransaction().rollback();
			}
		}
		return status;
	}

	@Override
	public LoginStatus getLoginStatus(String phoneNo) {
		
		final User user = getUser(phoneNo);
		
		final LoginStatus status;
		
		if (user == null) {
			status = LoginStatus.UNKNOWN_PHONENO;
		}
		else {
			status = user.getStatus();
		}
		
		return status;
	}
	

	@Override
	public void updateLoginStatus(String phoneNo, LoginStatus status) {
		final User user = getUser(phoneNo);

		user.setStatus(status);
		
		entityManager.getTransaction().begin();
		
		boolean ok = false;
		try {
			entityManager.persist(user);
			entityManager.getTransaction().commit();
			ok = true;
		}
		finally {
			if (!ok) {
				entityManager.getTransaction().rollback();
			}
		}
	}

	@Override
	public void storeCode(String phoneNo, String code, Date generatedTime) {

		if (code == null) {
			throw new IllegalArgumentException("code == null");
		}

		if (code.isEmpty()) {
			throw new IllegalArgumentException("code is empty");
		}

		if (generatedTime == null) {
			throw new IllegalArgumentException("generatedTime == null");
		}

		entityManager.getTransaction().begin();
		
		boolean ok = false;
		try {
			final User user = getUser(phoneNo);

			user.setCode(code);
			user.setCodeGeneratedTime(generatedTime);

			entityManager.persist(user);
			entityManager.getTransaction().commit();
			ok = true;
		}
		finally {
			if (!ok) {
				entityManager.getTransaction().rollback();
			}
		}
	}

	@Override
	public LoginCode getLoginStatusAndCode(String phoneNo) {
		
		final User user = getUser(phoneNo);
		
		final LoginCode code;
		
		if (user == null || user.getCode() == null) {
			code = null;
		}
		else {
			code = new LoginCode(user.getStatus(), user.getCode(), user.getCodeGeneratedTime());
		}

		return code;
	}
}
