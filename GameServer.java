
import java.io.*;
import java.net.*;
import java.util.concurrent.*; 

public class GameServer {
    private static final int PORT = 12345; 
    private static final int MAX_PLAYERS = 2; 
    private static int playerCounter = 0;
    private static final BlockingQueue<Socket> waitingPlayers = new LinkedBlockingQueue<>();
    private static final ExecutorService pool = Executors.newFixedThreadPool(4); 

    public static void main(String[] args) throws IOException {
        System.out.println("Tic-Tac-Toe Server is running...");
        try (ServerSocket listener = new ServerSocket(PORT)) {
            while (true) {
                Socket playerSocket = listener.accept(); 
                playerCounter++;
                System.out.println("Player " + playerCounter + " connected: " + playerSocket);

                waitingPlayers.offer(playerSocket); 

                if (waitingPlayers.size() >= MAX_PLAYERS) {
                    
                    System.out.println("Pairing two players for a new game...");
                    Socket player1 = waitingPlayers.poll();
                    Socket player2 = waitingPlayers.poll();
                    pool.execute(new GameHandler(player1, player2)); 
                }
            }
        } finally {
            pool.shutdown();
        }
    }
}


class GameHandler implements Runnable {
    private Socket player1Socket;
    private Socket player2Socket;
    private PrintWriter out1, out2;
    private BufferedReader in1, in2;
    private Board board; 
    private Seed currentPlayerTurn; 

    public GameHandler(Socket s1, Socket s2) {
        this.player1Socket = s1;
        this.player2Socket = s2;
        this.board = new Board(); 
        this.currentPlayerTurn = Seed.CROSS; 
    }

    private void sendMessage(PrintWriter out, String message) {
        out.println(message);
    }

    private void broadcastMessage(String message) {
        sendMessage(out1, message);
        sendMessage(out2, message);
    }

    private void sendInitialInfo() {
        
        sendMessage(out1, "YOU_ARE_X"); 
        sendMessage(out2, "YOU_ARE_O");

        sendMessage(out1, "MESSAGE X's Turn. Make your move.");
        sendMessage(out2, "MESSAGE X's Turn. Wait for opponent.");
    }


    @Override
    public void run() {
        try {
            
            out1 = new PrintWriter(player1Socket.getOutputStream(), true);
            in1 = new BufferedReader(new InputStreamReader(player1Socket.getInputStream()));
            out2 = new PrintWriter(player2Socket.getOutputStream(), true);
            in2 = new BufferedReader(new InputStreamReader(player2Socket.getInputStream()));

            sendInitialInfo(); 

            
            while (true) {
                
                BufferedReader currentIn = (currentPlayerTurn == Seed.CROSS) ? in1 : in2;
                PrintWriter currentOut = (currentPlayerTurn == Seed.CROSS) ? out1 : out2;
                PrintWriter otherOut = (currentPlayerTurn == Seed.CROSS) ? out2 : out1;
                String currentPlayerName = (currentPlayerTurn == Seed.CROSS) ? "X" : "O";

                
                sendMessage(currentOut, "YOUR_TURN");
                sendMessage(otherOut, "OPPONENT_TURN"); 

                String clientMove = currentIn.readLine(); 
                if (clientMove == null) {
                    System.out.println("Client disconnected.");
                    break; 
                }
                System.out.println("Received move from " + currentPlayerName + ": " + clientMove);

                
                String[] parts = clientMove.split(" ");
                if (parts.length == 3 && parts[0].equals("MOVE")) {
                    try {
                        int row = Integer.parseInt(parts[1]);
                        int col = Integer.parseInt(parts[2]);

                        
                        if (board.cells[row][col].content == Seed.NO_SEED) {
                            State newState = board.stepGame(currentPlayerTurn, row, col); 
                            String moveMessage = "MOVE " + currentPlayerName + " " + row + " " + col;
                            broadcastMessage(moveMessage); 

                            if (newState != State.PLAYING) {
                                broadcastMessage("GAME_OVER " + newState.toString());
                                System.out.println("Game over: " + newState);
                                break; 
                            } else {
                                currentPlayerTurn = (currentPlayerTurn == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS; 
                            }
                        } else {
                            sendMessage(currentOut, "INVALID_MOVE Cell already occupied. Try again.");
                            
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