

import java.awt.*;

public class Board {
   public static final int ROWS = 3;  
   public static final int COLS = 3;

   public static final int CANVAS_WIDTH = Cell.SIZE * COLS;  
   public static final int CANVAS_HEIGHT = Cell.SIZE * ROWS;
   public static final int GRID_WIDTH = 8;  
   public static final int GRID_WIDTH_HALF = GRID_WIDTH / 2; 
   public static final Color COLOR_GRID = Color.LIGHT_GRAY;  
   public static final int Y_OFFSET = 1;  

   Cell[][] cells;

   public Board() {
      initGame();
   }

   public void initGame() {
      cells = new Cell[ROWS][COLS]; 
      for (int row = 0; row < ROWS; ++row) {
         for (int col = 0; col < COLS; ++col) {
            cells[row][col] = new Cell(row, col);
         }
      }
   }


   public void newGame() {
      for (int row = 0; row < ROWS; ++row) {
         for (int col = 0; col < COLS; ++col) {
            cells[row][col].newGame(); 
         }
      }
   }
   public State stepGame(Seed player, int selectedRow, int selectedCol) {

      cells[selectedRow][selectedCol].content = player;


      if (cells[selectedRow][0].content == player  
              && cells[selectedRow][1].content == player
              && cells[selectedRow][2].content == player
              || cells[0][selectedCol].content == player 
              && cells[1][selectedCol].content == player
              && cells[2][selectedCol].content == player
              || selectedRow == selectedCol     
              && cells[0][0].content == player
              && cells[1][1].content == player
              && cells[2][2].content == player
              || selectedRow + selectedCol == 2 
              && cells[0][2].content == player
              && cells[1][1].content == player
              && cells[2][0].content == player) {
         return (player == Seed.CROSS) ? State.CROSS_WON : State.NOUGHT_WON;
      } else {
         for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLS; ++col) {
               if (cells[row][col].content == Seed.NO_SEED) {
                  return State.PLAYING; 
               }
            }
         }
         return State.DRAW; 
      }
   }


   public void paint(Graphics g) {
      // Draw the grid-lines
      g.setColor(COLOR_GRID);
      for (int row = 1; row < ROWS; ++row) {
         g.fillRoundRect(0, Cell.SIZE * row - GRID_WIDTH_HALF,
                 CANVAS_WIDTH - 1, GRID_WIDTH,
                 GRID_WIDTH, GRID_WIDTH);
      }
      for (int col = 1; col < COLS; ++col) {
         g.fillRoundRect(Cell.SIZE * col - GRID_WIDTH_HALF, 0 + Y_OFFSET,
                 GRID_WIDTH, CANVAS_HEIGHT - 1,
                 GRID_WIDTH, GRID_WIDTH);
      }

      for (int row = 0; row < ROWS; ++row) {
         for (int col = 0; col < COLS; ++col) {
            cells[row][col].paint(g);  
         }
      }
   }
}

