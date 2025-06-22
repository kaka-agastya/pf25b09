import javax.swing.*;
import java.util.Scanner;
import java.awt.event.ActionEvent; 

public class MainApp {

    public static void main(String[] args) {
        
        boolean loginSuccessful = false;
        String loggedInUsername = "";
        Scanner sc = new Scanner(System.in);

        do {
            System.out.print("Enter Username: ");
            loggedInUsername = sc.next();

            System.out.print("Password: ");
            String pass = sc.next();
            String truePass = "";

            try {
                
                truePass = DatabaseManager.getPassword(loggedInUsername, args);
            } catch (ClassNotFoundException e1) {
                System.out.println("Database driver not found: " + e1.getMessage());
                e1.printStackTrace();
                System.out.println("Exiting game due to driver error.");
                System.exit(1); 
            }

            if (pass.equals(truePass) && !truePass.isEmpty()) {
                loginSuccessful = true;
                System.out.println("Login successful for " + loggedInUsername + "!");
            } else {
                System.out.println("Wrong password or username not found. Please try again!");
            }
        } while (!loginSuccessful);

        sc.close(); 

        
        final String finalLoggedInUsername = loggedInUsername;

        
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                
                JFrame frame = new JFrame(GamePanel.TITLE); 
                
                GamePanel gamePanel = new GamePanel(finalLoggedInUsername);
                frame.setContentPane(gamePanel); 

                
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
                frame.pack(); 
                frame.setLocationRelativeTo(null); 
                frame.setVisible(true); 
            }
        });
    }
}
