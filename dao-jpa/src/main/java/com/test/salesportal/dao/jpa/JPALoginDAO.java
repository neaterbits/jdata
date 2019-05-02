package com.test.salesportal.dao.jpa;

import java.util.Date;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;

import com.test.salesportal.dao.LoginCode;
import com.test.salesportal.dao.LoginDAO;
import com.test.salesportal.model.login.LoginStatus;
import com.test.salesportal.model.login.LoginUser;

public class JPALoginDAO extends JPABaseDAO implements LoginDAO {

	public JPALoginDAO(EntityManagerFactory entityManagerFactory) {
		super(entityManagerFactory);
	}

	public JPALoginDAO(String persistenceUnitName) {
		super(persistenceUnitName);
	}
	
	public JPALoginDAO(String persistenceUnitName, Map<String, String> properties) {
		super(persistenceUnitName, properties);
	}

	private LoginUser getUser(String phoneNo) {
		
		LoginUser user = null;
		
		try {
			user = entityManager.createQuery("from LoginUser user where user.phoneNo = :phoneNo", LoginUser.class)
				.setParameter("phoneNo", phoneNo)
				.getSingleResult();
		}
		catch (NoResultException ex) {
			
		}

		return user;
	}
	
	@Override
	public LoginStatus getOrAddUser(String phoneNo, LoginStatus initialStatus) {

		return performInTransaction(() -> {
			final LoginStatus status;
			
			LoginUser user = getUser(phoneNo);

			if (user == null) {
				user = new LoginUser();
				
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
			entityManager.createQuery("delete from LoginUser user where user.phoneNo = :phoneNo")
				.setParameter("phoneNo", phoneNo)
				.executeUpdate();
			
			return null;
		});
	}

	@Override
	public LoginStatus getLoginStatus(String phoneNo) {
		
		final LoginUser user = getUser(phoneNo);
		
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
		final LoginUser user = getUser(phoneNo);

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
			final LoginUser user = getUser(phoneNo);

			user.setCode(code);
			user.setCodeGeneratedTime(generatedTime);

			entityManager.persist(user);

			return null;
		});
	}

	@Override
	public LoginCode getLoginStatusAndCode(String phoneNo) {
		
		final LoginUser user = getUser(phoneNo);
		
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
