package auth;

import java.io.Serializable;
import java.security.SecureRandom;

public class Admin extends User implements Serializable {
	private static final long serialVersionUID = 1L;
	
    private static int userCounter = 1;  // static counter shared by all Admin instances
    private SecureRandom random = new SecureRandom();

    public Admin(String username, String password) {
        super(username, password);
    }

    // Creates a new user with a sequential username formatted with three digits and random password
    public User createNewUser() {
        String username = generateUsername();
        String password = generatePassword();
        return new User(username, password);
    }

    private String generateUsername() {
        // Format number with leading zeros, width 3
        return String.format("user%03d", userCounter++);
    }

    private String generatePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
    
    // Getter and setter for userCounter to persist static field
    public static int getUserCounter() {
        return userCounter;
    }

    public static void setUserCounter(int counter) {
        userCounter = counter;
    }    
}
