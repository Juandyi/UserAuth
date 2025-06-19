import java.util.*;
import java.io.*;

public class SocialCalendarApp {
    public static List<Publicacion> muro = new ArrayList<>();
    private User currentUser;
    
    public SocialCalendarApp(User user) {
        this.currentUser = user;
    }
    
    public void start() {
        System.out.println("Iniciando aplicación de calendario para: " + currentUser.getUsername());
        // Implementar lógica de la aplicación de calendario
    }
    
    @SuppressWarnings("unchecked")
    public static void loadMuro() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("data/muro.ser"))) {
            muro = (List<Publicacion>) ois.readObject();
        } catch (FileNotFoundException e) {
            muro = new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading muro: " + e.getMessage());
            muro = new ArrayList<>();
        }
    }
    
    public static void saveMuro() {
        try {
            new File("data").mkdirs();
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("data/muro.ser"))) {
                oos.writeObject(muro);
            }
        } catch (IOException e) {
            System.err.println("Error saving muro: " + e.getMessage());
        }
    }
}