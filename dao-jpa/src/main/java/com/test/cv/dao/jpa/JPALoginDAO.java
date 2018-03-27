package com.test.cv.dao.jpa;

import java.util.Date;
import java.util.function.Supplier;

import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;

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
		
		User user = null;
		
		try {
			user = entityManager.createQuery("from User user where user.phoneNo = :phoneNo", User.class)
				.setParameter("phoneNo", phoneNo)
				.getSingleResult();
		}
		catch (NoResultException ex) {
			
		}

		return user;
	}

	private <R> R performInTransaction(Supplier<R> s) {
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
	
	@Override
	public LoginStatus getOrAddUser(String phoneNo, LoginStatus initialStatus) {

		return performInTransaction(() -> {
			final LoginStatus status;
			
			User user = getUser(phoneNo);

			if (user == null) {
				user = new User();
				
				user.setStatus(initialStatus);
				user.setPhoneNo(phoneNo);

				entityManager.persist(user);
				
				status = LoginStatus.UNKNOWN_PHONENO;
			}
			else {
				status = user.getStatus();
			}

			return status;
		});
	}

	@Override
	public void deleteUser(String phoneNo) {
		performInTransaction(() -> {
			entityManager.createQuery("delete from User user where user.phoneNo = :phoneNo")
				.setParameter("phoneNo", phoneNo)
				.executeUpdate();
			
			return null;
		});
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
		
		performInTransaction(() -> {
			entityManager.persist(user);
			
			return null;
		});
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

		
		performInTransaction(() -> {
			final User user = getUser(phoneNo);

			user.setCode(code);
			user.setCodeGeneratedTime(generatedTime);

			entityManager.persist(user);

			return null;
		});
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
