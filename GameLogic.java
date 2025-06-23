public class GameLogic {
    public enum GameMode {
        MULTIPLAYER,
        VS_AI
    }


    private Board board;
    private State currentState;
    private Seed currentPlayer;
    private GameMode gameMode;
    private String loggedInUsername;
    private int xScore;
    private int oScore;
    private boolean isMyTurn;
    private String playerRole;
    private String opponentUsername;
    private Seed playerSeed;
    private Seed aiSeed;
    private final GameUI gameUI;
    private final GamePanel gamePanel;


    public GameLogic(String username, GameUI gameUI, GamePanel gamePanel) {
        this.loggedInUsername = username;
        this.gameUI = gameUI;
        this.gamePanel = gamePanel;
        this.xScore = 0;
        this.oScore = 0;
        this.isMyTurn = false;
    }


    public void initGame() {
        board = new Board();
    }


    public void newGame() {
        board.newGame();
        currentState = State.PLAYING;
        currentPlayer = Seed.CROSS;
        isMyTurn = false;
        gameUI.updateScoreLabel();
        gameUI.updatePlayerNameLabels();
        gameUI.updateActionButtonsVisibility(false);
        gamePanel.revalidate();
        gamePanel.repaint();
        System.out.println("New game started, action buttons hidden");
        if (gameMode == GameMode.VS_AI) {
            gameUI.showFirstTurnPanelForVsAI();
        } else if (gameMode == GameMode.MULTIPLAYER) {
            gameUI.setStatusText("Welcome, " + loggedInUsername + "! Please select a game mode.");
        }
    }


    public void updateScore(Seed winner) {
        if (currentState == State.CROSS_WON) {
            xScore++;
        } else if (currentState == State.NOUGHT_WON) {
            oScore++;
        }
        gameUI.updateScoreLabel();
        System.out.println("Score updated: X=" + xScore + ", O=" + oScore);
    }


    public void resetScores() {
        xScore = 0;
        oScore = 0;
        newGame();
        gameUI.updateScoreLabel();
        System.out.println("Scores reset and new game started");
    }


    public Board getBoard() { return board; }
    public State getCurrentState() {
        System.out.println("Current state: " + currentState);
        return currentState;
    }
    public void setCurrentState(State state) { this.currentState = state; }
    public Seed getCurrentPlayer() { return currentPlayer; }
    public GameMode getGameMode() { return gameMode; }
    public void setGameMode(GameMode mode) { this.gameMode = mode; }
    public String getLoggedInUsername() { return loggedInUsername; }
    public int getXScore() { return xScore; }
    public int getOScore() { return oScore; }
    public boolean isMyTurn() { return isMyTurn; }
    public void setMyTurn(boolean myTurn) { this.isMyTurn = myTurn; }
    public String getPlayerRole() { return playerRole; }
    public void setPlayerRole(String role) { this.playerRole = role; }
    public String getOpponentUsername() { return opponentUsername; }
    public void setOpponentUsername(String username) { this.opponentUsername = username; }
    public Seed getPlayerSeed() { return playerSeed; }
    public void setPlayerSeed(Seed seed) { this.playerSeed = seed; }
    public Seed getAiSeed() { return aiSeed; }
    public void setAiSeed(Seed seed) { this.aiSeed = seed; }
}

