package com.test.salesportal.dao.xml;

import com.test.salesportal.dao.LockDAO;
import com.test.salesportal.xmlstorage.api.LockProvider;

public class DAOLockProvider implements LockProvider {

	private final LockDAO lockDAO;
	
	private static class LockObj implements Lock {
		private final Object daoLockObj;

		LockObj(Object daoLockObj) {
			if (daoLockObj == null) {
				throw new IllegalArgumentException("daoLockObj == null");
			}

			this.daoLockObj = daoLockObj;
		}
	}

	public DAOLockProvider(LockDAO lockDAO) {
		
		if (lockDAO == null) {
			throw new IllegalArgumentException("lockDAO == null");
		}
		
		this.lockDAO = lockDAO;
	}

	@Override
	public void createLock(String userId, String itemId) throws LockException {
		lockDAO.createLock(itemId);
	}

	@Override
	public Lock obtainLock(String userId, String itemId) throws LockException {
		return new LockObj(lockDAO.lock(itemId));
	}

	@Override
	public void releaseLock(Lock lock) {
		lockDAO.relaseLock(((LockObj)lock).daoLockObj);
	}

	@Override
	public void deleteLock(String userId, String itemId) throws LockException {
		lockDAO.deleteLock(itemId);
	}
}
