// package MainApp; // Asumsi paket jika tidak ada di file.

import javax.swing.*;
import java.util.Scanner;
import java.awt.event.ActionEvent;

/**
 * Kelas utama aplikasi Tic-Tac-Toe.
 * Bertanggung jawab untuk proses otentikasi (login) pengguna
 * dan kemudian meluncurkan antarmuka pengguna grafis (GUI) utama game.
 */
public class MainApp {

    /**
     * Metode utama (entry point) aplikasi.
     * Mengelola proses login pengguna dan memulai GUI game.
     * @param args Argumen baris perintah, mungkin berisi detail koneksi database.
     */
    public static void main(String[] args) {
        // Variabel untuk melacak keberhasilan login
        boolean loginSuccessful = false;
        // Variabel untuk menyimpan username yang berhasil login
        String loggedInUsername = "";
        // Scanner untuk membaca input dari konsol
        Scanner sc = new Scanner(System.in);

        // Loop do-while untuk mencoba login sampai berhasil
        do {
            System.out.print("Enter Username: ");
            loggedInUsername = sc.next(); // Membaca username dari konsol

            System.out.print("Password: ");
            String pass = sc.next(); // Membaca password dari konsol
            String truePass = ""; // Password yang sebenarnya dari database

            try {
                // Memanggil DatabaseManager untuk mendapatkan password yang benar berdasarkan username
                truePass = DatabaseManager.getPassword(loggedInUsername, args);
            } catch (ClassNotFoundException e1) {
                // Menangani jika driver database tidak ditemukan
                System.out.println("Database driver not found: " + e1.getMessage());
                e1.printStackTrace();
                System.out.println("Exiting game due to driver error.");
                System.exit(1); // Keluar dari aplikasi dengan kode error
            }

            // Memeriksa apakah password yang dimasukkan cocok dengan password dari database dan tidak kosong
            if (pass.equals(truePass) && !truePass.isEmpty()) {
                loginSuccessful = true; // Login berhasil
                System.out.println("Login successful for " + loggedInUsername + "!");
            } else {
                // Pesan error jika password salah atau username tidak ditemukan
                System.out.println("Wrong password or username not found. Please try again!");
            }
        } while (!loginSuccessful); // Ulangi jika login belum berhasil

        sc.close(); // Menutup scanner setelah penggunaan

        // Menyimpan username final untuk digunakan dalam Runnable (perlu final/effectively final)
        final String finalLoggedInUsername = loggedInUsername;

        // Menjalankan kode GUI dalam Event-Dispatching Thread (EDT) untuk keamanan thread.
        // SwingUtilities.invokeLater memastikan bahwa pembaruan GUI dilakukan di thread yang benar.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Membuat JFrame (jendela utama aplikasi GUI)
                JFrame frame = new JFrame(GamePanel.TITLE);

                // Membuat instance GamePanel, yang akan menjadi konten utama jendela
                GamePanel gamePanel = new GamePanel(finalLoggedInUsername);
                frame.setContentPane(gamePanel); // Mengatur GamePanel sebagai konten jendela

                // Mengatur operasi default saat jendela ditutup (keluar dari aplikasi)
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack(); // Mengatur ukuran jendela agar pas dengan semua komponennya
                frame.setLocationRelativeTo(null); // Memusatkan jendela di layar
                frame.setVisible(true); // Membuat jendela terlihat
            }
        });
    }
}