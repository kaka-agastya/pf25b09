import javax.swing.*;
import java.awt.*;


public class GameUI {
    public static final Color COLOR_CROSS = new Color(239, 105, 80);
    public static final Color COLOR_NOUGHT = new Color(64, 154, 225);
    public static final Color COLOR_BG_STATUS = new Color(0, 0, 0, 255);
    public static final Font FONT_STATUS = new Font("OCR A Extended", Font.PLAIN, 14);


    private GameLogic gameLogic;
    private JPanel topPanel;
    private JLabel titleLabel;
    private JLabel playerXNameLabel;
    private JLabel playerONameLabel;
    private JLabel scoreLabel;
    private JButton playVsComputerButton;
    private JButton playVsFriendButton;
    private JPanel modeButtonsPanel;
    private JPanel seedSelectionPanel;
    private JButton selectXButton;
    private JButton selectOButton;
    private JPanel firstTurnSelectionPanel;
    private JButton playerStartsButton;
    private JButton aiStartsButton;
    private JPanel bottomPanel;
    private JButton playAgainButton;
    private JButton resetScoreButton;
    private JLabel statusBar;


    public GameUI() {
        initUI();
    }


    private void initUI() {
        topPanel = new JPanel();
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


        scoreLabel = new JLabel("0 | 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 24));
        scoreLabel.setForeground(Color.WHITE);


        playerONameLabel = new JLabel("Player O: -");
        playerONameLabel.setFont(new Font("Arial", Font.BOLD, 20));
        playerONameLabel.setForeground(COLOR_NOUGHT);


        playerNamesPanel.add(playerXNameLabel);
        playerNamesPanel.add(scoreLabel);
        playerNamesPanel.add(playerONameLabel);


        modeButtonsPanel = new JPanel();
        modeButtonsPanel.setOpaque(false);
        modeButtonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 80)); //Button awaL


        playVsComputerButton = new JButton("Play vs Computer");
        styleButton(playVsComputerButton, new Color(64, 154, 225));


        playVsFriendButton = new JButton("Play vs Friend");
        styleButton(playVsFriendButton, new Color(239, 105, 80));


        modeButtonsPanel.add(playVsComputerButton);
        modeButtonsPanel.add(playVsFriendButton);


        topPanel.add(titleLabel);
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(playerNamesPanel);
        topPanel.add(Box.createVerticalStrut(120)); //Button choose X or O and Start mode
        topPanel.add(modeButtonsPanel);


        bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 100));


        playAgainButton = new JButton("Play Again");
        styleButton(playAgainButton, new Color(239, 105, 80));
        playAgainButton.setVisible(false);


        resetScoreButton = new JButton("Reset Score");
        styleButton(resetScoreButton, new Color(239, 105, 80));
        resetScoreButton.setVisible(false);


        bottomPanel.add(playAgainButton);
        bottomPanel.add(resetScoreButton);


        statusBar = new JLabel();
        statusBar.setFont(FONT_STATUS);
        statusBar.setBackground(COLOR_BG_STATUS);
        statusBar.setOpaque(true);
        statusBar.setPreferredSize(new Dimension(300, 30));
        statusBar.setHorizontalAlignment(JLabel.CENTER);
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 12));


        createSeedSelectionPanel();
        createFirstTurnSelectionPanel();
    }


    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("Arial", Font.BOLD, 30)); //Button awal
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setBorder(BorderFactory.createLineBorder(new Color(40, 120, 180), 2, true));
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


    private void createSeedSelectionPanel() {
        seedSelectionPanel = new JPanel();
        seedSelectionPanel.setOpaque(false);
        seedSelectionPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        seedSelectionPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));


        selectXButton = new JButton("Play as X");
        styleButton(selectXButton, COLOR_CROSS);


        selectOButton = new JButton("Play as O");
        styleButton(selectOButton, COLOR_NOUGHT);


        seedSelectionPanel.add(selectXButton);
        seedSelectionPanel.add(selectOButton);
    }


    private void createFirstTurnSelectionPanel() {
        firstTurnSelectionPanel = new JPanel();
        firstTurnSelectionPanel.setOpaque(false);
        firstTurnSelectionPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        firstTurnSelectionPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));


        playerStartsButton = new JButton("You Start");
        styleTurnButton(playerStartsButton, COLOR_NOUGHT);


        aiStartsButton = new JButton("AI Starts");
        styleTurnButton(aiStartsButton, COLOR_CROSS);


        firstTurnSelectionPanel.add(playerStartsButton);
        firstTurnSelectionPanel.add(aiStartsButton);
    }


    public void updateScoreLabel() {
        if (gameLogic == null) {
            System.err.println("GameLogic is null in updateScoreLabel");
            return;
        }
        SwingUtilities.invokeLater(() -> scoreLabel.setText(gameLogic.getXScore() + " | " + gameLogic.getOScore()));
    }


    public void updatePlayerNameLabels() {
        if (gameLogic == null) {
            System.err.println("GameLogic is null in updatePlayerNameLabels");
            return;
        }
        SwingUtilities.invokeLater(() -> {
            if (gameLogic.getGameMode() == GameLogic.GameMode.VS_AI) {
                Seed playerSeed = gameLogic.getPlayerSeed();
                if (playerSeed != null) {
                    playerXNameLabel.setText("Player X: " + (playerSeed == Seed.CROSS ? gameLogic.getLoggedInUsername() : "Computer"));
                    playerONameLabel.setText("Player O: " + (playerSeed == Seed.NOUGHT ? gameLogic.getLoggedInUsername() : "Computer"));
                } else {
                    playerXNameLabel.setText("Player X: (You)");
                    playerONameLabel.setText("Player O: (Computer)");
                }
            } else if (gameLogic.getGameMode() == GameLogic.GameMode.MULTIPLAYER) {
                String playerRole = gameLogic.getPlayerRole();
                String opponentUsername = gameLogic.getOpponentUsername();
                if (playerRole != null) {
                    if (playerRole.equals("X")) {
                        playerXNameLabel.setText("Player X: " + gameLogic.getLoggedInUsername());
                        playerONameLabel.setText("Player O: Opponent (" + (opponentUsername != null ? opponentUsername : "-") + ")");
                    } else {
                        playerXNameLabel.setText("Player X: Opponent (" + (opponentUsername != null ? opponentUsername : "-") + ")");
                        playerONameLabel.setText("Player O: " + gameLogic.getLoggedInUsername());
                    }
                } else {
                    playerXNameLabel.setText("Player X: -");
                    playerONameLabel.setText("Player O: -");
                }
            } else {
                playerXNameLabel.setText("Player X: -");
                playerONameLabel.setText("Player O: -");
            }
        });
    }


    public void updateActionButtonsVisibility(boolean visible) {
        SwingUtilities.invokeLater(() -> {
            if (playAgainButton == null || resetScoreButton == null) {
                System.err.println("playAgainButton or resetScoreButton is null in updateActionButtonsVisibility");
                return;
            }
            playAgainButton.setVisible(visible);
            resetScoreButton.setVisible(visible);
            bottomPanel.revalidate();
            bottomPanel.repaint();
            System.out.println("Action buttons visibility set to: " + visible);
        });
    }


    public void showSeedSelectionPanel() {
        modeButtonsPanel.setVisible(false);
        topPanel.getParent().add(seedSelectionPanel, BorderLayout.CENTER);
        topPanel.getParent().revalidate();
        topPanel.getParent().repaint();
        setStatusText("Choose your symbol (X or O):");
        updatePlayerNameLabels();
    }


    public void showFirstTurnPanelForVsAI() {
        topPanel.getParent().remove(seedSelectionPanel);
        topPanel.getParent().add(firstTurnSelectionPanel, BorderLayout.CENTER);
        topPanel.getParent().revalidate();
        topPanel.getParent().repaint();
        setStatusText("Who makes the first move?");
        updatePlayerNameLabels();
    }


    public void hideFirstTurnPanel() {
        topPanel.getParent().remove(firstTurnSelectionPanel);
        topPanel.getParent().revalidate();
        topPanel.getParent().repaint();
    }


    public void showModeButtonsPanel() {
        modeButtonsPanel.setVisible(true);
        topPanel.getParent().revalidate();
        topPanel.getParent().repaint();
    }


    public void setStatusText(String text) {
        SwingUtilities.invokeLater(() -> statusBar.setText(text));
    }


    public void setGameLogic(GameLogic gameLogic) {
        this.gameLogic = gameLogic;
    }


    public JPanel getTopPanel() { return topPanel; }
    public JPanel getBottomPanel() { return bottomPanel; }
    public JButton getPlayVsComputerButton() { return playVsComputerButton; }
    public JButton getPlayVsFriendButton() { return playVsFriendButton; }
    public JButton getPlayAgainButton() { return playAgainButton; }
    public JButton getResetScoreButton() { return resetScoreButton; }
    public JButton getSelectXButton() { return selectXButton; }
    public JButton getSelectOButton() { return selectOButton; }
    public JButton getPlayerStartsButton() { return playerStartsButton; }
    public JButton getAiStartsButton() { return aiStartsButton; }
    public JLabel getStatusBar() { return statusBar; }
}

