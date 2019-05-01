package com.test.salesportal.dao.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import com.test.salesportal.dao.LoginCode;
import com.test.salesportal.dao.jpa.JPALoginDAO;
import com.test.salesportal.dao.jpa.JPANames;
import com.test.salesportal.model.login.LoginStatus;

import junit.framework.TestCase;

public class JPALoginDAOTest extends TestCase {

	public void testPhoneNoNotAdded() throws Exception {
		try (JPALoginDAO loginDAO = new JPALoginDAO(JPANames.PERSISTENCE_UNIT_DERBY)) {
		
			final LoginStatus status = loginDAO.getLoginStatus("12345");
			
			assertThat(status).isEqualTo(LoginStatus.UNKNOWN_PHONENO);
		}
	}

	public void testAddAndVerify() throws Exception {
		try (JPALoginDAO loginDAO = new JPALoginDAO(JPANames.PERSISTENCE_UNIT_DERBY)) {

			final String phoneNo = "12345";
			final LoginStatus status = loginDAO.getOrAddUser(phoneNo, LoginStatus.APPROVING);
			assertThat(status == LoginStatus.UNKNOWN_PHONENO);

			try {
				assertThat(loginDAO.getLoginStatus(phoneNo)).isEqualTo(LoginStatus.APPROVING);
				
				loginDAO.approveUser(phoneNo);
				
				assertThat(loginDAO.getLoginStatus(phoneNo)).isEqualTo(LoginStatus.APPROVED);
				
				final String code = "654321";
				final Date generatedTime = new Date(System.currentTimeMillis());
				
				loginDAO.storeCode(phoneNo, code, generatedTime);
				
				final LoginCode loginCode = loginDAO.getLoginStatusAndCode(phoneNo);
				
				assertThat(loginCode.getLoginStatus()).isEqualTo(LoginStatus.APPROVED);
				assertThat(loginCode.getCode()).isEqualTo(code);
				assertThat(loginCode.getTimeGenerated()).isEqualTo(generatedTime);
			}
			finally {
				loginDAO.deleteUser(phoneNo);
				
				assertThat(loginDAO.getLoginStatus(phoneNo)).isEqualTo(LoginStatus.UNKNOWN_PHONENO);
			}
		}
	}
}
