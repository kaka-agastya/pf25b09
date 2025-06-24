// package GameLogic; // Asumsi paket jika tidak ada di file.

/**
 * Kelas GameLogic adalah inti dari permainan Tic-Tac-Toe.
 * Ini mengelola state papan, pemain saat ini, state game (bermain, seri, menang),
 * skor, dan mode permainan (vs AI atau multiplayer).
 */
public class GameLogic {
    /**
     * Enum untuk mendefinisikan mode permainan yang berbeda.
     * MULTIPLAYER: Bermain melawan pemain lain (via database).
     * VS_AI: Bermain melawan kecerdasan buatan.
     */
    public enum GameMode {
        MULTIPLAYER,
        VS_AI
    }

    // Atribut-atribut utama yang mendefinisikan state game
    private Board board;            // Papan permainan Tic-Tac-Toe
    private State currentState;     // State game saat ini (PLAYING, DRAW, CROSS_WON, NOUGHT_WON)
    private Seed currentPlayer;     // Pemain yang gilirannya saat ini (CROSS atau NOUGHT)
    private GameMode gameMode;      // Mode permainan yang dipilih
    private String loggedInUsername; // Username dari pemain yang login
    private int xScore;             // Skor untuk pemain 'X'
    private int oScore;             // Skor untuk pemain 'O'
    private boolean isMyTurn;       // Menunjukkan apakah giliran pemain lokal (human)
    private String playerRole;      // Peran pemain lokal ("X" atau "O") dalam multiplayer
    private String opponentUsername; // Username lawan dalam multiplayer
    private Seed playerSeed;        // Simbol (Seed) yang dipilih oleh pemain lokal (dalam VS AI)
    private Seed aiSeed;            // Simbol (Seed) yang dipilih oleh AI (dalam VS AI)

    // Referensi ke objek UI dan Panel untuk pembaruan tampilan
    private final GameUI gameUI;
    private final GamePanel gamePanel;

    /**
     * Konstruktor untuk GameLogic.
     * Menginisialisasi objek GameLogic dengan username pemain yang login
     * dan referensi ke objek GameUI dan GamePanel.
     * @param username Username pemain yang login.
     * @param gameUI Objek GameUI untuk interaksi UI.
     * @param gamePanel Objek GamePanel untuk pembaruan visual.
     */
    public GameLogic(String username, GameUI gameUI, GamePanel gamePanel) {
        this.loggedInUsername = username;
        this.gameUI = gameUI;
        this.gamePanel = gamePanel;
        this.xScore = 0; // Inisialisasi skor X
        this.oScore = 0; // Inisialisasi skor O
        this.isMyTurn = false; // Awalnya bukan giliran pemain
    }

    /**
     * Menginisialisasi papan permainan.
     * Metode ini dipanggil sekali saat setup game awal.
     */
    public void initGame() {
        board = new Board(); // Membuat instance baru dari papan permainan
    }

    /**
     * Mengatur ulang game ke state awal untuk permainan baru.
     * Membersihkan papan, mengatur ulang state game, dan memperbarui UI.
     */
    public void newGame() {
        board.newGame(); // Membersihkan isi papan
        currentState = State.PLAYING; // Mengatur state game ke BERMAIN
        currentPlayer = Seed.CROSS; // Pemain CROSS (X) memulai duluan
        isMyTurn = false; // Awalnya bukan giliran pemain (akan diatur oleh AIManager/MultiplayerManager)
        gameUI.updateScoreLabel(); // Memperbarui tampilan skor di UI
        gameUI.updatePlayerNameLabels(); // Memperbarui tampilan nama pemain di UI
        gameUI.updateActionButtonsVisibility(false); // Menyembunyikan tombol aksi ("Play Again", "Reset Score")
        gamePanel.revalidate(); // Memvalidasi ulang layout panel
        gamePanel.repaint(); // Menggambar ulang panel
        System.out.println("New game started, action buttons hidden"); // Log ke konsol

        // Menampilkan panel yang relevan berdasarkan mode game
        if (gameMode == GameMode.VS_AI) {
            gameUI.showFirstTurnPanelForVsAI(); // Menampilkan panel pilihan giliran pertama untuk VS AI
        } else if (gameMode == GameMode.MULTIPLAYER) {
            // Untuk multiplayer, status bar akan diatur oleh MultiplayerManager
            gameUI.setStatusText("Welcome, " + loggedInUsername + "! Please select a game mode.");
        }
    }

    /**
     * Memperbarui skor berdasarkan pemenang game.
     * Dipanggil saat game berakhir dengan kemenangan.
     * @param winner Seed dari pemain yang memenangkan game.
     */
    public void updateScore(Seed winner) {
        if (currentState == State.CROSS_WON) {
            xScore++; // Menambahkan skor untuk X jika X menang
        } else if (currentState == State.NOUGHT_WON) {
            oScore++; // Menambahkan skor untuk O jika O menang
        }
        gameUI.updateScoreLabel(); // Memperbarui tampilan skor di UI
        System.out.println("Score updated: X=" + xScore + ", O=" + oScore); // Log skor
    }

    /**
     * Mengatur ulang semua skor ke nol dan memulai game baru.
     */
    public void resetScores() {
        xScore = 0; // Reset skor X
        oScore = 0; // Reset skor O
        newGame(); // Memulai game baru setelah skor direset
        gameUI.updateScoreLabel(); // Memperbarui tampilan skor di UI
        System.out.println("Scores reset and new game started"); // Log
    }

    // --- Getters dan Setters untuk atribut-atribut GameLogic ---

    public Board getBoard() { return board; } // Mengembalikan objek papan
    public State getCurrentState() {
        System.out.println("Current state: " + currentState); // Log state saat ini
        return currentState;
    }
    public void setCurrentState(State state) { this.currentState = state; } // Mengatur state game
    public Seed getCurrentPlayer() { return currentPlayer; } // Mengembalikan pemain yang gilirannya saat ini
    public GameMode getGameMode() { return gameMode; } // Mengembalikan mode game saat ini
    public void setGameMode(GameMode mode) { this.gameMode = mode; } // Mengatur mode game
    public String getLoggedInUsername() { return loggedInUsername; } // Mengembalikan username yang login
    public int getXScore() { return xScore; } // Mengembalikan skor X
    public int getOScore() { return oScore; } // Mengembalikan skor O
    public boolean isMyTurn() { return isMyTurn; } // Mengembalikan apakah giliran pemain lokal
    public void setMyTurn(boolean myTurn) { this.isMyTurn = myTurn; } // Mengatur giliran pemain lokal
    public String getPlayerRole() { return playerRole; } // Mengembalikan peran pemain lokal (X/O) dalam multiplayer
    public void setPlayerRole(String role) { this.playerRole = role; } // Mengatur peran pemain lokal
    public String getOpponentUsername() { return opponentUsername; } // Mengembalikan username lawan dalam multiplayer
    public void setOpponentUsername(String username) { this.opponentUsername = username; } // Mengatur username lawan
    public Seed getPlayerSeed() { return playerSeed; } // Mengembalikan simbol pemain lokal (dalam VS AI)
    public void setPlayerSeed(Seed seed) { this.playerSeed = seed; } // Mengatur simbol pemain lokal
    public Seed getAiSeed() { return aiSeed; } // Mengembalikan simbol AI (dalam VS AI)
    public void setAiSeed(Seed seed) { this.aiSeed = seed; } // Mengatur simbol AI
}