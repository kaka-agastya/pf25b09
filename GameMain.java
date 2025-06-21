import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.Locale;
import java.util.Scanner;
import java.io.*;
import java.net.Socket;


public class GameMain extends JPanel {
    private static final long serialVersionUID = 1L; 

    public static final String TITLE = "Tic Tac Toe";
    public static final Color COLOR_BG = Color.WHITE;
    public static final Color COLOR_BG_STATUS = new Color(216, 216, 216);
    public static final Color COLOR_CROSS = new Color(239, 105, 80);  // Red #EF6950
    public static final Color COLOR_NOUGHT = new Color(64, 154, 225); // Blue #409AE1
    public static final Font FONT_STATUS = new Font("OCR A Extended", Font.PLAIN, 14);

    // Define game objects
    private Board board;          // the game board
    private State currentState;   // the current state of the game
    private Seed currentPlayer;   // the current player (locally tracks who moved last, but server decides next)
    private JLabel statusBar;     // for displaying status message

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String playerRole; 
    private boolean isMyTurn = false; 

    public GameMain() {

        super.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {  
                if (currentState == State.PLAYING && isMyTurn) { 
                    int mouseX = e.getX();
                    int mouseY = e.getY();
                 
                    int row = mouseY / Cell.SIZE;
                    int col = mouseX / Cell.SIZE;

                    if (row >= 0 && row < Board.ROWS && col >= 0 && col < Board.COLS
                            && board.cells[row][col].content == Seed.NO_SEED) {
                        sendMoveToServer(row, col); // Send move to server

                        SwingUtilities.invokeLater(() -> statusBar.setText("Waiting for opponent..."));
                    } else {
                        SwingUtilities.invokeLater(() -> statusBar.setText("Invalid move! Cell " + row + "," + col + " is occupied or out of bounds."));
                    }
                } else if (currentState != State.PLAYING) {      
          
                    SwingUtilities.invokeLater(() -> {
                        statusBar.setText("Game is over. Click to start a new game (if server supports).");

                    });
                } else if (!isMyTurn) {
                    SwingUtilities.invokeLater(() -> statusBar.setText("It's not your turn. Please wait for opponent."));
                }
            }
        });

        statusBar = new JLabel();
        statusBar.setFont(FONT_STATUS);
        statusBar.setBackground(COLOR_BG_STATUS);
        statusBar.setOpaque(true);
        statusBar.setPreferredSize(new Dimension(300, 30));
        statusBar.setHorizontalAlignment(JLabel.LEFT);
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 12));

        super.setLayout(new BorderLayout());
        super.add(statusBar, BorderLayout.PAGE_END); // same as SOUTH
        super.setPreferredSize(new Dimension(Board.CANVAS_WIDTH, Board.CANVAS_HEIGHT + 30));

        super.setBorder(BorderFactory.createLineBorder(COLOR_BG_STATUS, 2, false));

 
        initGame();
        newGame(); 
    }

    public void initGame() {
        board = new Board();  // allocate the game-board
    }

    public void newGame() {
        for (int row = 0; row < Board.ROWS; ++row) {
            for (int col = 0; col < Board.COLS; ++col) {
                board.cells[row][col].content = Seed.NO_SEED; // all cells empty
            }
        }
        currentPlayer = Seed.CROSS;    // cross plays first
        currentState = State.PLAYING;
    }

    @Override
    public void paintComponent(Graphics g) {  
        super.paintComponent(g);
        setBackground(COLOR_BG); // set its background color

        board.paint(g);  // ask the game board to paint itself

        if (currentState == State.PLAYING) {
            statusBar.setForeground(Color.BLACK);

        } else if (currentState == State.DRAW) {
            statusBar.setForeground(Color.RED);
            statusBar.setText("It's a Draw! Click to play again (server restart needed).");
        } else if (currentState == State.CROSS_WON) {
            statusBar.setForeground(Color.RED);
            statusBar.setText("'X' Won! Click to play again (server restart needed).");
        } else if (currentState == State.NOUGHT_WON) {
            statusBar.setForeground(Color.RED);
            statusBar.setText("'O' Won! Click to play again (server restart needed).");
        }
    }

    private void connectToServer(String serverIp, int port) {
        try {
            socket = new Socket(serverIp, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Connected to game server: " + serverIp + ":" + port);
            SwingUtilities.invokeLater(() -> statusBar.setText("Connected. Waiting for opponent..."));


            new Thread(this::listenForServerMessages).start();

        } catch (IOException e) {
            System.err.println("Could not connect to game server: " + e.getMessage());
            SwingUtilities.invokeLater(() -> statusBar.setText("Failed to connect to server. Check IP/Port."));
        }
    }

    private void sendMoveToServer(int row, int col) {
        if (out != null && socket.isConnected()) {
            out.println("MOVE " + row + " " + col); // Format: "MOVE <row> <col>"
            System.out.println("Sent move to server: " + row + "," + col);
            isMyTurn = false;
        } else {
            System.err.println("Not connected to server or output stream is null.");
            SwingUtilities.invokeLater(() -> statusBar.setText("Not connected to server!"));
        }
    }

    private void listenForServerMessages() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("Server says: " + line); 

                if (line.startsWith("YOU_ARE_X")) {
                    playerRole = "X";
                    SwingUtilities.invokeLater(() -> statusBar.setText("You are Player X. Waiting for opponent."));
                } else if (line.startsWith("YOU_ARE_O")) {
                    playerRole = "O";
                    SwingUtilities.invokeLater(() -> statusBar.setText("You are Player O. Waiting for Player X."));
                } else if (line.startsWith("YOUR_TURN")) {
                    isMyTurn = true;
                    SoundEffect.EAT_FOOD.play(); // Optional: sound for turn
                    SwingUtilities.invokeLater(() -> statusBar.setText("Your turn (" + playerRole + "). Make your move."));
                } else if (line.startsWith("OPPONENT_TURN")) {
                    isMyTurn = false;
                    SwingUtilities.invokeLater(() -> statusBar.setText("Opponent's turn. Please wait."));
                } else if (line.startsWith("MOVE")) {

                    String[] parts = line.split(" ");
                    if (parts.length >= 4) { // Ensure enough parts
                        String playerSymbol = parts[1];
                        int row = Integer.parseInt(parts[2]);
                        int col = Integer.parseInt(parts[3]);

                        Seed movedSeed = (playerSymbol.equals("X")) ? Seed.CROSS : Seed.NOUGHT;

                        SwingUtilities.invokeLater(() -> {
                            board.cells[row][col].content = movedSeed;
                            repaint(); // Redraw the board
                        });
                        SoundEffect.DIE.play(); // Optional: sound for opponent move
                    }
                } else if (line.startsWith("GAME_OVER")) {
                    String[] parts = line.split(" ");
                    State finalState = State.valueOf(parts[1]); // e.g., CROSS_WON, NOUGHT_WON, DRAW
                    SwingUtilities.invokeLater(() -> {
                        currentState = finalState; // Update game state for UI
                        repaint(); // Trigger paintComponent to show game over message
                        isMyTurn = false; // Game over, no more turns
                    });
                    SoundEffect.DIE.play(); // Optional: game over sound
                } else if (line.startsWith("INVALID_MOVE")) {
                    String message = line.substring("INVALID_MOVE ".length());
                    SwingUtilities.invokeLater(() -> statusBar.setText("Invalid Move: " + message));
                    isMyTurn = true; // Still my turn if move was invalid
                } else if (line.startsWith("MESSAGE")) {
                    String message = line.substring("MESSAGE ".length());
                    SwingUtilities.invokeLater(() -> statusBar.setText(message));
                }

            }
        } catch (IOException e) {
            System.err.println("Disconnected from server: " + e.getMessage());
            SwingUtilities.invokeLater(() -> statusBar.setText("Lost connection to server."));
        } catch (NumberFormatException e) {
            System.err.println("Error parsing server message: " + e.getMessage());
        } finally {
            try {
                if (socket != null) socket.close();
                System.out.println("Client socket closed.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private static final String DB_HOST = "mysql-tictactoee-kakagastya2-ecdb.c.aivencloud.com";
    private static final String DB_PORT = "28606";
    private static final String DB_NAME = "defaultdb";
    private static final String DB_USER = "avnadmin";
    private static final String DB_PASS = "AVNS_bgOBYQNuctKG0ErN3KQ";

    static String getPassword(String uName, String[] args) throws ClassNotFoundException {
        String pass = ""; 
        String host = DB_HOST;
        String port = DB_PORT;
        String databaseName = DB_NAME;
        String userName = DB_USER;
        String dbPassword = DB_PASS; // Password untuk koneksi DB

        // Logika parsing command-line arguments untuk override kredensial DB
        for (int i = 0; i < args.length; i++) {
            if (i + 1 < args.length) { // Pastikan ada argumen setelah flag
                switch (args[i].toLowerCase(Locale.ROOT)) {
                    case "-host": host = args[++i]; break;
                    case "-username": userName = args[++i]; break;
                    case "-password": dbPassword = args[++i]; break;
                    case "-database": databaseName = args[++i]; break;
                    case "-port": port = args[++i]; break;
                }
            }
        }

        if (host == null || port == null || databaseName == null) {
            System.out.println("Host, port, database information is required.");
            return "";
        }

        Class.forName("com.mysql.cj.jdbc.Driver");
        String sqlQuery = "SELECT password FROM gameuser WHERE username = ?";

        try (final Connection connection =
                     DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + databaseName + "?sslmode=require", userName, dbPassword);
             final PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, uName);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) { 
                    pass = resultSet.getString("password");
                } else {
                    System.out.println("Username '" + uName + "' not found in database.");
                    pass = "";
                }
            }
        } catch (SQLException e) {
            System.out.println("Database connection or query failed: " + e.getMessage());
            e.printStackTrace();
            pass = ""; 
        }
        return pass;
    }


    public static void main(String[] args) {
      
        boolean wrongPassword = true;
        String uName = "";
        Scanner sc = new Scanner(System.in); 

        do {
            System.out.print("Enter Username: ");
            uName = sc.next();

            System.out.print("Password: ");
            String pass = sc.next();
            String truePass = "";

            try {
                truePass = getPassword(uName, args);
            } catch (ClassNotFoundException e1) {
                System.out.println("Database driver not found: " + e1.getMessage());
                e1.printStackTrace();
                System.out.println("Exiting game due to driver error.");
                System.exit(1); 
            }

            if (pass.equals(truePass) && !truePass.isEmpty()) { 
                wrongPassword = false;
                System.out.println("Login successful!");
            } else {
                System.out.println("Wrong password or username not found. Please try again!");
            }
        } while (wrongPassword);
        
        sc.close(); 

        final String loggedInUsername = uName; 

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new JFrame(TITLE);
                GameMain gamePanel = new GameMain(); // Create an instance of GameMain
                frame.setContentPane(gamePanel);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setLocationRelativeTo(null); // Center the application window
                frame.setVisible(true);          // show it

                gamePanel.connectToServer("localhost", 12345); 

            }
        });
    }
}