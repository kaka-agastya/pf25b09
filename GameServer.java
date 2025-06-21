// GameServer.java (Ini adalah file terpisah, berjalan di konsol)
import java.io.*;
import java.net.*;
import java.util.concurrent.*; // Untuk ExecutorService

public class GameServer {
    private static final int PORT = 12345; // Port untuk server mendengarkan koneksi
    private static final int MAX_PLAYERS = 2; // Hanya 2 pemain per game
    private static int playerCounter = 0;
    private static final BlockingQueue<Socket> waitingPlayers = new LinkedBlockingQueue<>();
    private static final ExecutorService pool = Executors.newFixedThreadPool(4); // Pool untuk handler klien

    public static void main(String[] args) throws IOException {
        System.out.println("Tic-Tac-Toe Server is running...");
        try (ServerSocket listener = new ServerSocket(PORT)) {
            while (true) {
                Socket playerSocket = listener.accept(); // Menunggu koneksi pemain baru
                playerCounter++;
                System.out.println("Player " + playerCounter + " connected: " + playerSocket);

                waitingPlayers.offer(playerSocket); // Masukkan pemain ke antrian

                if (waitingPlayers.size() >= MAX_PLAYERS) {
                    // Cukup pemain untuk memulai game baru
                    System.out.println("Pairing two players for a new game...");
                    Socket player1 = waitingPlayers.poll();
                    Socket player2 = waitingPlayers.poll();
                    pool.execute(new GameHandler(player1, player2)); // Jalankan game di thread terpisah
                }
            }
        } finally {
            pool.shutdown();
        }
    }
}

// Kelas internal atau kelas terpisah untuk mengelola setiap game
class GameHandler implements Runnable {
    private Socket player1Socket;
    private Socket player2Socket;
    private PrintWriter out1, out2;
    private BufferedReader in1, in2;
    private Board board; // Gunakan kelas Board yang sudah ada
    private Seed currentPlayerTurn; // X atau O

    public GameHandler(Socket s1, Socket s2) {
        this.player1Socket = s1;
        this.player2Socket = s2;
        this.board = new Board(); // Inisialisasi papan baru untuk game ini
        this.currentPlayerTurn = Seed.CROSS; // X selalu mulai
    }

    private void sendMessage(PrintWriter out, String message) {
        out.println(message);
    }

    private void broadcastMessage(String message) {
        sendMessage(out1, message);
        sendMessage(out2, message);
    }

    private void sendInitialInfo() {
        // Kirim info siapa pemain X dan siapa pemain O
        sendMessage(out1, "YOU_ARE_X"); // Perlu UI di client untuk menerima ini
        sendMessage(out2, "YOU_ARE_O");

        sendMessage(out1, "MESSAGE X's Turn. Make your move.");
        sendMessage(out2, "MESSAGE X's Turn. Wait for opponent.");
    }


    @Override
    public void run() {
        try {
            // Setup input/output streams
            out1 = new PrintWriter(player1Socket.getOutputStream(), true);
            in1 = new BufferedReader(new InputStreamReader(player1Socket.getInputStream()));
            out2 = new PrintWriter(player2Socket.getOutputStream(), true);
            in2 = new BufferedReader(new InputStreamReader(player2Socket.getInputStream()));

            sendInitialInfo(); // Kirim info awal ke klien

            // Game Loop
            while (true) {
                // Tentukan giliran pemain dan stream yang akan didengarkan
                BufferedReader currentIn = (currentPlayerTurn == Seed.CROSS) ? in1 : in2;
                PrintWriter currentOut = (currentPlayerTurn == Seed.CROSS) ? out1 : out2;
                PrintWriter otherOut = (currentPlayerTurn == Seed.CROSS) ? out2 : out1;
                String currentPlayerName = (currentPlayerTurn == Seed.CROSS) ? "X" : "O";

                // Beri tahu klien yang sedang giliran untuk bergerak
                sendMessage(currentOut, "YOUR_TURN");
                sendMessage(otherOut, "OPPONENT_TURN"); // Beri tahu lawan untuk menunggu

                String clientMove = currentIn.readLine(); // Tunggu gerakan dari klien
                if (clientMove == null) {
                    System.out.println("Client disconnected.");
                    break; // Klien putus koneksi
                }
                System.out.println("Received move from " + currentPlayerName + ": " + clientMove);

                // Parse gerakan (misal: "MOVE 0 1")
                String[] parts = clientMove.split(" ");
                if (parts.length == 3 && parts[0].equals("MOVE")) {
                    try {
                        int row = Integer.parseInt(parts[1]);
                        int col = Integer.parseInt(parts[2]);

                        // Validasi gerakan (perlu Board.isValidMove method)
                        if (board.cells[row][col].content == Seed.NO_SEED) {
                            State newState = board.stepGame(currentPlayerTurn, row, col); // Update board
                            String moveMessage = "MOVE " + currentPlayerName + " " + row + " " + col;
                            broadcastMessage(moveMessage); // Kirim gerakan ke kedua klien

                            if (newState != State.PLAYING) {
                                broadcastMessage("GAME_OVER " + newState.toString());
                                System.out.println("Game over: " + newState);
                                break; // Akhiri game
                            } else {
                                currentPlayerTurn = (currentPlayerTurn == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS; // Ganti giliran
                            }
                        } else {
                            sendMessage(currentOut, "INVALID_MOVE Cell already occupied. Try again.");
                            // Jangan ganti giliran jika move tidak valid
                        }
                    } catch (NumberFormatException e) {
                        sendMessage(currentOut, "INVALID_MOVE Malformed move. Try again.");
                    }
                } else {
                    sendMessage(currentOut, "INVALID_COMMAND Unknown command. Try again.");
                }
            }
        } catch (IOException e) {
            System.err.println("Game communication error: " + e.getMessage());
        } finally {
            try {
                if (player1Socket != null) player1Socket.close();
                if (player2Socket != null) player2Socket.close();
                System.out.println("Game session closed.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}