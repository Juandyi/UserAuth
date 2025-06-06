package baloto;

import javax.swing.*;

import auth.User;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class BalotoApp {
    private User user; // authenticated user
    private Properties suggestions;
    private final String PROPERTIES_FILE = "data\\baloto.properties";

    public BalotoApp(User user) {
        this.user = user;
        suggestions = new Properties();
        // Load existing suggestions if needed (optional)
        // try (FileInputStream in = new FileInputStream(PROPERTIES_FILE)) {
        //     suggestions.load(in);
        // } catch (IOException e) {
        //     // ignore if file does not exist
        // }
    }

    public void start() {
        while (true) {
            int option = JOptionPane.showOptionDialog(null,
                    "Welcome " + user.getUsername() + "! What do you want to do?",
                    "Baloto Number Generator",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    new String[]{"Suggest Numbers", "Exit"},
                    "Suggest Numbers");

            if (option != 0) break;

            List<Integer> balls = generateBalls();
            int superBall = generateSuperBall();

            showSuggestion(balls, superBall);

            int saveOption = JOptionPane.showConfirmDialog(null,
                    "Save this suggestion?",
                    "Save Suggestion",
                    JOptionPane.YES_NO_OPTION);

            if (saveOption == JOptionPane.YES_OPTION) {
                saveSuggestion(balls, superBall);
            }
        }
    }

    private List<Integer> generateBalls() {
        Random rand = new Random();
        Set<Integer> balls = new TreeSet<>();
        while (balls.size() < 6) {
            balls.add(rand.nextInt(46) + 1);
        }
        return new ArrayList<>(balls);
    }

    private int generateSuperBall() {
        Random rand = new Random();
        return rand.nextInt(16) + 1;
    }

    private void showSuggestion(List<Integer> balls, int superBall) {
        // Build HTML text with large font and colors
        StringBuilder sb = new StringBuilder("<html><div style='font-size:24pt; text-align:center;'>");
        sb.append("Balls: ");
        for (int ball : balls) {
            sb.append("<span style='color:blue; margin: 0 5px;'>").append(ball).append("</span>");
        }
        sb.append("<br>");
        sb.append("SuperBall: <span style='color:red;'>").append(superBall).append("</span>");
        sb.append("</div></html>");

        JLabel label = new JLabel(sb.toString());
        label.setHorizontalAlignment(SwingConstants.CENTER);

        JOptionPane.showMessageDialog(null, label, "Baloto Suggestion", JOptionPane.PLAIN_MESSAGE);
    }

    private void saveSuggestion(List<Integer> balls, int superBall) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String value = "Balls=" + balls.toString() + ", SuperBall=" + superBall;
        suggestions.setProperty(timestamp, value);

        try (FileOutputStream out = new FileOutputStream(PROPERTIES_FILE)) {
            suggestions.store(out, "Baloto Suggestions for user: " + user.getUsername());
            JOptionPane.showMessageDialog(null, "Suggestion saved!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving suggestion: " + e.getMessage());
        }
    }
}

