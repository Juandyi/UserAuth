package auth;

import baloto.BalotoApp;
import todo.ToDoApp;

public class MainApp {
	
    public static void main(String[] args) {
    	// testToDoApp();
    	// testBalotoApp();
           Controller controller = new Controller();
           controller.start();
    }
    
    // Method to test the ToDoApp functionality
    //public static void testToDoApp() {
        // Create a fictitious user
        //User testUser = new User("testuser", "testpass");

        // Launch the simple ToDoApp for this user
        //ToDoApp todoApp = new ToDoApp(testUser);
        //todoApp.start();    	
    //}
    
    // Method to test the ToDoApp functionality
    //public static void testBalotoApp() {
        // Create a fictitious user
        //User testUser = new User("testuser", "testpass");

        // Launch the simple ToDoApp for this user
        //BalotoApp balotoApp = new BalotoApp(testUser);
        //balotoApp.start();    	
    //}  

	// Metdodo para comprobar la funcionalidad de SocialCalendarApp
	public static void testSocialCalendarApp(){
		User testUser = new User("testuser", "testpass");
		socialCalendarApp = new SocialCalendarApp(testUser);
		socialCalendarApp.start();
}
