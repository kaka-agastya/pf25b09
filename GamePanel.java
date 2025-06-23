import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.URL;


public class GamePanel extends JPanel implements GameClientListener {
    private static final long serialVersionUID = 1L;


    public static final String TITLE = "Tic Tac Toe";
    public static final Color COLOR_BG_STATUS = new Color(0, 0, 0, 255);


    private ImageIcon backgroundImage;
    private GameUI gameUI;
    private GameLogic gameLogic;
    private MultiplayerManager multiplayerManager;
    private AIManager aiManager;


    public GamePanel(String username) {
        gameUI = new GameUI();
        gameLogic = new GameLogic(username, gameUI, this);
        gameUI.setGameLogic(gameLogic); // Set GameLogic di GameUI
        multiplayerManager = new MultiplayerManager(gameLogic, gameUI, this);
        aiManager = new AIManager(gameLogic, gameUI, this);


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


        add(gameUI.getTopPanel(), BorderLayout.PAGE_START);
        add(gameUI.getBottomPanel(), BorderLayout.PAGE_END);
        add(gameUI.getStatusBar(), BorderLayout.SOUTH); // Status bar di bagian bawah
        addMouseListenerToGameBoard();


        addListeners();


        gameLogic.initGame();
        gameLogic.newGame();
    }


    private void addListeners() {
        gameUI.getPlayVsComputerButton().addActionListener(e -> {
            gameLogic.setGameMode(GameLogic.GameMode.VS_AI);
            gameUI.showSeedSelectionPanel();
        });
        gameUI.getPlayVsFriendButton().addActionListener(e -> {
            gameLogic.setGameMode(GameLogic.GameMode.MULTIPLAYER);
            multiplayerManager.promptMultiplayerSetup();
        });
        gameUI.getPlayAgainButton().addActionListener(e -> {
            System.out.println("Play Again button clicked");
            gameLogic.newGame();
        });
        gameUI.getResetScoreButton().addActionListener(e -> {
            System.out.println("Reset Score button clicked");
            gameLogic.resetScores();
        });
        gameUI.getSelectXButton().addActionListener(e -> {
            aiManager.setPlayerSeed(Seed.CROSS);
            aiManager.setAiSeed(Seed.NOUGHT);
            gameUI.showFirstTurnPanelForVsAI();
        });
        gameUI.getSelectOButton().addActionListener(e -> {
            aiManager.setPlayerSeed(Seed.NOUGHT);
            aiManager.setAiSeed(Seed.CROSS);
            gameUI.showFirstTurnPanelForVsAI();
        });
        gameUI.getPlayerStartsButton().addActionListener(e -> aiManager.startVsAIGame(true));
        gameUI.getAiStartsButton().addActionListener(e -> aiManager.startVsAIGame(false));
    }


    private void addMouseListenerToGameBoard() {
        MouseListener[] listeners = getMouseListeners();
        for (MouseListener l : listeners) {
            removeMouseListener(l);
        }
        super.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e);
            }
        });
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


            if (gameLogic.getCurrentState() == State.PLAYING && gameLogic.isMyTurn()) {
                if (row >= 0 && row < Board.ROWS && col >= 0 && col < Board.COLS
                        && gameLogic.getBoard().cells[row][col].content == Seed.NO_SEED) {
                    if (gameLogic.getGameMode() == GameLogic.GameMode.VS_AI) {
                        aiManager.makeMove(row, col);
                    } else if (gameLogic.getGameMode() == GameLogic.GameMode.MULTIPLAYER) {
                        multiplayerManager.makeMove(row, col);
                    }
                } else {
                    gameUI.setStatusText("Invalid move! Cell already occupied.");
                }
            } else if (gameLogic.getCurrentState() != State.PLAYING) {
                gameUI.setStatusText(gameLogic.getCurrentState().getDisplayName() + "! Click Play Again to restart.");
                gameUI.updateActionButtonsVisibility(true); // Pastikan tombol muncul saat permainan berakhir
            } else if (!gameLogic.isMyTurn()) {
                gameUI.setStatusText("It's not your turn. Please wait.");
            }
        }
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


        if (gameLogic.getGameMode() != null) {
            int boardStartX = (getWidth() - Board.CANVAS_WIDTH) / 2;
            int boardStartY = (getHeight() - Board.CANVAS_HEIGHT) / 2;
            g.translate(boardStartX, boardStartY);
            gameLogic.getBoard().paint(g);
            g.translate(-boardStartX, -boardStartY);
        }


        if (gameLogic.getGameMode() == GameLogic.GameMode.VS_AI) {
            if (gameLogic.getPlayerSeed() != null && gameLogic.getAiSeed() != null && gameLogic.getCurrentState() == State.PLAYING) {
                gameUI.getStatusBar().setForeground(Color.BLACK);
                gameUI.setStatusText(gameLogic.isMyTurn() ? "Your Turn (" + gameLogic.getPlayerSeed().getDisplayName() + ")" : "AI's Turn (" + gameLogic.getAiSeed().getDisplayName() + ")");
            } else if (gameLogic.getCurrentState() == State.PLAYING) {
                gameUI.getStatusBar().setForeground(Color.BLACK);
                gameUI.setStatusText("Choose your symbol (X or O).");
            }
        } else if (gameLogic.getGameMode() == GameLogic.GameMode.MULTIPLAYER) {
            if (gameLogic.getCurrentState() == State.PLAYING) {
                gameUI.getStatusBar().setForeground(Color.BLACK);
                gameUI.setStatusText(gameLogic.isMyTurn() ? "Your Turn (" + gameLogic.getPlayerRole() + ")" : "Opponent's Turn");
            }
        }


        if (gameLogic.getCurrentState() != State.PLAYING) {
            gameUI.getStatusBar().setForeground(Color.RED);
            gameUI.setStatusText(gameLogic.getCurrentState().getDisplayName() + "! Click Play Again to restart.");
            gameUI.updateActionButtonsVisibility(true); // Pastikan tombol muncul saat permainan berakhir
        } else if (gameLogic.getGameMode() == null) {
            gameUI.getStatusBar().setForeground(Color.BLACK);
            gameUI.setStatusText("Welcome, " + gameLogic.getLoggedInUsername() + "! Please select a game mode.");
        }
    }


    @Override
    public void onConnected(String message) {}
    @Override
    public void onDisconnected(String message) {}
    @Override
    public void onPlayerAssigned(String role) {}
    @Override
    public void onTurnChange(boolean myTurn, String playerSymbol) {}
    @Override
    public void onMoveReceived(String playerSymbol, int row, int col) {}
    @Override
    public void onGameOver(State finalState) {}
    @Override
    public void onInvalidMove(String message) {}
    @Override
    public void onMessage(String message) {}
    @Override
    public void onConnectionError(String message) {}
}

