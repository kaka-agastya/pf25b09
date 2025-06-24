// package GameUI; // Asumsi paket jika tidak ada di file.

import javax.swing.*;
import java.awt.*;

/**
 * Kelas GameUI bertanggung jawab untuk mengatur dan mengelola semua elemen
 * Antarmuka Pengguna (UI) grafis dari game Tic-Tac-Toe.
 * Ini mencakup label judul, label nama pemain, label skor, tombol-tombol,
 * dan status bar. Kelas ini bertanggung jawab untuk menampilkan informasi
 * kepada pengguna dan menerima input melalui tombol.
 */
public class GameUI {
    // Konstanta warna untuk elemen UI
    public static final Color COLOR_CROSS = new Color(239, 105, 80);  // Merah untuk X
    public static final Color COLOR_NOUGHT = new Color(64, 154, 225); // Biru untuk O
    public static final Color COLOR_BG_STATUS = new Color(0, 0, 0, 255); // Hitam penuh untuk status bar
    public static final Font FONT_STATUS = new Font("OCR A Extended", Font.PLAIN, 14); // Font untuk status bar

    // Referensi ke objek GameLogic untuk mendapatkan data game
    private GameLogic gameLogic;

    // Komponen-komponen UI utama
    private JPanel topPanel;                // Panel atas yang berisi judul, nama pemain, skor, dan tombol mode
    private JLabel titleLabel;              // Label untuk judul game
    private JLabel playerXNameLabel;        // Label untuk nama pemain X
    private JLabel playerONameLabel;        // Label untuk nama pemain O
    private JLabel scoreLabel;              // Label untuk menampilkan skor
    private JButton playVsComputerButton;   // Tombol untuk mode vs komputer
    private JButton playVsFriendButton;     // Tombol untuk mode vs teman (multiplayer)
    private JPanel modeButtonsPanel;        // Panel yang berisi tombol-tombol pilihan mode game

    private JPanel seedSelectionPanel;      // Panel untuk memilih simbol pemain (X atau O) di mode VS AI
    private JButton selectXButton;          // Tombol untuk memilih X
    private JButton selectOButton;          // Tombol untuk memilih O

    private JPanel firstTurnSelectionPanel; // Panel untuk memilih siapa yang memulai duluan di mode VS AI
    private JButton playerStartsButton;     // Tombol "You Start"
    private JButton aiStartsButton;         // Tombol "AI Starts"

    private JPanel bottomPanel;             // Panel bawah yang berisi tombol aksi
    private JButton playAgainButton;        // Tombol "Play Again"
    private JButton resetScoreButton;       // Tombol "Reset Score"
    private JLabel statusBar;               // Status bar di bagian bawah layar

    /**
     * Konstruktor GameUI.
     * Memanggil metode initUI() untuk menginisialisasi semua komponen UI.
     */
    public GameUI() {
        initUI();
    }

    /**
     * Menginisialisasi dan menata letak semua komponen UI dari game.
     */
    private void initUI() {
        // --- Panel Atas (TopPanel) ---
        topPanel = new JPanel();
        topPanel.setOpaque(false); // Membuat panel transparan
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS)); // Tata letak vertikal
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0)); // Padding

        // Label Judul Game
        titleLabel = new JLabel("TicTacToe!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48)); // Font besar dan tebal
        titleLabel.setForeground(Color.YELLOW); // Warna kuning
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Pusatkan horizontal

        // Panel Nama Pemain dan Skor
        JPanel playerNamesPanel = new JPanel();
        playerNamesPanel.setOpaque(false); // Transparan
        playerNamesPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0)); // Tata letak flow, pusatkan, jarak 20px

        // Label Nama Pemain X
        playerXNameLabel = new JLabel("Player X: -");
        playerXNameLabel.setFont(new Font("Arial", Font.BOLD, 20));
        playerXNameLabel.setForeground(COLOR_CROSS); // Warna merah untuk X

        // Label Skor
        scoreLabel = new JLabel("0 | 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 24));
        scoreLabel.setForeground(Color.WHITE); // Warna putih

        // Label Nama Pemain O
        playerONameLabel = new JLabel("Player O: -");
        playerONameLabel.setFont(new Font("Arial", Font.BOLD, 20));
        playerONameLabel.setForeground(COLOR_NOUGHT); // Warna biru untuk O

        // Menambahkan label nama pemain dan skor ke playerNamesPanel
        playerNamesPanel.add(playerXNameLabel);
        playerNamesPanel.add(scoreLabel);
        playerNamesPanel.add(playerONameLabel);

        // Panel Tombol Pilihan Mode Game
        modeButtonsPanel = new JPanel();
        modeButtonsPanel.setOpaque(false); // Transparan
        modeButtonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 5)); // Tata letak flow, pusatkan, jarak 15px

        // Tombol "Play vs Computer"
        playVsComputerButton = new JButton("Play vs Computer");
        styleButton(playVsComputerButton, new Color(64, 154, 225)); // Gaya tombol biru

        // Tombol "Play vs Friend"
        playVsFriendButton = new JButton("Play vs Friend");
        styleButton(playVsFriendButton, new Color(239, 105, 80)); // Gaya tombol merah

        // Menambahkan tombol mode ke modeButtonsPanel
        modeButtonsPanel.add(playVsComputerButton);
        modeButtonsPanel.add(playVsFriendButton);

        // Menambahkan semua komponen ke topPanel
        topPanel.add(titleLabel);
        topPanel.add(Box.createVerticalStrut(10)); // Jarak vertikal
        topPanel.add(playerNamesPanel);
        topPanel.add(Box.createVerticalStrut(10)); // Jarak vertikal
        topPanel.add(modeButtonsPanel);

        // --- Panel Bawah (BottomPanel) ---
        bottomPanel = new JPanel();
        bottomPanel.setOpaque(false); // Transparan
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10)); // Tata letak flow, pusatkan, jarak 15px

        // Tombol "Play Again"
        playAgainButton = new JButton("Play Again");
        styleButton(playAgainButton, new Color(239, 105, 80));
        playAgainButton.setVisible(false); // Awalnya tidak terlihat

        // Tombol "Reset Score"
        resetScoreButton = new JButton("Reset Score");
        styleButton(resetScoreButton, new Color(239, 105, 80));
        resetScoreButton.setVisible(false); // Awalnya tidak terlihat

        // Menambahkan tombol aksi ke bottomPanel
        bottomPanel.add(playAgainButton);
        bottomPanel.add(resetScoreButton);

        // --- Status Bar ---
        statusBar = new JLabel();
        statusBar.setFont(FONT_STATUS);
        statusBar.setBackground(COLOR_BG_STATUS); // Latar belakang hitam
        statusBar.setOpaque(true); // Pastikan latar belakang terlihat
        statusBar.setPreferredSize(new Dimension(300, 30)); // Ukuran preferensi
        statusBar.setHorizontalAlignment(JLabel.CENTER); // Pusatkan teks
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 12)); // Padding

        // Membuat panel pilihan simbol dan giliran (tidak ditambahkan ke layout utama di sini)
        createSeedSelectionPanel();
        createFirstTurnSelectionPanel();
    }

    /**
     * Menerapkan gaya visual standar pada JButton.
     * @param button JButton yang akan diberi gaya.
     * @param bgColor Warna latar belakang tombol.
     */
    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("Arial", Font.BOLD, 30));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false); // Tidak menggambar fokus saat diklik
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Mengubah kursor menjadi tangan saat di hover
        button.setOpaque(true); // Memastikan warna latar belakang terlihat
        button.setBorder(BorderFactory.createLineBorder(new Color(40, 120, 180), 2, true)); // Border
    }

    /**
     * Menerapkan gaya visual khusus untuk tombol pilihan giliran.
     * @param button JButton yang akan diberi gaya.
     * @param bgColor Warna latar belakang tombol.
     */
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

    /**
     * Membuat panel untuk pilihan simbol pemain (X atau O) di mode VS AI.
     */
    private void createSeedSelectionPanel() {
        seedSelectionPanel = new JPanel();
        seedSelectionPanel.setOpaque(false);
        seedSelectionPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 120));
        seedSelectionPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        selectXButton = new JButton("Play as X");
        styleButton(selectXButton, COLOR_CROSS); // Gaya tombol X

        selectOButton = new JButton("Play as O");
        styleButton(selectOButton, COLOR_NOUGHT); // Gaya tombol O

        seedSelectionPanel.add(selectXButton);
        seedSelectionPanel.add(selectOButton);
    }

    /**
     * Membuat panel untuk pilihan siapa yang membuat langkah pertama di mode VS AI.
     */
    private void createFirstTurnSelectionPanel() {
        firstTurnSelectionPanel = new JPanel();
        firstTurnSelectionPanel.setOpaque(false);
        firstTurnSelectionPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 120));
        firstTurnSelectionPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        playerStartsButton = new JButton("You Start");
        styleTurnButton(playerStartsButton, COLOR_NOUGHT); // Gaya tombol "You Start"

        aiStartsButton = new JButton("AI Starts");
        styleTurnButton(aiStartsButton, COLOR_CROSS); // Gaya tombol "AI Starts"

        firstTurnSelectionPanel.add(playerStartsButton);
        firstTurnSelectionPanel.add(aiStartsButton);
    }

    /**
     * Memperbarui label skor di UI dengan nilai terbaru dari GameLogic.
     * Pembaruan dilakukan di EDT untuk keamanan thread.
     */
    public void updateScoreLabel() {
        if (gameLogic == null) {
            System.err.println("GameLogic is null in updateScoreLabel");
            return;
        }
        SwingUtilities.invokeLater(() -> scoreLabel.setText(gameLogic.getXScore() + " | " + gameLogic.getOScore()));
    }

    /**
     * Memperbarui label nama pemain di UI berdasarkan mode game dan peran pemain.
     * Pembaruan dilakukan di EDT untuk keamanan thread.
     */
    public void updatePlayerNameLabels() {
        if (gameLogic == null) {
            System.err.println("GameLogic is null in updatePlayerNameLabels");
            return;
        }
        SwingUtilities.invokeLater(() -> {
            if (gameLogic.getGameMode() == GameLogic.GameMode.VS_AI) {
                // Untuk mode VS AI: menampilkan nama pemain manusia atau "Computer"
                Seed playerSeed = gameLogic.getPlayerSeed();
                if (playerSeed != null) {
                    playerXNameLabel.setText("Player X: " + (playerSeed == Seed.CROSS ? gameLogic.getLoggedInUsername() : "Computer"));
                    playerONameLabel.setText("Player O: " + (playerSeed == Seed.NOUGHT ? gameLogic.getLoggedInUsername() : "Computer"));
                } else {
                    playerXNameLabel.setText("Player X: (You)");
                    playerONameLabel.setText("Player O: (Computer)");
                }
            } else if (gameLogic.getGameMode() == GameLogic.GameMode.MULTIPLAYER) {
                // Untuk mode Multiplayer: menampilkan username pemain lokal dan lawan
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
                // Default jika mode game belum dipilih
                playerXNameLabel.setText("Player X: -");
                playerONameLabel.setText("Player O: -");
            }
        });
    }

    /**
     * Mengatur visibilitas tombol "Play Again" dan "Reset Score".
     * Pembaruan dilakukan di EDT untuk keamanan thread.
     * @param visible True jika tombol harus terlihat, false sebaliknya.
     */
    public void updateActionButtonsVisibility(boolean visible) {
        SwingUtilities.invokeLater(() -> {
            if (playAgainButton == null || resetScoreButton == null) {
                System.err.println("playAgainButton or resetScoreButton is null in updateActionButtonsVisibility");
                return;
            }
            playAgainButton.setVisible(visible);
            resetScoreButton.setVisible(visible);
            bottomPanel.revalidate(); // Memvalidasi ulang layout bottomPanel
            bottomPanel.repaint();    // Menggambar ulang bottomPanel
            System.out.println("Action buttons visibility set to: " + visible);
        });
    }

    /**
     * Mengatur visibilitas tombol pilihan mode game ("Play vs Computer", "Play vs Friend").
     * Pembaruan dilakukan di EDT untuk keamanan thread.
     * @param visible True jika tombol harus terlihat, false sebaliknya.
     */
    public void updateModeButtonsVisibility(boolean visible) {
        SwingUtilities.invokeLater(() -> {
            modeButtonsPanel.setVisible(visible);
            topPanel.revalidate(); // Memvalidasi ulang layout topPanel
            topPanel.repaint();    // Menggambar ulang topPanel
            System.out.println("Mode buttons visibility set to: " + visible);
        });
    }

    /**
     * Menampilkan panel pilihan simbol pemain (seed selection panel).
     * Menyembunyikan tombol mode game sebelumnya.
     */
    public void showSeedSelectionPanel() {
        updateModeButtonsVisibility(false); // Sembunyikan tombol mode game
        // Menambahkan panel pilihan simbol ke parent dari topPanel (biasanya GamePanel itu sendiri)
        topPanel.getParent().add(seedSelectionPanel, BorderLayout.CENTER);
        topPanel.getParent().revalidate();
        topPanel.getParent().repaint();
        setStatusText("Choose your symbol (X or O):"); // Atur pesan status
        updatePlayerNameLabels(); // Perbarui label nama pemain
    }

    /**
     * Menampilkan panel pilihan giliran pertama untuk mode VS AI.
     * Menyembunyikan panel pilihan simbol sebelumnya.
     */
    public void showFirstTurnPanelForVsAI() {
        topPanel.getParent().remove(seedSelectionPanel); // Hapus panel pilihan simbol
        // Menambahkan panel pilihan giliran pertama
        topPanel.getParent().add(firstTurnSelectionPanel, BorderLayout.CENTER);
        topPanel.getParent().revalidate();
        topPanel.getParent().repaint();
        setStatusText("Who makes the first move?"); // Atur pesan status
        updatePlayerNameLabels(); // Perbarui label nama pemain
    }

    /**
     * Menyembunyikan panel pilihan giliran pertama.
     */
    public void hideFirstTurnPanel() {
        if (topPanel.getParent() != null && firstTurnSelectionPanel != null) {
            topPanel.getParent().remove(firstTurnSelectionPanel); // Hapus panel
            topPanel.getParent().revalidate();
            topPanel.getParent().repaint();
        }
    }

    /**
     * Menampilkan kembali tombol pilihan mode game.
     */
    public void showModeButtonsPanel() {
        updateModeButtonsVisibility(true); // Tampilkan tombol mode game
        topPanel.getParent().revalidate();
        topPanel.repaint();
    }

    /**
     * Mengatur teks pada status bar.
     * Pembaruan dilakukan di EDT untuk keamanan thread.
     * @param text Teks yang akan ditampilkan.
     */
    public void setStatusText(String text) {
        SwingUtilities.invokeLater(() -> statusBar.setText(text));
    }

    /**
     * Mengatur objek GameLogic yang terkait dengan UI ini.
     * @param gameLogic Objek GameLogic.
     */
    public void setGameLogic(GameLogic gameLogic) {
        this.gameLogic = gameLogic;
    }

    // --- Getters untuk komponen-komponen UI ---
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