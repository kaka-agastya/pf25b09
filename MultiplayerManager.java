import javax.swing.*;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MultiplayerManager {
    private final GameLogic gameLogic;
    private final GameUI gameUI;
    private final GamePanel gamePanel;
    private String gameId;
    private int lastKnownMoveNumber;
    private ScheduledExecutorService poller;


    public MultiplayerManager(GameLogic gameLogic, GameUI gameUI, GamePanel gamePanel) {
        this.gameLogic = gameLogic;
        this.gameUI = gameUI;
        this.gamePanel = gamePanel;
        this.lastKnownMoveNumber = 0;
    }


    public void promptMultiplayerSetup() {
        String inputGameId = JOptionPane.showInputDialog(gamePanel, "Enter Game ID (e.g., 'mygame123') or leave empty to join existing:", "Multiplayer Game ID", JOptionPane.QUESTION_MESSAGE);
        if (inputGameId == null || inputGameId.trim().isEmpty()) {
            JOptionPane.showMessageDialog(gamePanel, "Game ID cannot be empty. Returning to mode selection.", "Error", JOptionPane.ERROR_MESSAGE);
            revertToModeSelection();
            return;
        }
        this.gameId = inputGameId.trim();


        String[] roles = {"X", "O"};
        int roleChoice = JOptionPane.showOptionDialog(gamePanel, "Choose your role for game ID: " + gameId, "Choose Role", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, roles, roles[0]);
        if (roleChoice == JOptionPane.CLOSED_OPTION) {
            JOptionPane.showMessageDialog(gamePanel, "Role selection cancelled. Returning to mode selection.", "Cancelled", JOptionPane.WARNING_MESSAGE);
            revertToModeSelection();
            return;
        }
        gameLogic.setPlayerRole(roles[roleChoice]);
        gameLogic.setPlayerSeed(gameLogic.getPlayerRole().equals("X") ? Seed.CROSS : Seed.NOUGHT);
        gameLogic.setAiSeed(gameLogic.getPlayerRole().equals("X") ? Seed.NOUGHT : Seed.CROSS);


        startGameInSelectedMode();
    }


    private void startGameInSelectedMode() {
        gameUI.showModeButtonsPanel();
        gameLogic.newGame();
    }


    private void startGamePolling() {
        if (poller != null && !poller.isShutdown()) {
            poller.shutdownNow();
        }


        lastKnownMoveNumber = 0;
        gameLogic.setMyTurn(false);


        poller = Executors.newSingleThreadScheduledExecutor();
        poller.scheduleAtFixedRate(() -> {
            try {
                List<DatabaseManager.Move> newMoves = DatabaseManager.fetchMoves(gameId, lastKnownMoveNumber);
                if (!newMoves.isEmpty()) {
                    SwingUtilities.invokeLater(() -> {
                        for (DatabaseManager.Move move : newMoves) {
                            if (move.moveNumber > lastKnownMoveNumber) {
                                System.out.println("Processing new move: " + move);
                                Seed movedSeed = (move.playerSeed.equals("X") ? Seed.CROSS : Seed.NOUGHT);
                                gameLogic.getBoard().cells[move.row][move.col].content = movedSeed;
                                SwingUtilities.invokeLater(() -> {
                                    gamePanel.repaint();
                                    gamePanel.paintImmediately(gamePanel.getBounds());
                                });
                                if (!move.playerUsername.equals(gameLogic.getLoggedInUsername())) {
                                    SoundEffect.EAT_FOOD.play();
                                }
                                lastKnownMoveNumber = move.moveNumber;
                                State currentGameState = gameLogic.getBoard().stepGame(movedSeed, move.row, move.col);
                                if (currentGameState != State.PLAYING) {
                                    gameLogic.setCurrentState(currentGameState);
                                    gamePanel.repaint();
                                    gameLogic.updateScore(movedSeed);
                                    gameUI.updateScoreLabel(); // Ditambahkan
                                    gameUI.updateActionButtonsVisibility(true);
                                    poller.shutdownNow();
                                    gameUI.setStatusText(currentGameState.getDisplayName() + "! Click Play Again.");
                                    SoundEffect.DIE.play();
                                    return;
                                }
                            }
                        }
                        if (lastKnownMoveNumber % 2 == 0) {
                            gameLogic.setMyTurn(gameLogic.getPlayerRole().equals("X"));
                        } else {
                            gameLogic.setMyTurn(gameLogic.getPlayerRole().equals("O"));
                        }
                        gameUI.setStatusText(gameLogic.isMyTurn() ? "Your turn." : "Opponent's turn.");
                        gameUI.updatePlayerNameLabels();
                    });
                }
            } catch (SQLException | ClassNotFoundException ex) {
                System.err.println("Error polling for moves: " + ex.getMessage());
                poller.shutdownNow();
                SwingUtilities.invokeLater(() -> revertToModeSelection());
            }
        }, 0, 1, TimeUnit.SECONDS);
    }


    public void makeMove(int row, int col) {
        if (gameLogic.getCurrentState() == State.PLAYING && gameLogic.getBoard().cells[row][col].content == Seed.NO_SEED) {
            try {
                int currentMoveNumber = lastKnownMoveNumber + 1;
                DatabaseManager.insertMove(gameId, currentMoveNumber, gameLogic.getLoggedInUsername(), gameLogic.getPlayerRole(), row, col);
                lastKnownMoveNumber = currentMoveNumber;


                gameLogic.getBoard().cells[row][col].content = (gameLogic.getPlayerRole().equals("X") ? Seed.CROSS : Seed.NOUGHT);
                SoundEffect.EAT_FOOD.play();


                SwingUtilities.invokeLater(() -> {
                    gamePanel.repaint();
                    gamePanel.paintImmediately(gamePanel.getBounds());
                });


                gameLogic.setCurrentState(gameLogic.getBoard().stepGame((gameLogic.getPlayerRole().equals("X") ? Seed.CROSS : Seed.NOUGHT), row, col));
                if (gameLogic.getCurrentState() != State.PLAYING) {
                    poller.shutdownNow();
                    gameUI.setStatusText(gameLogic.getCurrentState().getDisplayName() + "! Click Play Again.");
                    gameLogic.updateScore((gameLogic.getPlayerRole().equals("X") ? Seed.CROSS : Seed.NOUGHT));
                    gameUI.updateScoreLabel(); // Ditambahkan
                    gameUI.updateActionButtonsVisibility(true);
                    SoundEffect.DIE.play();
                } else {
                    gameLogic.setMyTurn(false);
                    gameUI.setStatusText("Waiting for opponent...");
                }
            } catch (SQLException | ClassNotFoundException ex) {
                System.err.println("Error submitting move: " + ex.getMessage());
                gameUI.setStatusText("Error submitting move. Try again.");
            }
        }
    }


    private void revertToModeSelection() {
        if (poller != null && !poller.isShutdown()) {
            poller.shutdownNow();
            System.out.println("Multiplayer: Poller stopped.");
        }


        gameLogic.setGameMode(null);
        gameLogic.newGame();
        gameUI.updateScoreLabel();
        gameUI.updateActionButtonsVisibility(false);
        gameUI.showModeButtonsPanel();
    }
}

