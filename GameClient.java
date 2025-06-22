import java.io.*;
import java.net.Socket;
import java.net.ConnectException;
import javax.swing.SwingUtilities;

public class GameClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private GameClientListener listener;
    private String serverIp;
    private int serverPort;
    private Thread listenerThread;

    public GameClient(GameClientListener listener) {
        this.listener = listener;
    }

    public void connectToServer(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;

        new Thread(() -> {
            try {
                socket = new Socket(serverIp, serverPort);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                SwingUtilities.invokeLater(() -> listener.onConnected("Connected. Waiting for opponent..."));
                System.out.println("GameClient: Connected to game server: " + serverIp + ":" + serverPort);

                if (listenerThread != null && listenerThread.isAlive()) {
                    listenerThread.interrupt(); 
                }
                listenerThread = new Thread(this::listenForServerMessages);
                listenerThread.start();

            } catch (ConnectException e) {
                System.err.println("GameClient: Connection refused. Server might not be running or IP/Port is wrong: " + e.getMessage());
                SwingUtilities.invokeLater(() -> listener.onConnectionError("Connection refused. Server might not be running."));
            } catch (IOException e) {
                System.err.println("GameClient: Could not connect to game server: " + e.getMessage());
                SwingUtilities.invokeLater(() -> listener.onConnectionError("Failed to connect to server. Check IP/Port."));
            }
        }).start();
    }

    public void sendMove(int row, int col) {
        if (out != null && socket != null && socket.isConnected()) {
            out.println("MOVE " + row + " " + col);
            System.out.println("GameClient: Sent move to server: " + row + "," + col);
        } else {
            System.err.println("GameClient: Not connected to server or output stream is null.");
            SwingUtilities.invokeLater(() -> listener.onMessage("Not connected to server!"));
        }
    }

    public void sendNewGameRequest() {
        if (out != null && socket != null && socket.isConnected()) {
            out.println("NEW_GAME_REQUEST"); 
            System.out.println("GameClient: Sent new game request to server.");
        } else {
            System.err.println("GameClient: Not connected. Cannot send new game request.");
            SwingUtilities.invokeLater(() -> listener.onMessage("Cannot request new game, not connected!"));
        }
    }

    private void listenForServerMessages() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("GameClient: Server says: " + line);

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
                    State finalState = State.valueOf(parts[1]);
                    SwingUtilities.invokeLater(() -> listener.onGameOver(finalState));
                } else if (line.startsWith("INVALID_MOVE")) {
                    String message = line.substring("INVALID_MOVE ".length());
                    SwingUtilities.invokeLater(() -> listener.onInvalidMove(message));
                } else if (line.startsWith("MESSAGE")) {
                    String message = line.substring("MESSAGE ".length());
                    SwingUtilities.invokeLater(() -> listener.onMessage(message));
                }

                else if (line.startsWith("NEW_GAME_STARTED")) {
                    SwingUtilities.invokeLater(() -> {
                        listener.onMessage("Server restarted game. Waiting for turn...");
                        listener.onTurnChange(false, null);
                    });
                }
            }
        } catch (IOException e) {
            System.err.println("GameClient: Disconnected from server: " + e.getMessage());
            SwingUtilities.invokeLater(() -> listener.onDisconnected("Lost connection to server."));
        } catch (NumberFormatException e) {
            System.err.println("GameClient: Error parsing server message: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    public void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("GameClient: Socket connection closed.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
}