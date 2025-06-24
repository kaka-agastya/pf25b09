// package MultiplayerManager; // Asumsi paket jika tidak ada di file.

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

/**
 * Kelas MultiplayerManager mengelola logika dan alur permainan dalam mode multiplayer.
 * Implementasi multiplayer ini menggunakan database sebagai perantara untuk
 * menyinkronkan state papan antara dua pemain (polling database).
 */
public class MultiplayerManager {
    // Referensi ke objek-objek inti game
    private final GameLogic gameLogic;
    private final GameUI gameUI;
    private final GamePanel gamePanel;
    private String gameId;         // ID unik untuk game multiplayer saat ini
    private int lastMoveNumber;    // Nomor langkah terakhir yang diketahui dari database

    /**
     * Konstruktor untuk MultiplayerManager.
     * Menginisialisasi referensi ke GameLogic, GameUI, dan GamePanel.
     */
    public MultiplayerManager(GameLogic gameLogic, GameUI gameUI, GamePanel gamePanel) {
        if (gameLogic == null || gameUI == null || gamePanel == null) {
            throw new IllegalArgumentException("GameLogic, GameUI, or GamePanel cannot be null");
        }
        this.gameLogic = gameLogic;
        this.gameUI = gameUI;
        this.gamePanel = gamePanel;
        this.lastMoveNumber = 0; // Awalnya, belum ada langkah yang diketahui
    }

    /**
     * Meminta pengguna untuk memilih apakah akan membuat game baru atau bergabung ke game yang sudah ada.
     * Mengelola ID game dan inisialisasi awal untuk mode multiplayer.
     */
    public void promptMultiplayerSetup() {
        gameUI.updateModeButtonsVisibility(false); // Sembunyikan tombol pilihan mode game
        String[] options = {"Create Game", "Join Game"}; // Opsi dialog

        // Menampilkan dialog pilihan
        int choice = JOptionPane.showOptionDialog(
                null,
                "Create a new game or join an existing one?",
                "Multiplayer Setup",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) { // Pengguna memilih "Create Game"
            gameId = JOptionPane.showInputDialog(null, "Enter Game ID for the new game:"); // Meminta ID game
            if (gameId == null || gameId.trim().isEmpty()) {
                gameUI.setStatusText("Game ID cannot be empty!");
                gameLogic.setGameMode(null); // Kembali ke mode tanpa game
                gameUI.showModeButtonsPanel(); // Tampilkan kembali tombol mode game
                return;
            }
            try {
                // Membersihkan langkah-langkah game lama untuk ID game ini (memulai game baru)
                DatabaseManager.clearGameMoves(gameId);
            } catch (SQLException | ClassNotFoundException e) {
                gameUI.setStatusText("Error clearing game moves: " + e.getMessage());
                System.err.println("MultiplayerManager: Error clearing game moves: " + e.getMessage());
                gameLogic.setGameMode(null);
                gameUI.showModeButtonsPanel();
                return;
            }

            gameLogic.setPlayerRole("X");    // Pembuat game adalah pemain 'X'
            gameLogic.setOpponentUsername("Waiting..."); // Lawan belum diketahui
            gameLogic.newGame();             // Memulai game baru
            gameLogic.setCurrentState(State.PLAYING); // Mengatur state ke PLAYING
            gameLogic.setMyTurn(true);       // Giliran pemain lokal
            gameUI.setStatusText("Game created! Your turn as X. Waiting for opponent..."); // Memperbarui status bar
            gameUI.updatePlayerNameLabels(); // Memperbarui label nama pemain
            startPollingForOpponentMove();   // Mulai polling untuk langkah lawan
            System.out.println("MultiplayerManager: Game created with ID: " + gameId);
        } else if (choice == 1) { // Pengguna memilih "Join Game"
            gameId = JOptionPane.showInputDialog(null, "Enter Game ID to join:"); // Meminta ID game
            if (gameId == null || gameId.trim().isEmpty()) {
                gameUI.setStatusText("Game ID cannot be empty!");
                gameLogic.setGameMode(null);
                gameUI.showModeButtonsPanel();
                return;
            }
            gameLogic.setPlayerRole("O");    // Bergabung sebagai pemain 'O'
            gameLogic.setOpponentUsername("Host"); // Lawan adalah pembuat game
            gameLogic.newGame();             // Memulai game baru
            gameLogic.setCurrentState(State.PLAYING);
            gameLogic.setMyTurn(false);      // Bukan giliran pemain lokal
            gameUI.setStatusText("Joined game as O. Waiting for opponent's move..."); // Memperbarui status bar
            gameUI.updatePlayerNameLabels();
            startPollingForOpponentMove();   // Mulai polling untuk langkah lawan
            System.out.println("MultiplayerManager: Joined game with ID: " + gameId);
        } else { // Pengguna membatalkan setup
            gameLogic.setGameMode(null);
            gameUI.showModeButtonsPanel();
            gameUI.setStatusText("Multiplayer setup cancelled.");
        }
    }

    /**
     * Dipanggil ketika pemain lokal membuat langkah pada sel (row, col).
     * Memperbarui papan lokal, menyimpan langkah ke database, dan melanjutkan polling lawan.
     */
    public void makeMove(int row, int col) {
        // Memeriksa inisialisasi GameLogic dan Board
        if (gameLogic.getCurrentState() == null || gameLogic.getBoard() == null) {
            gameUI.setStatusText("Error: Game state or board is not initialized.");
            System.err.println("MultiplayerManager: Game state or board is null in makeMove");
            return;
        }
        // Memvalidasi langkah: game sedang bermain, giliran pemain lokal, dan sel kosong
        if (gameLogic.getCurrentState() == State.PLAYING && gameLogic.isMyTurn() &&
            gameLogic.getBoard().cells[row][col].content == Seed.NO_SEED) {
            // Menentukan simbol pemain berdasarkan peran
            Seed playerSeed = gameLogic.getPlayerRole().equals("X") ? Seed.CROSS : Seed.NOUGHT;
            gameLogic.getBoard().cells[row][col].content = playerSeed; // Menetapkan simbol pemain ke sel
            SoundEffect.EAT_FOOD.play(); // Memainkan efek suara
            // Memperbarui state game setelah langkah pemain
            gameLogic.setCurrentState(gameLogic.getBoard().stepGame(playerSeed, row, col));

            // Memperbarui tampilan panel secara asinkron di EDT
            SwingUtilities.invokeLater(() -> {
                gamePanel.repaint(); // Menggambar ulang panel
                gamePanel.paintImmediately(gamePanel.getBounds()); // Memaksa gambar ulang segera
            });

            try {
                // Menyimpan langkah ke database
                DatabaseManager.insertMove(
                        gameId,
                        ++lastMoveNumber, // Meningkatkan nomor langkah
                        gameLogic.getLoggedInUsername(),
                        playerSeed.getDisplayName(),
                        row,
                        col
                );
                System.out.println("MultiplayerManager: Move sent: row=" + row + ", col=" + col + ", gameId=" + gameId + ", moveNumber=" + lastMoveNumber);
            } catch (SQLException | ClassNotFoundException e) {
                gameUI.setStatusText("Error saving move to database: " + e.getMessage());
                System.err.println("MultiplayerManager: Error inserting move: " + e.getMessage());
                return;
            }

            System.out.println("MultiplayerManager: Game state after player move: " + gameLogic.getCurrentState());
            if (gameLogic.getCurrentState() == State.PLAYING) {
                gameLogic.setMyTurn(false); // Mengatur giliran ke lawan
                gameUI.setStatusText("Waiting for opponent's move..."); // Memperbarui status bar
                startPollingForOpponentMove(); // Lanjutkan polling untuk langkah lawan
            } else {
                // Jika game berakhir
                gameLogic.updateScore(playerSeed); // Memperbarui skor
                gameUI.updateScoreLabel(); // Memperbarui label skor di UI
                gameUI.setStatusText(gameLogic.getCurrentState().getDisplayName() + "! Click Play Again to restart.");
                gameLogic.setMyTurn(false);
                gameUI.updateActionButtonsVisibility(true); // Menampilkan tombol aksi
                gameUI.showModeButtonsPanel(); // Tampilkan kembali tombol mode saat permainan berakhir
                System.out.println("MultiplayerManager: Setting action buttons visible: true");
                SoundEffect.WIN.play(); // Memainkan efek suara
            }
        } else {
            gameUI.setStatusText("Invalid move! Cell already occupied or not your turn."); // Pesan error
        }
    }

    /**
     * Memulai timer yang secara berkala memanggil checkForOpponentMove()
     * untuk memeriksa pergerakan lawan di database.
     */
    private void startPollingForOpponentMove() {
        // Membuat timer yang memicu setiap 500ms
        Timer timer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Menghentikan timer setelah dipicu (untuk polling sekali pemicu)
                // Namun, timer ini akan terus berjalan sampai dihentikan secara eksplisit oleh gameLogic.getCurrentState() != State.PLAYING
                // atau gameLogic.isMyTurn() dalam checkForOpponentMove()
                // ((Timer) e.getSource()).stop(); // Baris ini mungkin dihilangkan agar polling berkelanjutan
                checkForOpponentMove(); // Memanggil metode untuk memeriksa langkah lawan
            }
        });
        timer.start(); // Memulai timer
        System.out.println("MultiplayerManager: Started polling for opponent moves, gameId=" + gameId);
    }

    /**
     * Memeriksa database untuk langkah baru yang dibuat oleh lawan.
     * Jika ada, memperbarui papan lokal, state game, dan UI.
     * Dipanggil secara berkala oleh timer.
     */
    private void checkForOpponentMove() {
        // Keluar jika game tidak dalam state PLAYING atau sudah giliran pemain lokal
        if (gameLogic.getCurrentState() != State.PLAYING || gameLogic.isMyTurn()) {
            return;
        }
        try {
            // Mengambil langkah baru dari database
            List<DatabaseManager.Move> moves = DatabaseManager.fetchMoves(gameId, lastMoveNumber);
            if (!moves.isEmpty()) {
                DatabaseManager.Move latestMove = moves.get(moves.size() - 1); // Mengambil langkah terakhir
                lastMoveNumber = latestMove.moveNumber; // Memperbarui nomor langkah terakhir yang diketahui

                // Menentukan simbol lawan dan memperbarui sel papan
                Seed opponentSeed = latestMove.playerSeed.equals("X") ? Seed.CROSS : Seed.NOUGHT;
                gameLogic.getBoard().cells[latestMove.row][latestMove.col].content = opponentSeed;
                SoundEffect.TOY.play(); // Memainkan efek suara
                // Memperbarui state game setelah langkah lawan
                gameLogic.setCurrentState(gameLogic.getBoard().stepGame(opponentSeed, latestMove.row, latestMove.col));

                // Memperbarui tampilan panel secara asinkron di EDT
                SwingUtilities.invokeLater(() -> {
                    gamePanel.repaint();
                    gamePanel.paintImmediately(gamePanel.getBounds());
                });

                System.out.println("MultiplayerManager: Opponent move received: " + latestMove);
                if (gameLogic.getCurrentState() == State.PLAYING) {
                    gameLogic.setMyTurn(true); // Mengatur giliran ke pemain lokal
                    gameUI.setStatusText("Your turn (" + gameLogic.getPlayerRole() + ")."); // Memperbarui status bar
                } else {
                    // Jika game berakhir
                    gameLogic.updateScore(opponentSeed); // Memperbarui skor
                    gameUI.updateScoreLabel(); // Memperbarui label skor di UI
                    gameUI.setStatusText(gameLogic.getCurrentState().getDisplayName() + "! Click Play Again to restart.");
                    gameLogic.setMyTurn(false);
                    gameUI.updateActionButtonsVisibility(true); // Menampilkan tombol aksi
                    gameUI.showModeButtonsPanel(); // Tampilkan kembali tombol mode saat permainan berakhir
                    System.out.println("MultiplayerManager: Setting action buttons visible: true (Opponent move)");
                    SoundEffect.WIN.play();
                }
            } else {
                System.out.println("MultiplayerManager: No new moves found for gameId=" + gameId + ", lastMoveNumber=" + lastMoveNumber);
            }
        } catch (SQLException | ClassNotFoundException e) {
            gameUI.setStatusText("Error fetching opponent move: " + e.getMessage());
            System.err.println("MultiplayerManager: Error fetching moves: " + e.getMessage());
        }
    }
}