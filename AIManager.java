// package AIManager; // Asumsi paket jika tidak ada di file.

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Kelas AIManager mengelola logika dan alur permainan ketika bermain melawan AI (mode single player).
 * Ini bertanggung jawab untuk memulai game AI, mengatur giliran pemain/AI,
 * dan memicu AI untuk membuat pergerakan setelah giliran pemain manusia.
 */
public class AIManager {
    // Referensi ke objek-objek inti game
    private final GameLogic gameLogic;
    private final GameUI gameUI;
    private final GamePanel gamePanel;
    private TicTacToeAI ai; // Objek yang berisi algoritma AI

    /**
     * Konstruktor untuk AIManager.
     * Menginisialisasi referensi ke GameLogic, GameUI, dan GamePanel.
     */
    public AIManager(GameLogic gameLogic, GameUI gameUI, GamePanel gamePanel) {
        if (gameLogic == null || gameUI == null || gamePanel == null) {
            throw new IllegalArgumentException("GameLogic, GameUI, or GamePanel cannot be null");
        }
        this.gameLogic = gameLogic;
        this.gameUI = gameUI;
        this.gamePanel = gamePanel;
    }

    /**
     * Mengatur simbol (Seed) yang akan digunakan oleh pemain manusia.
     */
    public void setPlayerSeed(Seed seed) {
        gameLogic.setPlayerSeed(seed);
    }

    /**
     * Mengatur simbol (Seed) yang akan digunakan oleh AI.
     */
    public void setAiSeed(Seed seed) {
        gameLogic.setAiSeed(seed);
    }

    /**
     * Memulai permainan melawan AI.
     * Menentukan siapa yang memulai duluan (pemain atau AI) dan mengatur UI yang sesuai.
     */
    public void startVsAIGame(boolean humanStarts) {
        gameUI.hideFirstTurnPanel(); // Menyembunyikan panel pilihan giliran pertama
        ai = new TicTacToeAI(); // Membuat instance baru dari algoritma AI

        // Mendapatkan nama tampilan simbol pemain
        String playerSeedDisplay = gameLogic.getPlayerSeed() != null ? gameLogic.getPlayerSeed().getDisplayName() : "Unknown";
        if (humanStarts) {
            gameLogic.setMyTurn(true); // Mengatur giliran pemain manusia
            gameUI.setStatusText("You are " + playerSeedDisplay + ". Your turn."); // Memperbarui status bar
        } else {
            gameLogic.setMyTurn(false); // Mengatur giliran AI
            gameUI.setStatusText("You are " + playerSeedDisplay + ". AI's turn."); // Memperbarui status bar
            // Memulai timer untuk memicu langkah AI setelah penundaan singkat
            Timer timer = new Timer(100, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ((Timer) e.getSource()).stop(); // Menghentikan timer setelah dipicu
                    System.out.println("Timer triggered: Calling aiMakeMove() in startVsAIGame");
                    aiMakeMove(); // Memanggil metode langkah AI
                }
            });
            timer.start(); // Memulai timer
        }
        gameUI.updatePlayerNameLabels(); // Memperbarui label nama pemain di UI
    }

    /**
     * Dipanggil ketika pemain manusia membuat langkah pada sel (row, col).
     * Memperbarui papan, memeriksa state game, dan memicu langkah AI jika game berlanjut.
     */
    public void makeMove(int row, int col) {
        // Memeriksa inisialisasi GameLogic dan Board
        if (gameLogic.getCurrentState() == null || gameLogic.getBoard() == null) {
            gameUI.setStatusText("Error: Game state or board is not initialized.");
            System.err.println("GameLogic state or board is null in makeMove");
            return;
        }
        // Memvalidasi langkah: game sedang bermain dan sel kosong
        if (gameLogic.getCurrentState() == State.PLAYING &&
                gameLogic.getBoard().cells[row][col].content == Seed.NO_SEED) {
            gameLogic.getBoard().cells[row][col].content = gameLogic.getPlayerSeed(); // Menetapkan simbol pemain ke sel
            SoundEffect.EAT_FOOD.play(); // Memainkan efek suara
            // Memperbarui state game setelah langkah pemain
            gameLogic.setCurrentState(gameLogic.getBoard().stepGame(gameLogic.getPlayerSeed(), row, col));

            // Memperbarui tampilan panel secara asinkron di EDT
            SwingUtilities.invokeLater(() -> {
                gamePanel.repaint(); // Menggambar ulang panel
                gamePanel.paintImmediately(gamePanel.getBounds()); // Memaksa gambar ulang segera
            });

            System.out.println("Game state after player move: " + gameLogic.getCurrentState()); // Log state game
            if (gameLogic.getCurrentState() == State.PLAYING) {
                gameLogic.setMyTurn(false); // Mengatur giliran ke AI
                gameUI.setStatusText("AI's turn. Please wait."); // Memperbarui status bar
                // Memulai timer untuk memicu langkah AI
                Timer timer = new Timer(100, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ((Timer) e.getSource()).stop(); // Menghentikan timer
                        System.out.println("Timer triggered: Calling aiMakeMove() in makeMove");
                        aiMakeMove(); // Memanggil metode langkah AI
                    }
                });
                timer.start(); // Memulai timer
            } else {
                // Jika game berakhir
                gameLogic.updateScore(gameLogic.getPlayerSeed()); // Memperbarui skor
                gameUI.updateScoreLabel(); // Memperbarui label skor di UI
                gameUI.setStatusText(gameLogic.getCurrentState().getDisplayName() + "! Click Play Again to restart."); // Memperbarui status bar
                gameLogic.setMyTurn(false); // Memastikan giliran bukan pemain lagi
                gameUI.updateActionButtonsVisibility(true); // Menampilkan tombol aksi
                System.out.println("Setting action buttons visible: true");
                SoundEffect.WIN.play(); // Memainkan efek suara
            }
        } else {
            gameUI.setStatusText("Invalid move! Cell already occupied or game not in progress."); // Pesan error
        }
    }

    /**
     * Memicu AI untuk membuat langkah.
     * Metode ini dipanggil oleh timer setelah giliran pemain manusia.
     */
    private void aiMakeMove() {
        // Keluar jika game tidak dalam state PLAYING
        if (gameLogic.getCurrentState() != State.PLAYING) {
            System.out.println("aiMakeMove: Game not in PLAYING state, exiting.");
            return;
        }

        // Penanganan error jika AI tidak diinisialisasi
        if (ai == null) {
            gameUI.setStatusText("Error: AI not initialized!");
            System.err.println("AI is null in aiMakeMove");
            gameLogic.setCurrentState(State.DRAW); // Game dianggap seri
            SwingUtilities.invokeLater(() -> {
                gamePanel.repaint();
                gamePanel.paintImmediately(gamePanel.getBounds());
            });
            gameUI.updateScoreLabel();
            gameUI.updateActionButtonsVisibility(true);
            System.out.println("Setting action buttons visible: true (AI null)");
            return;
        }

        // Meminta AI untuk menemukan langkah terbaik
        int[] bestMove = ai.findBestMove(gameLogic.getBoard(), gameLogic.getAiSeed());
        // Penanganan error jika langkah AI tidak valid
        if (bestMove == null || bestMove.length < 2 || (bestMove[0] == -1 && bestMove[1] == -1)) {
            gameUI.setStatusText("AI couldn't find a valid move! (Error in AI logic or full board)");
            System.err.println("Invalid bestMove from AI: " + (bestMove == null ? "null" : "length=" + bestMove.length));
            gameLogic.setCurrentState(State.DRAW); // Game dianggap seri
            SwingUtilities.invokeLater(() -> {
                gamePanel.repaint();
                gamePanel.paintImmediately(gamePanel.getBounds());
            });
            gameUI.updateScoreLabel();
            gameUI.updateActionButtonsVisibility(true);
            System.out.println("Setting action buttons visible: true (Invalid AI move)");
            return;
        }

        int aiRow = bestMove[0]; // Baris langkah AI
        int aiCol = bestMove[1]; // Kolom langkah AI

        // Jika langkah AI valid
        if (aiRow != -1 && aiCol != -1) {
            System.out.println("AI move: Row=" + aiRow + ", Col=" + aiCol + ", Seed=" + gameLogic.getAiSeed());
            gameLogic.getBoard().cells[aiRow][aiCol].content = gameLogic.getAiSeed(); // Menetapkan simbol AI ke sel
            SoundEffect.TOY.play(); // Memainkan efek suara
            // Memperbarui state game setelah langkah AI
            gameLogic.setCurrentState(gameLogic.getBoard().stepGame(gameLogic.getAiSeed(), aiRow, aiCol));

            // Memperbarui tampilan panel secara asinkron di EDT
            SwingUtilities.invokeLater(() -> {
                gamePanel.repaint();
                gamePanel.paintImmediately(gamePanel.getBounds());
            });

            System.out.println("Game state after AI move: " + gameLogic.getCurrentState()); // Log state game
            if (gameLogic.getCurrentState() == State.PLAYING) {
                gameLogic.setMyTurn(true); // Mengatur giliran ke pemain manusia
                String playerSeedDisplay = gameLogic.getPlayerSeed() != null ? gameLogic.getPlayerSeed().getDisplayName() : "Unknown";
                gameUI.setStatusText("Your turn (" + playerSeedDisplay + "). Make your move."); // Memperbarui status bar
            } else {
                // Jika game berakhir
                gameLogic.updateScore(gameLogic.getAiSeed()); // Memperbarui skor
                gameUI.updateScoreLabel(); // Memperbarui label skor di UI
                gameUI.setStatusText(gameLogic.getCurrentState().getDisplayName() + "! Click Play Again to restart."); // Memperbarui status bar
                gameLogic.setMyTurn(false); // Memastikan giliran bukan pemain lagi
                gameUI.updateActionButtonsVisibility(true); // Menampilkan tombol aksi
                System.out.println("Setting action buttons visible: true (Game over)");
                SoundEffect.WIN.play(); // Memainkan efek suara
            }
        } else {
            // Jika AI tidak dapat menemukan langkah yang valid
            gameUI.setStatusText("AI couldn't find a move! (Error in AI logic or full board)");
            gameLogic.setCurrentState(State.DRAW); // Game dianggap seri
            SwingUtilities.invokeLater(() -> {
                gamePanel.repaint();
                gamePanel.paintImmediately(gamePanel.getBounds());
            });
            gameUI.updateScoreLabel();
            gameUI.updateActionButtonsVisibility(true);
            System.out.println("Setting action buttons visible: true (No valid AI move)");
        }
    }
}