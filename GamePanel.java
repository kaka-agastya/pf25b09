// package GamePanel; // Asumsi paket jika tidak ada di file.

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.URL;

/**
 * Kelas GamePanel adalah panel GUI utama untuk game Tic-Tac-Toe.
 * Ini berfungsi sebagai kontainer untuk semua komponen UI lainnya (GameUI)
 * dan juga menangani interaksi pengguna langsung dengan papan permainan (klik mouse).
 * Ini adalah jembatan antara tampilan grafis (GUI) dan logika game.
 */
public class GamePanel extends JPanel implements GameClientListener {
    private static final long serialVersionUID = 1L; // Untuk menghindari peringatan serializable

    // Konstanta untuk judul jendela dan warna status bar
    public static final String TITLE = "Tic Tac Toe";
    public static final Color COLOR_BG_STATUS = new Color(0, 0, 0, 255); // Warna hitam penuh untuk status bar

    // Atribut untuk komponen UI dan objek manajemen game
    private ImageIcon backgroundImage;          // Gambar latar belakang panel
    private GameUI gameUI;                      // Objek untuk mengelola elemen visual UI
    private GameLogic gameLogic;                // Objek untuk mengelola logika permainan
    private MultiplayerManager multiplayerManager; // Objek untuk mengelola mode multiplayer
    private AIManager aiManager;                // Objek untuk mengelola mode vs AI

    /**
     * Konstruktor untuk GamePanel.
     * Menginisialisasi semua objek manajer game dan menyiapkan tampilan panel.
     */
    public GamePanel(String username) {
        // Inisialisasi objek-objek manajer game
        gameUI = new GameUI();
        gameLogic = new GameLogic(username, gameUI, this); // Meneruskan gameUI dan GamePanel ke GameLogic
        gameUI.setGameLogic(gameLogic); // Mengatur GameLogic di GameUI
        multiplayerManager = new MultiplayerManager(gameLogic, gameUI, this); // Meneruskan objek yang diperlukan
        aiManager = new AIManager(gameLogic, gameUI, this); // Meneruskan objek yang diperlukan

        // Memuat gambar latar belakang
        try {
            URL bgURL = getClass().getClassLoader().getResource("images/Super Mario Background .jpeg");
            if (bgURL != null) {
                backgroundImage = new ImageIcon(bgURL);
            } else {
                System.err.println("Background image not found: images/Super Mario Background .jpeg");
                setBackground(new Color(50, 50, 100)); // Warna solid jika gambar tidak ditemukan
            }
        } catch (Exception e) {
            System.err.println("Error loading background image: " + e.getMessage());
            setBackground(new Color(50, 50, 100)); // Warna solid jika ada error saat memuat
        }

        // Mengatur tata letak dan properti panel utama
        super.setLayout(new BorderLayout()); // Menggunakan BorderLayout
        super.setPreferredSize(new Dimension(Board.CANVAS_WIDTH + 300, Board.CANVAS_HEIGHT + 300)); // Ukuran preferensi
        super.setBorder(BorderFactory.createLineBorder(COLOR_BG_STATUS, 2, false)); // Border di sekitar panel
        super.setOpaque(false); // Mengatur panel tidak opak agar gambar latar belakang terlihat

        // Menambahkan panel UI dari GameUI ke GamePanel
        add(gameUI.getTopPanel(), BorderLayout.PAGE_START);    // Panel atas (judul, skor, tombol mode)
        add(gameUI.getBottomPanel(), BorderLayout.PAGE_END); // Panel bawah (tombol play again, reset score)
        add(gameUI.getStatusBar(), BorderLayout.SOUTH);        // Status bar di bagian bawah

        // Menambahkan mouse listener untuk interaksi dengan papan game
        addMouseListenerToGameBoard();
        // Menambahkan action listeners untuk semua tombol UI
        addListeners();

        // Menginisialisasi dan memulai game baru melalui GameLogic
        gameLogic.initGame();
        gameLogic.newGame();
    }

    /**
     * Menambahkan action listeners ke semua tombol UI yang dikelola oleh GameUI.
     * Listener ini memicu aksi yang sesuai di GameLogic, AIManager, atau MultiplayerManager.
     */
    private void addListeners() {
        // Listener untuk tombol "Play vs Computer"
        gameUI.getPlayVsComputerButton().addActionListener(e -> {
            gameLogic.setGameMode(GameLogic.GameMode.VS_AI); // Mengatur mode game ke VS_AI
            gameUI.showSeedSelectionPanel(); // Menampilkan panel pilihan simbol
        });
        // Listener untuk tombol "Play vs Friend"
        gameUI.getPlayVsFriendButton().addActionListener(e -> {
            gameLogic.setGameMode(GameLogic.GameMode.MULTIPLAYER); // Mengatur mode game ke MULTIPLAYER
            multiplayerManager.promptMultiplayerSetup(); // Meminta setup multiplayer (create/join game)
        });
        // Listener untuk tombol "Play Again"
        gameUI.getPlayAgainButton().addActionListener(e -> {
            System.out.println("Play Again button clicked");
            gameLogic.newGame(); // Memulai game baru
        });
        // Listener untuk tombol "Reset Score"
        gameUI.getResetScoreButton().addActionListener(e -> {
            System.out.println("Reset Score button clicked");
            gameLogic.resetScores(); // Mereset skor
        });
        // Listener untuk tombol "Play as X" (pilihan simbol di mode VS AI)
        gameUI.getSelectXButton().addActionListener(e -> {
            aiManager.setPlayerSeed(Seed.CROSS);   // Mengatur simbol pemain ke X
            aiManager.setAiSeed(Seed.NOUGHT);      // Mengatur simbol AI ke O
            gameUI.showFirstTurnPanelForVsAI();    // Menampilkan panel pilihan giliran pertama
        });
        // Listener untuk tombol "Play as O" (pilihan simbol di mode VS AI)
        gameUI.getSelectOButton().addActionListener(e -> {
            aiManager.setPlayerSeed(Seed.NOUGHT);  // Mengatur simbol pemain ke O
            aiManager.setAiSeed(Seed.CROSS);       // Mengatur simbol AI ke X
            gameUI.showFirstTurnPanelForVsAI();    // Menampilkan panel pilihan giliran pertama
        });
        // Listener untuk tombol "You Start" (pilihan giliran pertama di mode VS AI)
        gameUI.getPlayerStartsButton().addActionListener(e -> aiManager.startVsAIGame(true)); // Pemain mulai
        // Listener untuk tombol "AI Starts" (pilihan giliran pertama di mode VS AI)
        gameUI.getAiStartsButton().addActionListener(e -> aiManager.startVsAIGame(false)); // AI mulai
    }

    /**
     * Menambahkan MouseListener ke area papan game.
     * Ini menghapus listener mouse yang ada sebelumnya dan menambahkan listener baru.
     */
    private void addMouseListenerToGameBoard() {
        // Mengambil semua MouseListener yang sudah ada
        MouseListener[] listeners = getMouseListeners();
        // Menghapus semua MouseListener yang ada
        for (MouseListener l : listeners) {
            removeMouseListener(l);
        }
        // Menambahkan MouseAdapter baru untuk menangani klik mouse
        super.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e); // Memanggil metode penanganan klik mouse
            }
        });
    }

    /**
     * Menangani peristiwa klik mouse pada GamePanel.
     * Menentukan sel papan mana yang diklik dan memicu aksi game yang sesuai.
     */
    private void handleMouseClick(MouseEvent e) {
        int mouseX = e.getX(); // Koordinat X klik mouse
        int mouseY = e.getY(); // Koordinat Y klik mouse

        // Menghitung area papan game di dalam panel
        int boardStartX = (getWidth() - Board.CANVAS_WIDTH) / 2;
        int boardStartY = (getHeight() - Board.CANVAS_HEIGHT) / 2;
        int boardEndX = boardStartX + Board.CANVAS_WIDTH;
        int boardEndY = boardStartY + Board.CANVAS_HEIGHT;

        // Memeriksa apakah klik mouse berada di dalam area papan game
        if (mouseX >= boardStartX && mouseX < boardEndX &&
                mouseY >= boardStartY && mouseY < boardEndY) {

            // Menghitung baris dan kolom sel yang diklik
            int row = (mouseY - boardStartY) / Cell.SIZE;
            int col = (mouseX - boardStartX) / Cell.SIZE;

            // Memproses klik berdasarkan state game dan giliran pemain
            if (gameLogic.getCurrentState() == State.PLAYING && gameLogic.isMyTurn()) {
                // Memeriksa apakah sel yang diklik valid (di dalam batas dan kosong)
                if (row >= 0 && row < Board.ROWS && col >= 0 && col < Board.COLS
                        && gameLogic.getBoard().cells[row][col].content == Seed.NO_SEED) {
                    // Memanggil manajer yang sesuai berdasarkan mode game
                    if (gameLogic.getGameMode() == GameLogic.GameMode.VS_AI) {
                        aiManager.makeMove(row, col); // Memicu langkah pemain di mode VS AI
                    } else if (gameLogic.getGameMode() == GameLogic.GameMode.MULTIPLAYER) {
                        multiplayerManager.makeMove(row, col); // Memicu langkah pemain di mode Multiplayer
                    }
                } else {
                    gameUI.setStatusText("Invalid move! Cell already occupied."); // Pesan jika sel sudah terisi
                }
            } else if (gameLogic.getCurrentState() != State.PLAYING) {
                // Pesan jika game sudah berakhir
                gameUI.setStatusText(gameLogic.getCurrentState().getDisplayName() + "! Click Play Again to restart.");
                gameUI.updateActionButtonsVisibility(true); // Menampilkan tombol aksi
            } else if (!gameLogic.isMyTurn()) {
                gameUI.setStatusText("It's not your turn. Please wait."); // Pesan jika bukan giliran pemain
            }
        }
    }

    /**
     * Metode untuk menggambar komponen pada panel.
     * Dipanggil oleh sistem Swing setiap kali panel perlu digambar ulang.
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g); // Memanggil implementasi paintComponent superclass

        // Menggambar gambar latar belakang jika tersedia
        if (backgroundImage != null) {
            g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
        } else {
            // Menggambar latar belakang solid jika gambar tidak ada
            g.setColor(new Color(50, 50, 100));
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        // Menggambar papan game jika mode game sudah dipilih
        if (gameLogic.getGameMode() != null) {
            // Menghitung posisi awal papan agar terpusat
            int boardStartX = (getWidth() - Board.CANVAS_WIDTH) / 2;
            int boardStartY = (getHeight() - Board.CANVAS_HEIGHT) / 2;
            g.translate(boardStartX, boardStartY); // Menggeser koordinat untuk menggambar papan
            gameLogic.getBoard().paint(g); // Meminta papan game untuk menggambar dirinya
            g.translate(-boardStartX, -boardStartY); // Mengembalikan translasi koordinat
        }

        // Memperbarui teks status bar berdasarkan mode game dan giliran
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

        // Menangani status game akhir (DRAW, WON)
        if (gameLogic.getCurrentState() != State.PLAYING) {
            gameUI.getStatusBar().setForeground(Color.RED);
            gameUI.setStatusText(gameLogic.getCurrentState().getDisplayName() + "! Click Play Again to restart.");
            gameUI.updateActionButtonsVisibility(true);
        } else if (gameLogic.getGameMode() == null) {
            // Pesan sambutan jika belum ada mode game yang dipilih
            gameUI.getStatusBar().setForeground(Color.BLACK);
            gameUI.setStatusText("Welcome, " + gameLogic.getLoggedInUsername() + "! Please select a game mode.");
        }
    }

    // --- Implementasi metode dari antarmuka GameClientListener ---
    // Metode-metode ini saat ini kosong, menunjukkan bahwa fungsionalitas
    // GameClient/GameServer untuk gameplay tidak sepenuhnya terintegrasi
    // dalam GamePanel versi ini.
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