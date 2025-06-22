import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TicTacToeAI {
    private Random random = new Random();

    public int[] findBestMove(Board currentBoard, Seed aiPlayerSeed) {
        
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                if (currentBoard.cells[r][c].content == Seed.NO_SEED) {
                    currentBoard.cells[r][c].content = aiPlayerSeed; 
                    if (currentBoard.stepGame(aiPlayerSeed, r, c) == (aiPlayerSeed == Seed.CROSS ? State.CROSS_WON : State.NOUGHT_WON)) {
                        currentBoard.cells[r][c].content = Seed.NO_SEED; 
                        return new int[]{r, c}; 
                    }
                    currentBoard.cells[r][c].content = Seed.NO_SEED; 
                }
            }
        }

        
        Seed opponentSeed = (aiPlayerSeed == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                if (currentBoard.cells[r][c].content == Seed.NO_SEED) {
                    currentBoard.cells[r][c].content = opponentSeed; 
                    if (currentBoard.stepGame(opponentSeed, r, c) == (opponentSeed == Seed.CROSS ? State.CROSS_WON : State.NOUGHT_WON)) {
                        currentBoard.cells[r][c].content = Seed.NO_SEED; 
                        return new int[]{r, c}; 
                    }
                    currentBoard.cells[r][c].content = Seed.NO_SEED; 
                }
            }
        }
        
        
        if (currentBoard.cells[1][1].content == Seed.NO_SEED) {
            return new int[]{1, 1};
        }

        
        int[][] corners = {{0, 0}, {0, 2}, {2, 0}, {2, 2}};
        for (int[] corner : corners) {
            if (currentBoard.cells[corner[0]][corner[1]].content == Seed.NO_SEED) {
                return corner;
            }
        }

        
        List<int[]> emptyCells = new ArrayList<>();
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                if (currentBoard.cells[r][c].content == Seed.NO_SEED) {
                    emptyCells.add(new int[]{r, c});
                }
            }
        }
        if (!emptyCells.isEmpty()) {
            return emptyCells.get(random.nextInt(emptyCells.size()));
        }

        return new int[]{-1, -1}; 
    }
}
