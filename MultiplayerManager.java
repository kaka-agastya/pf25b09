import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

public class MultiplayerManager {
    private final GameLogic gameLogic;
    private final GameUI gameUI;
    private final GamePanel gamePanel;
    private String gameId;
    private int lastMoveNumber;

    public MultiplayerManager(GameLogic gameLogic, GameUI gameUI, GamePanel gamePanel) {
        if (gameLogic == null || gameUI == null || gamePanel == null) {
            throw new IllegalArgumentException("GameLogic, GameUI, or GamePanel cannot be null");
        }
        this.gameLogic = gameLogic;
        this.gameUI = gameUI;
        this.gamePanel = gamePanel;
        this.lastMoveNumber = 0;
    }

    public void promptMultiplayerSetup() {
        gameUI.updateModeButtonsVisibility(false);
        String[] options = {"Create Game", "Join Game"};
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

        if (choice == 0) {
            // Create new game
            gameId = JOptionPane.showInputDialog(null, "Enter Game ID for the new game:");
            if (gameId == null || gameId.trim().isEmpty()) {
                gameUI.setStatusText("Game ID cannot be empty!");
                gameLogic.setGameMode(null);
                gameUI.showModeButtonsPanel();
                return;
            }
            try {
                DatabaseManager.clearGameMoves(gameId);
            } catch (SQLException | ClassNotFoundException e) {
                gameUI.setStatusText("Error clearing game moves: " + e.getMessage());
                System.err.println("MultiplayerManager: Error clearing game moves: " + e.getMessage());
                gameLogic.setGameMode(null);
                gameUI.showModeButtonsPanel();
                return;
            }

            gameLogic.setPlayerRole("X");
            gameLogic.setOpponentUsername("Waiting...");
            gameLogic.newGame();
            gameLogic.setCurrentState(State.PLAYING);
            gameLogic.setMyTurn(true);
            gameUI.setStatusText("Game created! Your turn as X. Waiting for opponent...");
            gameUI.updatePlayerNameLabels();
            startPollingForOpponentMove();
            System.out.println("MultiplayerManager: Game created with ID: " + gameId);
        } else if (choice == 1) {
            // Join existing game
            gameId = JOptionPane.showInputDialog(null, "Enter Game ID to join:");
            if (gameId == null || gameId.trim().isEmpty()) {
                gameUI.setStatusText("Game ID cannot be empty!");
                gameLogic.setGameMode(null);
                gameUI.showModeButtonsPanel();
                return;
            }
            gameLogic.setPlayerRole("O");
            gameLogic.setOpponentUsername("Host");
            gameLogic.newGame();
            gameLogic.setCurrentState(State.PLAYING);
            gameLogic.setMyTurn(false);
            gameUI.setStatusText("Joined game as O. Waiting for opponent's move...");
            gameUI.updatePlayerNameLabels();
            startPollingForOpponentMove();
            System.out.println("MultiplayerManager: Joined game with ID: " + gameId);
        } else {
            gameLogic.setGameMode(null);
            gameUI.showModeButtonsPanel();
            gameUI.setStatusText("Multiplayer setup cancelled.");
        }
    }

    public void makeMove(int row, int col) {
        if (gameLogic.getCurrentState() == null || gameLogic.getBoard() == null) {
            gameUI.setStatusText("Error: Game state or board is not initialized.");
            System.err.println("MultiplayerManager: Game state or board is null in makeMove");
            return;
        }
        if (gameLogic.getCurrentState() == State.PLAYING && gameLogic.isMyTurn() &&
                gameLogic.getBoard().cells[row][col].content == Seed.NO_SEED) {
            Seed playerSeed = gameLogic.getPlayerRole().equals("X") ? Seed.CROSS : Seed.NOUGHT;
            gameLogic.getBoard().cells[row][col].content = playerSeed;
            SoundEffect.EAT_FOOD.play();
            gameLogic.setCurrentState(gameLogic.getBoard().stepGame(playerSeed, row, col));

            SwingUtilities.invokeLater(() -> {
                gamePanel.repaint();
                gamePanel.paintImmediately(gamePanel.getBounds());
            });

            try {
                DatabaseManager.insertMove(
                        gameId,
                        ++lastMoveNumber,
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
                gameLogic.setMyTurn(false);
                gameUI.setStatusText("Waiting for opponent's move...");
                startPollingForOpponentMove();
            } else {
                gameLogic.updateScore(playerSeed);
                gameUI.updateScoreLabel();
                gameUI.setStatusText(gameLogic.getCurrentState().getDisplayName() + "! Click Play Again to restart.");
                gameLogic.setMyTurn(false);
                gameUI.updateActionButtonsVisibility(true);
                gameUI.showModeButtonsPanel(); // Tampilkan kembali tombol mode saat permainan berakhir
                System.out.println("MultiplayerManager: Setting action buttons visible: true");
                SoundEffect.WIN.play();
            }
        } else {
            gameUI.setStatusText("Invalid move! Cell already occupied or not your turn.");
        }
    }

    private void startPollingForOpponentMove() {
        Timer timer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkForOpponentMove();
            }
        });
        timer.start();
        System.out.println("MultiplayerManager: Started polling for opponent moves, gameId=" + gameId);
    }

    private void checkForOpponentMove() {
        if (gameLogic.getCurrentState() != State.PLAYING || gameLogic.isMyTurn()) {
            return;
        }
        try {
            List<DatabaseManager.Move> moves = DatabaseManager.fetchMoves(gameId, lastMoveNumber);
            if (!moves.isEmpty()) {
                DatabaseManager.Move latestMove = moves.get(moves.size() - 1);
                lastMoveNumber = latestMove.moveNumber;

                Seed opponentSeed = latestMove.playerSeed.equals("X") ? Seed.CROSS : Seed.NOUGHT;
                gameLogic.getBoard().cells[latestMove.row][latestMove.col].content = opponentSeed;
                SoundEffect.TOY.play();
                gameLogic.setCurrentState(gameLogic.getBoard().stepGame(opponentSeed, latestMove.row, latestMove.col));

                SwingUtilities.invokeLater(() -> {
                    gamePanel.repaint();
                    gamePanel.paintImmediately(gamePanel.getBounds());
                });

                System.out.println("MultiplayerManager: Opponent move received: " + latestMove);
                if (gameLogic.getCurrentState() == State.PLAYING) {
                    gameLogic.setMyTurn(true);
                    gameUI.setStatusText("Your turn (" + gameLogic.getPlayerRole() + ").");
                } else {
                    gameLogic.updateScore(opponentSeed);
                    gameUI.updateScoreLabel();
                    gameUI.setStatusText(gameLogic.getCurrentState().getDisplayName() + "! Click Play Again to restart.");
                    gameLogic.setMyTurn(false);
                    gameUI.updateActionButtonsVisibility(true);
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