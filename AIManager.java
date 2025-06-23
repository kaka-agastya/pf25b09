import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AIManager {
    private final GameLogic gameLogic;
    private final GameUI gameUI;
    private final GamePanel gamePanel;
    private TicTacToeAI ai;

    public AIManager(GameLogic gameLogic, GameUI gameUI, GamePanel gamePanel) {
        if (gameLogic == null || gameUI == null || gamePanel == null) {
            throw new IllegalArgumentException("GameLogic, GameUI, or GamePanel cannot be null");
        }
        this.gameLogic = gameLogic;
        this.gameUI = gameUI;
        this.gamePanel = gamePanel;
    }

    public void setPlayerSeed(Seed seed) {
        gameLogic.setPlayerSeed(seed);
    }

    public void setAiSeed(Seed seed) {
        gameLogic.setAiSeed(seed);
    }

    public void startVsAIGame(boolean humanStarts) {
        gameUI.hideFirstTurnPanel();
        ai = new TicTacToeAI();

        String playerSeedDisplay = gameLogic.getPlayerSeed() != null ? gameLogic.getPlayerSeed().getDisplayName() : "Unknown";
        if (humanStarts) {
            gameLogic.setMyTurn(true);
            gameUI.setStatusText("You are " + playerSeedDisplay + ". Your turn.");
        } else {
            gameLogic.setMyTurn(false);
            gameUI.setStatusText("You are " + playerSeedDisplay + ". AI's turn.");
            Timer timer = new Timer(100, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ((Timer) e.getSource()).stop();
                    System.out.println("Timer triggered: Calling aiMakeMove() in startVsAIGame");
                    aiMakeMove();
                }
            });
            timer.start();
        }
        gameUI.updatePlayerNameLabels();
    }

    public void makeMove(int row, int col) {
        if (gameLogic.getCurrentState() == null || gameLogic.getBoard() == null) {
            gameUI.setStatusText("Error: Game state or board is not initialized.");
            System.err.println("GameLogic state or board is null in makeMove");
            return;
        }
        if (gameLogic.getCurrentState() == State.PLAYING && 
            gameLogic.getBoard().cells[row][col].content == Seed.NO_SEED) {
            gameLogic.getBoard().cells[row][col].content = gameLogic.getPlayerSeed();
            SoundEffect.EAT_FOOD.play();
            gameLogic.setCurrentState(gameLogic.getBoard().stepGame(gameLogic.getPlayerSeed(), row, col));

            SwingUtilities.invokeLater(() -> {
                gamePanel.repaint();
                gamePanel.paintImmediately(gamePanel.getBounds());
            });

            System.out.println("Game state after player move: " + gameLogic.getCurrentState());
            if (gameLogic.getCurrentState() == State.PLAYING) {
                gameLogic.setMyTurn(false);
                gameUI.setStatusText("AI's turn. Please wait.");
                Timer timer = new Timer(100, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ((Timer) e.getSource()).stop();
                        System.out.println("Timer triggered: Calling aiMakeMove() in makeMove");
                        aiMakeMove();
                    }
                });
                timer.start();
            } else {
                gameLogic.updateScore(gameLogic.getPlayerSeed());
                gameUI.updateScoreLabel();
                gameUI.setStatusText(gameLogic.getCurrentState().getDisplayName() + "! Click Play Again to restart.");
                gameLogic.setMyTurn(false);
                gameUI.updateActionButtonsVisibility(true);
                System.out.println("Setting action buttons visible: true");
                SoundEffect.DIE.play();
            }
        } else {
            gameUI.setStatusText("Invalid move! Cell already occupied or game not in progress.");
        }
    }

    private void aiMakeMove() {
        if (gameLogic.getCurrentState() != State.PLAYING) {
            System.out.println("aiMakeMove: Game not in PLAYING state, exiting.");
            return;
        }

        if (ai == null) {
            gameUI.setStatusText("Error: AI not initialized!");
            System.err.println("AI is null in aiMakeMove");
            gameLogic.setCurrentState(State.DRAW);
            SwingUtilities.invokeLater(() -> {
                gamePanel.repaint();
                gamePanel.paintImmediately(gamePanel.getBounds());
            });
            gameUI.updateScoreLabel();
            gameUI.updateActionButtonsVisibility(true);
            System.out.println("Setting action buttons visible: true (AI null)");
            return;
        }

        int[] bestMove = ai.findBestMove(gameLogic.getBoard(), gameLogic.getAiSeed());
        if (bestMove == null || bestMove.length < 2) {
            gameUI.setStatusText("AI couldn't find a valid move! (Error in AI logic)");
            System.err.println("Invalid bestMove from AI: " + (bestMove == null ? "null" : "length=" + bestMove.length));
            gameLogic.setCurrentState(State.DRAW);
            SwingUtilities.invokeLater(() -> {
                gamePanel.repaint();
                gamePanel.paintImmediately(gamePanel.getBounds());
            });
            gameUI.updateScoreLabel();
            gameUI.updateActionButtonsVisibility(true);
            System.out.println("Setting action buttons visible: true (Invalid AI move)");
            return;
        }

        int aiRow = bestMove[0];
        int aiCol = bestMove[1];

        if (aiRow != -1 && aiCol != -1) {
            System.out.println("AI move: Row=" + aiRow + ", Col=" + aiCol + ", Seed=" + gameLogic.getAiSeed());
            gameLogic.getBoard().cells[aiRow][aiCol].content = gameLogic.getAiSeed();
            SoundEffect.DIE.play();
            gameLogic.setCurrentState(gameLogic.getBoard().stepGame(gameLogic.getAiSeed(), aiRow, aiCol));

            SwingUtilities.invokeLater(() -> {
                gamePanel.repaint();
                gamePanel.paintImmediately(gamePanel.getBounds());
            });

            System.out.println("Game state after AI move: " + gameLogic.getCurrentState());
            if (gameLogic.getCurrentState() == State.PLAYING) {
                gameLogic.setMyTurn(true);
                String playerSeedDisplay = gameLogic.getPlayerSeed() != null ? gameLogic.getPlayerSeed().getDisplayName() : "Unknown";
                gameUI.setStatusText("Your turn (" + playerSeedDisplay + "). Make your move.");
            } else {
                gameLogic.updateScore(gameLogic.getAiSeed());
                gameUI.updateScoreLabel();
                gameUI.setStatusText(gameLogic.getCurrentState().getDisplayName() + "! Click Play Again to restart.");
                gameLogic.setMyTurn(false);
                gameUI.updateActionButtonsVisibility(true);
                System.out.println("Setting action buttons visible: true (Game over)");
                SoundEffect.DIE.play();
            }
        } else {
            gameUI.setStatusText("AI couldn't find a move! (Error in AI logic or full board)");
            gameLogic.setCurrentState(State.DRAW);
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
