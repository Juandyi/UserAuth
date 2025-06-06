package auth;

import java.io.Serializable;

public class User implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String username;
	private String password;
	private boolean passwordResetPending;
	
	public User(String username, String password) {
		 this.username = username;
		 this.password = password;
		 this.passwordResetPending = true;  // Flag for password reset on first login
	}
	
	public String getUsername() {
		 return username;
	}
	
	public boolean authenticate(String password) {
		 return this.password.equals(password);
	}

	public String setPassword(String password) {
		 return this.password = password;
	}
	
	public String getPassword() {
		 return password;
	}
	
	public boolean isPasswordResetPending() {
	return passwordResetPending;
	}
	
	public void setPasswordResetPending(boolean passwordResetPending) {
	this.passwordResetPending = passwordResetPending;
	}
	
}

