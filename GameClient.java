// package GameClient; // Asumsi paket jika tidak ada di file.

import java.io.*;
import java.net.Socket;
import java.net.ConnectException;
import javax.swing.SwingUtilities;

/**
 * Kelas GameClient menangani komunikasi sisi klien untuk mode multiplayer berbasis soket.
 * Ini bertanggung jawab untuk terhubung ke GameServer, mengirim pergerakan,
 * dan mendengarkan pesan dari server secara real-time.
 *
 * CATATAN PENTING:
 * Berdasarkan analisis kode yang tersedia, fungsionalitas GameClient ini
 * tampaknya merupakan implementasi terpisah atau alternatif untuk multiplayer
 * dan tidak secara langsung digunakan oleh GamePanel dan MultiplayerManager
 * untuk mengirim/menerima pergerakan game. Multiplayer utama yang aktif
 * mengandalkan DatabaseManager untuk sinkronisasi.
 */
public class GameClient {
    private Socket socket;                 // Objek soket untuk koneksi ke server
    private PrintWriter out;               // Untuk mengirim data ke server
    private BufferedReader in;             // Untuk menerima data dari server
    private GameClientListener listener;   // Listener untuk callback peristiwa game
    private String serverIp;               // Alamat IP server
    private int serverPort;                // Port server
    private Thread listenerThread;         // Thread untuk mendengarkan pesan server

    /**
     * Konstruktor GameClient.
     */
    public GameClient(GameClientListener listener) {
        this.listener = listener;
    }

    /**
     * Mencoba terhubung ke GameServer.
     * Operasi koneksi dilakukan dalam thread terpisah agar tidak memblokir UI.
     */
    public void connectToServer(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;

        new Thread(() -> { // Membuat dan menjalankan thread baru
            try {
                socket = new Socket(serverIp, serverPort); // Mencoba membuat koneksi soket
                out = new PrintWriter(socket.getOutputStream(), true); // Stream output
                in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Stream input
                // Memberitahu listener bahwa koneksi berhasil
                SwingUtilities.invokeLater(() -> listener.onConnected("Connected. Waiting for opponent..."));
                System.out.println("GameClient: Connected to game server: " + serverIp + ":" + serverPort);

                // Memastikan thread listener sebelumnya diinterupsi jika masih berjalan
                if (listenerThread != null && listenerThread.isAlive()) {
                    listenerThread.interrupt();
                }
                // Membuat dan menjalankan thread baru untuk mendengarkan pesan server
                listenerThread = new Thread(this::listenForServerMessages);
                listenerThread.start();

            } catch (ConnectException e) {
                // Menangani jika koneksi ditolak (server tidak berjalan atau IP/Port salah)
                System.err.println("GameClient: Connection refused. Server might not be running or IP/Port is wrong: " + e.getMessage());
                SwingUtilities.invokeLater(() -> listener.onConnectionError("Connection refused. Server might not be running."));
            } catch (IOException e) {
                // Menangani error I/O lainnya saat koneksi
                System.err.println("GameClient: Could not connect to game server: " + e.getMessage());
                SwingUtilities.invokeLater(() -> listener.onConnectionError("Failed to connect to server. Check IP/Port."));
            }
        }).start();
    }

    /**
     * Mengirim pergerakan (baris dan kolom) ke server.
     */
    public void sendMove(int row, int col) {
        if (out != null && socket != null && socket.isConnected()) {
            out.println("MOVE " + row + " " + col); // Mengirim pesan "MOVE"
            System.out.println("GameClient: Sent move to server: " + row + "," + col);
        } else {
            System.err.println("GameClient: Not connected to server or output stream is null.");
            SwingUtilities.invokeLater(() -> listener.onMessage("Not connected to server!"));
        }
    }

    /**
     * Mengirim permintaan game baru ke server.
     */
    public void sendNewGameRequest() {
        if (out != null && socket != null && socket.isConnected()) {
            out.println("NEW_GAME_REQUEST"); // Mengirim pesan permintaan game baru
            System.out.println("GameClient: Sent new game request to server.");
        } else {
            System.err.println("GameClient: Not connected. Cannot send new game request.");
            SwingUtilities.invokeLater(() -> listener.onMessage("Cannot request new game, not connected!"));
        }
    }

    /**
     * Metode utama untuk mendengarkan pesan yang masuk dari server.
     * Berjalan dalam thread terpisah.
     */
    private void listenForServerMessages() {
        try {
            String line;
            // Membaca baris demi baris dari server sampai koneksi terputus
            while ((line = in.readLine()) != null) {
                System.out.println("GameClient: Server says: " + line);

                // Memproses berbagai jenis pesan dari server
                if (line.startsWith("YOU_ARE_X")) {
                    SwingUtilities.invokeLater(() -> listener.onPlayerAssigned("X"));
                } else if (line.startsWith("YOU_ARE_O")) {
                    SwingUtilities.invokeLater(() -> listener.onPlayerAssigned("O"));
                } else if (line.startsWith("YOUR_TURN")) {
                    SwingUtilities.invokeLater(() -> listener.onTurnChange(true, null));
                } else if (line.startsWith("OPPONENT_TURN")) {
                    SwingUtilities.invokeLater(() -> listener.onTurnChange(false, null));
                } else if (line.startsWith("MOVE")) {
                    String[] parts = line.split(" ");
                    if (parts.length >= 4) {
                        String playerSymbol = parts[1];
                        int row = Integer.parseInt(parts[2]);
                        int col = Integer.parseInt(parts[3]);
                        SwingUtilities.invokeLater(() -> listener.onMoveReceived(playerSymbol, row, col));
                    }
                } else if (line.startsWith("GAME_OVER")) {
                    String[] parts = line.split(" ");
                    State finalState = State.valueOf(parts[1]); // Mengubah string menjadi enum State
                    SwingUtilities.invokeLater(() -> listener.onGameOver(finalState));
                } else if (line.startsWith("INVALID_MOVE")) {
                    String message = line.substring("INVALID_MOVE ".length());
                    SwingUtilities.invokeLater(() -> listener.onInvalidMove(message));
                } else if (line.startsWith("MESSAGE")) {
                    String message = line.substring("MESSAGE ".length());
                    SwingUtilities.invokeLater(() -> listener.onMessage(message));
                }
                // Pesan untuk game baru yang dimulai oleh server
                else if (line.startsWith("NEW_GAME_STARTED")) {
                    SwingUtilities.invokeLater(() -> {
                        listener.onMessage("Server restarted game. Waiting for turn...");
                        listener.onTurnChange(false, null); // Mengatur giliran lawan sampai server menentukan
                    });
                }
            }
        } catch (IOException e) {
            System.err.println("GameClient: Disconnected from server: " + e.getMessage());
            SwingUtilities.invokeLater(() -> listener.onDisconnected("Lost connection to server."));
        } catch (NumberFormatException e) {
            System.err.println("GameClient: Error parsing server message: " + e.getMessage());
        } finally {
            closeConnection(); // Memastikan koneksi ditutup jika loop berakhir
        }
    }

    /**
     * Menutup koneksi soket ke server.
     */
    public void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close(); // Menutup soket
                System.out.println("GameClient: Socket connection closed.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Memeriksa apakah klien saat ini terhubung ke server.
     * @return True jika terhubung dan soket tidak ditutup, false sebaliknya.
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
}