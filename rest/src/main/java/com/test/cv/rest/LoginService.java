package com.test.cv.rest;

import java.security.SecureRandom;
import java.util.Date;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.test.cv.dao.LoginCode;
import com.test.cv.dao.LoginDAO;
import com.test.cv.dao.jpa.JPALoginDAO;
import com.test.cv.model.login.CodeStatus;
import com.test.cv.model.login.LoginStatus;
import com.test.cv.notifications.aws.AWSSMSSender;

@Path("/login")
public class LoginService {

	private static final SecureRandom randomGenerator = new SecureRandom();
	
	public static class LoginResponse {
		private LoginStatus status;

		public LoginStatus getStatus() {
			return status;
		}

		public void setStatus(LoginStatus status) {
			this.status = status;
		}
	}

	public static class CheckCodeResponse {
		private CodeStatus status;

		public CodeStatus getStatus() {
			return status;
		}

		public void setStatus(CodeStatus status) {
			this.status = status;
		}
	}

	private LoginDAO getDAO() {
		return new JPALoginDAO("sdsd");
	}
	
	@Path("checkphoneno")
	@Produces("application/json")
	@POST
	public LoginResponse checkPhoneNo(@QueryParam("phoneNo") String phoneNo) {
		
		final LoginDAO loginDAO = getDAO();
		
		final LoginStatus loginStatus = loginDAO.getOrAddUser(phoneNo);
		
		switch (loginStatus) {
		case APPROVED:
			// Web page will request code
			sendCodeToUser(phoneNo, loginDAO);
			break;
			
		case APPROVING:
			// Just show update in webpage
			break;
			
		case UNKNOWN_PHONENO:
			// Send notification that phone number must be approved
			sendApproveNotification(phoneNo);
			break;
		}
		
		final LoginResponse response = new LoginResponse();
		
		response.setStatus(loginStatus);
		
		return response;
	}
	
	
	private void sendCodeToUser(String phoneNo, LoginDAO loginDAO) {
		// Check login-status that this is a logged in user in case someone tries to invoke
		// web service directly

		if (loginDAO.getLoginStatus(phoneNo) != LoginStatus.APPROVED) {
			throw new IllegalStateException("Phone number not approved");
		}

		final String code = generateCode();
		
		// Store code in DB for later
		loginDAO.storeCode(phoneNo, code, new Date(System.currentTimeMillis()));

		sendSMS(phoneNo, "Her er din innloggins-kode: " + code);
	}
	

	// Send whenever user presses button to send a code
	@Path("sendcode")
	@POST
	@Produces("application/json")
	public CheckCodeResponse checkCode(@QueryParam("phoneNo") String phoneNo, @QueryParam("code") String code) {
		// Compare code to what is in database
		
		final LoginDAO loginDAO = getDAO();
		
		final LoginCode loginCode = loginDAO.getLoginStatusAndCode(phoneNo);
		
		// Double-check approval status in case calling web service directly
		if (loginCode.getLoginStatus() != LoginStatus.APPROVED) {
			throw new IllegalStateException("Not approved");
		}

		
		final CodeStatus status;
		if (code.equals(loginCode.getCode())) {
			if (System.currentTimeMillis() - loginCode.getTimeGenerated().getTime() >= (1000 * 60 * 15)) {
				status = CodeStatus.EXPIRED;
			}
			else {
				status = CodeStatus.VERIFIED;
			}
		}
		else {
			status = CodeStatus.NONMATCHING;
		}
		
		final CheckCodeResponse response = new CheckCodeResponse();

		response.setStatus(status);

		return response;
	}
	

	private String generateCode() {
		final int code = randomGenerator.nextInt(100000);
		
		return String.format("%06d", code);
	}
	
	private void sendApproveNotification(String phoneNo) {
		
		final String approvalPhoneNo = System.getenv("ELTODO_APPROVAL_NOTIFICATION_PHONENUMBER");

		if (approvalPhoneNo == null || approvalPhoneNo.trim().isEmpty()) {
			throw new IllegalStateException("No approval notification phone number");
		}
		sendSMS(approvalPhoneNo.trim(), "Nytt telefonnummer må godkjennes: \"" + phoneNo + "\"");
	}
	
	private void sendSMS(String phoneNo, String message) {
		new AWSSMSSender().sendSMS("ElTodo", phoneNo, message);
	}
}
