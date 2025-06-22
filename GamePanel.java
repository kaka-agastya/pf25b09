import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.URL;

public class GamePanel extends JPanel implements GameClientListener {
    private static final long serialVersionUID = 1L;

    public static final String TITLE = "Tic Tac Toe"; 
    public static final Color COLOR_BG = Color.WHITE; 
    public static final Color COLOR_BG_STATUS = new Color(0, 0, 0, 255); 
    public static final Color COLOR_CROSS = new Color(239, 105, 80);
    public static final Color COLOR_NOUGHT = new Color(64, 154, 225);
    public static final Font FONT_STATUS = new Font("OCR A Extended", Font.PLAIN, 14);

    private ImageIcon backgroundImage;
    private JLabel titleLabel;
    private JLabel playerXNameLabel; 
    private JLabel playerONameLabel; 
    private JLabel scoreLabel;
    private JButton playVsComputerButton;
    private JButton playVsFriendButton;
    private JButton playAgainButton;
    private JButton resetScoreButton;
    private JLabel statusBar; 

    private Board board;
    private State currentState;
    private Seed currentPlayer; 

    private GameMode gameMode = null; 
    private String loggedInUsername;
    private int xScore = 0; 
    private int oScore = 0; 

    private GameClient gameClient; 
    private String playerRole; 

    private volatile boolean isMyTurn = false;

    private TicTacToeAI ai;
    private Seed playerSeed;
    private Seed aiSeed;

    private JPanel seedSelectionPanel;
    private JButton selectXButton;
    private JButton selectOButton;

    private JPanel firstTurnSelectionPanel;
    private JButton playerStartsButton;
    private JButton aiStartsButton;


    public enum GameMode {
        MULTIPLAYER,
        VS_AI
    }

    public GamePanel(String username) {
        this.loggedInUsername = username;
        this.gameClient = new GameClient(this); 
        
        try {
            URL bgURL = getClass().getClassLoader().getResource("images/background.jpg");
            if (bgURL != null) {
                backgroundImage = new ImageIcon(bgURL);
            } else {
                System.err.println("Background image not found: images/background.jpg");
                setBackground(new Color(50, 50, 100)); 
            }
        } catch (Exception e) {
            System.err.println("Error loading background image: " + e.getMessage());
            setBackground(new Color(50, 50, 100)); 
        }

        super.setLayout(new BorderLayout());
        super.setPreferredSize(new Dimension(Board.CANVAS_WIDTH + 300, Board.CANVAS_HEIGHT + 300));
        super.setBorder(BorderFactory.createLineBorder(COLOR_BG_STATUS, 2, false));
        super.setOpaque(false);

        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS)); 
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0)); 

        titleLabel = new JLabel("Tiny Tactics!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.YELLOW);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        
        JPanel playerNamesPanel = new JPanel();
        playerNamesPanel.setOpaque(false);
        playerNamesPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0)); 

        playerXNameLabel = new JLabel("Player X: -"); 
        playerXNameLabel.setFont(new Font("Arial", Font.BOLD, 20));
        playerXNameLabel.setForeground(COLOR_CROSS); 
        
        scoreLabel = new JLabel(xScore + " | " + oScore); 
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 24));
        scoreLabel.setForeground(Color.WHITE);

        playerONameLabel = new JLabel("Player O: -"); 
        playerONameLabel.setFont(new Font("Arial", Font.BOLD, 20));
        playerONameLabel.setForeground(COLOR_NOUGHT); 

        playerNamesPanel.add(playerXNameLabel);
        playerNamesPanel.add(scoreLabel);
        playerNamesPanel.add(playerONameLabel);
        
        
        JPanel modeButtonsPanel = new JPanel();
        modeButtonsPanel.setOpaque(false);
        modeButtonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 5));

        playVsComputerButton = new JButton("Play vs Computer");
        styleModeButton(playVsComputerButton);
        playVsComputerButton.addActionListener(e -> {
            gameMode = GameMode.VS_AI;
            showSeedSelectionPanel(); 
        });

        playVsFriendButton = new JButton("Play vs Friend");
        styleModeButton(playVsFriendButton);
        playVsFriendButton.addActionListener(e -> {
            gameMode = GameMode.MULTIPLAYER;
            startGameInSelectedMode(); 
        });

        modeButtonsPanel.add(playVsComputerButton);
        modeButtonsPanel.add(playVsFriendButton);

        topPanel.add(titleLabel);
        topPanel.add(Box.createVerticalStrut(10)); 
        topPanel.add(playerNamesPanel); 
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(modeButtonsPanel);

        super.add(topPanel, BorderLayout.PAGE_START);

        JPanel bottomButtonsPanel = new JPanel();
        bottomButtonsPanel.setOpaque(false);
        bottomButtonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));

        playAgainButton = new JButton("Play Again");
        styleActionButton(playAgainButton);
        playAgainButton.addActionListener(e -> newGame()); 

        resetScoreButton = new JButton("Reset Score");
        styleActionButton(resetScoreButton);
        resetScoreButton.addActionListener(e -> resetScores()); 

        bottomButtonsPanel.add(playAgainButton);
        bottomButtonsPanel.add(resetScoreButton);
        super.add(bottomButtonsPanel, BorderLayout.PAGE_END);

        addMouseListenerToGameBoard(); 

        statusBar = new JLabel();
        statusBar.setFont(FONT_STATUS);
        statusBar.setBackground(COLOR_BG_STATUS);
        statusBar.setOpaque(true);
        statusBar.setPreferredSize(new Dimension(300, 30));
        statusBar.setHorizontalAlignment(JLabel.CENTER);
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 12));
        
        
        initGame(); 
        newGame(); 
        updateScoreLabel(); 
        updatePlayerNameLabels(); 
        updateActionButtonsVisibility(false); 

        createSeedSelectionPanel(); 
        createFirstTurnSelectionPanel(); 
    }

    private void styleModeButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(new Color(64, 154, 225)); 
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setBorder(BorderFactory.createLineBorder(new Color(40, 120, 180), 2, true));
    }

    private void styleActionButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(new Color(239, 105, 80)); 
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setBorder(BorderFactory.createLineBorder(new Color(180, 80, 60), 2, true));
    }

    private void updateScoreLabel() {
        SwingUtilities.invokeLater(() -> scoreLabel.setText(xScore + " | " + oScore));
    }
    
    private void updatePlayerNameLabels() {
        if (gameMode == GameMode.VS_AI) {
            SwingUtilities.invokeLater(() -> {
                
                if (playerSeed != null) {
                    if (playerSeed == Seed.CROSS) {
                        playerXNameLabel.setText("Player X: " + loggedInUsername);
                        playerONameLabel.setText("Player O: Computer");
                    } else { 
                        playerXNameLabel.setText("Player X: Computer");
                        playerONameLabel.setText("Player O: " + loggedInUsername);
                    }
                } else { 
                    playerXNameLabel.setText("Player X: (You)");
                    playerONameLabel.setText("Player O: (Computer)");
                }
            });
        } else if (gameMode == GameMode.MULTIPLAYER) {
            SwingUtilities.invokeLater(() -> {
                if (playerRole != null) { 
                    if (playerRole.equals("X")) {
                        playerXNameLabel.setText("Player X: " + loggedInUsername);
                        playerONameLabel.setText("Player O: Opponent");
                    } else { 
                        playerXNameLabel.setText("Player X: Opponent");
                        playerONameLabel.setText("Player O: " + loggedInUsername);
                    }
                } else { 
                    playerXNameLabel.setText("Player X: -");
                    playerONameLabel.setText("Player O: -");
                }
            });
        } else { 
            SwingUtilities.invokeLater(() -> {
                playerXNameLabel.setText("Player X: -");
                playerONameLabel.setText("Player O: -");
            });
        }
    }


    private void updateActionButtonsVisibility(boolean visible) {
        SwingUtilities.invokeLater(() -> {
            playAgainButton.setVisible(visible);
            resetScoreButton.setVisible(visible);
        });
    }

    private void addMouseListenerToGameBoard() {
        
        MouseListener[] listeners = getMouseListeners();
        for (MouseListener l : listeners) {
            removeMouseListener(l);
        }
        
        super.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                GamePanel.this.handleMouseClick(e); 
            }
        });
    }

    public void initGame() {
        board = new Board();
    }

    
    public void newGame() { 
        board.newGame(); 
        currentState = State.PLAYING;
        currentPlayer = Seed.CROSS; 
        isMyTurn = false; 
        repaint(); 
        updateActionButtonsVisibility(false); 

        
        if (gameMode == GameMode.VS_AI) {
            
            
            showFirstTurnPanelForVsAI(); 
        } else if (gameMode == GameMode.MULTIPLAYER) {
            
            if (gameClient.isConnected()) { 
                gameClient.sendNewGameRequest(); 
                SwingUtilities.invokeLater(() -> statusBar.setText("Requesting new game from server..."));
            } else {
                
                SwingUtilities.invokeLater(() -> statusBar.setText("Disconnected. Cannot request new game. Select mode again."));
                revertToModeSelection();
            }
        } else {
            
            SwingUtilities.invokeLater(() -> statusBar.setText("Welcome, " + loggedInUsername + "! Please select a game mode."));
        }
        updatePlayerNameLabels(); 
    }

    private void showSeedSelectionPanel() {
        
        playVsComputerButton.setVisible(false);
        playVsFriendButton.setVisible(false);
        
        super.add(seedSelectionPanel, BorderLayout.CENTER); 
        revalidate(); 
        repaint(); 
        SwingUtilities.invokeLater(() -> statusBar.setText("Choose your symbol (X or O):"));
        updatePlayerNameLabels(); 
    }

    private void createSeedSelectionPanel() {
        seedSelectionPanel = new JPanel();
        seedSelectionPanel.setOpaque(false);
        seedSelectionPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20)); 
        seedSelectionPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50)); 

        selectXButton = new JButton("Play as X");
        styleSeedButton(selectXButton, COLOR_CROSS);
        selectXButton.addActionListener(e -> {
            playerSeed = Seed.CROSS;
            aiSeed = Seed.NOUGHT;
            showFirstTurnPanelForVsAI(); 
        });

        selectOButton = new JButton("Play as O");
        styleSeedButton(selectOButton, COLOR_NOUGHT);
        selectOButton.addActionListener(e -> {
            playerSeed = Seed.NOUGHT;
            aiSeed = Seed.CROSS;
            showFirstTurnPanelForVsAI(); 
        });

        seedSelectionPanel.add(selectXButton);
        seedSelectionPanel.add(selectOButton);
    }

    private void styleSeedButton(JButton button, Color bgColor) {
        button.setFont(new Font("Arial", Font.BOLD, 36));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40)); 
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); 
        button.setOpaque(true);
        button.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3, true)); 
    }

    private void showFirstTurnPanelForVsAI() {
        super.remove(seedSelectionPanel); 
        super.add(firstTurnSelectionPanel, BorderLayout.CENTER); 
        revalidate();
        repaint();
        SwingUtilities.invokeLater(() -> statusBar.setText("Who makes the first move?"));
        
        updatePlayerNameLabels(); 
    }

    private void createFirstTurnSelectionPanel() {
        firstTurnSelectionPanel = new JPanel();
        firstTurnSelectionPanel.setOpaque(false);
        firstTurnSelectionPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        firstTurnSelectionPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        playerStartsButton = new JButton("You Start");
        styleTurnButton(playerStartsButton, COLOR_NOUGHT); 
        playerStartsButton.addActionListener(e -> startVsAIGame(true)); 

        aiStartsButton = new JButton("AI Starts");
        styleTurnButton(aiStartsButton, COLOR_CROSS); 
        aiStartsButton.addActionListener(e -> startVsAIGame(false)); 

        firstTurnSelectionPanel.add(playerStartsButton);
        firstTurnSelectionPanel.add(aiStartsButton);
    }

    private void styleTurnButton(JButton button, Color bgColor) {
        button.setFont(new Font("Arial", Font.BOLD, 28));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3, true));
    }


    private void startGameInSelectedMode() {
        
        playVsComputerButton.setVisible(false);
        playVsFriendButton.setVisible(false);
        
        newGame(); 
        
        if (gameMode == GameMode.MULTIPLAYER) {
            
            gameClient.connectToServer("localhost", 12345);
        }
        
        updatePlayerNameLabels(); 
    }

    private void startVsAIGame(boolean humanStarts) {
        super.remove(firstTurnSelectionPanel); 
        
        revalidate();
        repaint();

        ai = new TicTacToeAI(); 

        
        if (humanStarts) {
            isMyTurn = true;
            SwingUtilities.invokeLater(() -> statusBar.setText("You are " + playerSeed.getDisplayName() + ". Your turn."));
        } else { 
            isMyTurn = false;
            SwingUtilities.invokeLater(() -> statusBar.setText("You are " + playerSeed.getDisplayName() + ". AI's turn."));
            
            new Timer(100, (e) -> { 
                ((Timer) ((ActionEvent) e).getSource()).stop(); 
                aiMakeMove(); 
            }).start();
        }
        updatePlayerNameLabels(); 
    }

    private void handleMouseClick(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();

        
        int boardStartX = (getWidth() - Board.CANVAS_WIDTH) / 2;
        int boardStartY = (getHeight() - Board.CANVAS_HEIGHT) / 2;
        int boardEndX = boardStartX + Board.CANVAS_WIDTH;
        int boardEndY = boardStartY + Board.CANVAS_HEIGHT;

        
        if (mouseX >= boardStartX && mouseX < boardEndX &&
            mouseY >= boardStartY && mouseY < boardEndY) {

            int row = (mouseY - boardStartY) / Cell.SIZE;
            int col = (mouseX - boardStartX) / Cell.SIZE;

            
            if (currentState == State.PLAYING && isMyTurn) {
                
                if (row >= 0 && row < Board.ROWS && col >= 0 && col < Board.COLS
                        && board.cells[row][col].content == Seed.NO_SEED) {
                    makeMove(row, col); 
                } else {
                    SwingUtilities.invokeLater(() -> statusBar.setText("Invalid move! Cell already occupied."));
                }
            } else if (currentState != State.PLAYING) {
                
                SwingUtilities.invokeLater(() -> statusBar.setText(currentState.getDisplayName() + "! Click Play Again to restart."));
            } else if (!isMyTurn) {
                
                SwingUtilities.invokeLater(() -> statusBar.setText("It's not your turn. Please wait."));
            }
        }
    }

    private void makeMove(int row, int col) { 
        
        if (currentState == State.PLAYING && board.cells[row][col].content == Seed.NO_SEED) {
            if (gameMode == GameMode.VS_AI) {
                
                board.cells[row][col].content = playerSeed; 
                SoundEffect.EAT_FOOD.play();
                currentState = board.stepGame(playerSeed, row, col); 

                GamePanel.this.invalidate();
                GamePanel.this.revalidate();
                repaint(); 

                if (currentState == State.PLAYING) {
                    
                    isMyTurn = false;
                    SwingUtilities.invokeLater(() -> statusBar.setText("AI's turn. Please wait."));
                    new Timer(700, (e) -> { 
                        ((Timer) ((ActionEvent) e).getSource()).stop();
                        aiMakeMove(); 
                    }).start();
                } else {
                    
                    updateScore(playerSeed); 
                    SwingUtilities.invokeLater(() -> statusBar.setText(currentState.getDisplayName() + "! Click Play Again to restart."));
                    isMyTurn = false;
                    updateActionButtonsVisibility(true); 
                }

            } else if (gameMode == GameMode.MULTIPLAYER) {
                
                gameClient.sendMove(row, col);
                isMyTurn = false; 
                SwingUtilities.invokeLater(() -> statusBar.setText("Waiting for opponent..."));
            }
        }
    }

    private void aiMakeMove() {
        if (currentState != State.PLAYING) return; 

        int[] bestMove = ai.findBestMove(board, aiSeed); 
        int aiRow = bestMove[0];
        int aiCol = bestMove[1];

        if (aiRow != -1 && aiCol != -1) { 
            board.cells[aiRow][aiCol].content = aiSeed; 
            SoundEffect.DIE.play(); 
            currentState = board.stepGame(aiSeed, aiRow, aiCol); 
            
            GamePanel.this.invalidate();
            GamePanel.this.revalidate();
            repaint(); 

            if (currentState == State.PLAYING) {
                
                isMyTurn = true;
                SwingUtilities.invokeLater(() -> statusBar.setText("Your turn (" + playerSeed.getDisplayName() + "). Make your move."));
            } else {
                
                updateScore(aiSeed); 
                SwingUtilities.invokeLater(() -> statusBar.setText(currentState.getDisplayName() + "! Click Play Again to restart."));
                isMyTurn = false;
                updateActionButtonsVisibility(true); 
            }
        } else {
            
            SwingUtilities.invokeLater(() -> statusBar.setText("AI couldn't find a move! (Error in AI logic or full board)"));
            currentState = State.DRAW; 
            repaint();
            updateScore(null); 
            updateActionButtonsVisibility(true);
        }
    }

    private void updateScore(Seed winner) {
        if (currentState == State.CROSS_WON) { 
            xScore++;
        } else if (currentState == State.NOUGHT_WON) { 
            oScore++;
        }
        
        updateScoreLabel(); 
    }

    private void resetScores() {
        xScore = 0;
        oScore = 0;
        updateScoreLabel(); 
        newGame(); 
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g); 

        
        if (backgroundImage != null) {
            g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(new Color(50, 50, 100));
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        
        if (gameMode != null) {
            
            int boardStartX = (getWidth() - Board.CANVAS_WIDTH) / 2;
            int boardStartY = (getHeight() - Board.CANVAS_HEIGHT) / 2;
            g.translate(boardStartX, boardStartY); 
            board.paint(g); 
            g.translate(-boardStartX, -boardStartY); 
        }

        
        if (gameMode == GameMode.VS_AI) {
            
            if (playerSeed != null && aiSeed != null && currentState == State.PLAYING) {
                statusBar.setForeground(Color.BLACK);
                statusBar.setText(isMyTurn ? "Your Turn (" + playerSeed.getDisplayName() + ")" : "AI's Turn (" + aiSeed.getDisplayName() + ")");
            } else if (currentState == State.PLAYING) {
                
                statusBar.setForeground(Color.BLACK);
                statusBar.setText("Choose your symbol (X or O).");
            }
        } else if (gameMode == GameMode.MULTIPLAYER) {
            if (currentState == State.PLAYING) {
                statusBar.setForeground(Color.BLACK);
                statusBar.setText(isMyTurn ? "Your Turn (" + playerRole + ")" : "Opponent's Turn");
            }
        }
        
        
        if (currentState != State.PLAYING) {
            statusBar.setForeground(Color.RED);
            statusBar.setText(currentState.getDisplayName() + "! Click Play Again to restart.");
        } else if (gameMode == null) {
            
            statusBar.setForeground(Color.BLACK);
            statusBar.setText("Welcome, " + loggedInUsername + "! Please select a game mode.");
        }
    }

    
    @Override
    public void onConnected(String message) {
        SwingUtilities.invokeLater(() -> statusBar.setText(message));
    }

    @Override
    public void onDisconnected(String message) {
        SwingUtilities.invokeLater(() -> {
            statusBar.setText(message);
            JOptionPane.showMessageDialog(this, message, "Disconnected", JOptionPane.WARNING_MESSAGE);
            revertToModeSelection(); 
        });
    }

    @Override
    public void onPlayerAssigned(String role) {
        this.playerRole = role;
        
        this.playerSeed = (role.equals("X")) ? Seed.CROSS : Seed.NOUGHT;
        this.aiSeed = (role.equals("X")) ? Seed.NOUGHT : Seed.CROSS; 
        SwingUtilities.invokeLater(() -> {
            statusBar.setText("You are Player " + playerRole + ". Waiting for opponent.");
            updatePlayerNameLabels(); 
        });
    }

    @Override
    public void onTurnChange(boolean myTurn, String playerSymbol) { 
        this.isMyTurn = myTurn;
        if (myTurn) {
            SoundEffect.EAT_FOOD.play();
            SwingUtilities.invokeLater(() -> statusBar.setText("Your turn (" + playerRole + "). Make your move."));
        } else {
            SwingUtilities.invokeLater(() -> statusBar.setText("Opponent's turn. Please wait."));
        }
    }

    @Override
    public void onMoveReceived(String playerSymbol, int row, int col) {
        Seed movedSeed = (playerSymbol.equals("X")) ? Seed.CROSS : Seed.NOUGHT;
        SwingUtilities.invokeLater(() -> {
            board.cells[row][col].content = movedSeed;
            repaint();
        });
        SoundEffect.DIE.play(); 
    }

    @Override
    public void onGameOver(State finalState) {
        SwingUtilities.invokeLater(() -> {
            currentState = finalState;
            repaint();
            isMyTurn = false; 
        });
        SoundEffect.DIE.play(); 

        
        if (finalState == State.CROSS_WON) {
            if (playerRole != null && playerRole.equals("X")) updateScore(Seed.CROSS); 
            else if (playerRole != null && playerRole.equals("O")) updateScore(Seed.CROSS); 
            
        } else if (finalState == State.NOUGHT_WON) {
            if (playerRole != null && playerRole.equals("O")) updateScore(Seed.NOUGHT); 
            else if (playerRole != null && playerRole.equals("X")) updateScore(Seed.NOUGHT); 
            
        }
        

        updateActionButtonsVisibility(true); 
    }

    @Override
    public void onInvalidMove(String message) {
        SwingUtilities.invokeLater(() -> statusBar.setText("Invalid Move: " + message));
        this.isMyTurn = true; 
    }

    @Override
    public void onMessage(String message) {
        SwingUtilities.invokeLater(() -> statusBar.setText(message));
    }

    @Override
    public void onConnectionError(String message) {
        SwingUtilities.invokeLater(() -> {
            statusBar.setText(message);
            JOptionPane.showMessageDialog(this, message, "Connection Error", JOptionPane.ERROR_MESSAGE);
            revertToModeSelection(); 
        });
    }

    private void revertToModeSelection() {
        if (gameClient != null && gameClient.isConnected()) {
            gameClient.closeConnection(); 
        }

        gameMode = null; 
        newGame(); 
        updateScoreLabel(); 
        updateActionButtonsVisibility(false); 

        
        playVsComputerButton.setVisible(true);
        playVsFriendButton.setVisible(true);

        revalidate(); 
        repaint(); 
        
    }
}
