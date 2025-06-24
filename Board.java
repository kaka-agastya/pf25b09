// package Board; // Asumsi paket jika tidak ada di file.

import java.awt.*;

/**
 * Kelas Board memodelkan papan permainan Tic-Tac-Toe (grid 3x3).
 * Kelas ini bertanggung jawab untuk mengelola state dari masing-masing sel di papan
 * dan menyediakan metode untuk memeriksa kondisi kemenangan atau seri.
 */
public class Board {

   // Konstanta untuk dimensi papan
   public static final int ROWS = 3;  // Jumlah baris
   public static final int COLS = 3;  // Jumlah kolom

   // Konstanta untuk dimensi kanvas gambar papan
   public static final int CANVAS_WIDTH = Cell.SIZE * COLS;   // Lebar kanvas gambar
   public static final int CANVAS_HEIGHT = Cell.SIZE * ROWS;  // Tinggi kanvas gambar
   public static final int GRID_WIDTH = 8;                   // Lebar garis grid
   public static final int GRID_WIDTH_HALF = GRID_WIDTH / 2; // Setengah lebar garis grid
   public static final Color COLOR_GRID = Color.LIGHT_GRAY;  // Warna garis grid
   public static final int Y_OFFSET = 1;                     // Offset Y untuk fine-tuning tampilan

   // Atribut: Array 2D dari objek Cell yang merepresentasikan sel-sel di papan
   Cell[][] cells;

   /**
    * Konstruktor untuk Board.
    * Memanggil metode initGame() untuk menginisialisasi papan.
    */
   public Board() {
      initGame();
   }

   /**
    * Menginisialisasi objek-objek game (dipanggil sekali saat Board dibuat).
    * Mengalokasikan array Cell dan membuat instance Cell untuk setiap posisi.
    */
   public void initGame() {
      cells = new Cell[ROWS][COLS]; // Mengalokasikan array 2D
      for (int row = 0; row < ROWS; ++row) {
         for (int col = 0; col < COLS; ++col) {
            // Mengalokasikan dan menginisialisasi setiap elemen array dengan objek Cell baru
            cells[row][col] = new Cell(row, col);
         }
      }
   }

   /**
    * Mengatur ulang isi papan game ke state awal, siap untuk game baru.
    * Memanggil metode newGame() pada setiap Cell di papan.
    */
   public void newGame() {
      for (int row = 0; row < ROWS; ++row) {
         for (int col = 0; col < COLS; ++col) {
            cells[row][col].newGame(); // Meminta setiap sel untuk membersihkan isinya
         }
      }
   }

   /**
    * Pemain yang diberikan membuat langkah pada sel (selectedRow, selectedCol).
    * Metode ini memperbarui isi sel yang dipilih, lalu menghitung dan mengembalikan
    * state game yang baru (PLAYING, DRAW, CROSS_WON, NOUGHT_WON).
    * @param player Simbol (Seed) pemain yang membuat langkah.
    * @param selectedRow Baris sel yang dipilih.
    * @param selectedCol Kolom sel yang dipilih.
    * @return State objek State yang menunjukkan state game baru.
    */
   public State stepGame(Seed player, int selectedRow, int selectedCol) {
      // Memperbarui papan game dengan langkah pemain
      cells[selectedRow][selectedCol].content = player;

      // Menghitung dan mengembalikan state game yang baru
      // Memeriksa kondisi kemenangan (3-in-a-row, 3-in-a-column, 3-in-a-diagonal)
      if (cells[selectedRow][0].content == player        // 3-sejajar di baris yang sama
              && cells[selectedRow][1].content == player
              && cells[selectedRow][2].content == player
              || cells[0][selectedCol].content == player // 3-sejajar di kolom yang sama
              && cells[1][selectedCol].content == player
              && cells[2][selectedCol].content == player
              || selectedRow == selectedCol              // 3-sejajar di diagonal utama
              && cells[0][0].content == player
              && cells[1][1].content == player
              && cells[2][2].content == player
              || selectedRow + selectedCol == 2          // 3-sejajar di diagonal berlawanan
              && cells[0][2].content == player
              && cells[1][1].content == player
              && cells[2][0].content == player) {
         // Jika ada pemenang, mengembalikan state kemenangan yang sesuai
         return (player == Seed.CROSS) ? State.CROSS_WON : State.NOUGHT_WON;
      } else {
         // Tidak ada pemenang. Memeriksa apakah game DRAW (semua sel terisi) atau PLAYING.
         for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLS; ++col) {
               if (cells[row][col].content == Seed.NO_SEED) {
                  return State.PLAYING; // Masih ada sel kosong, game masih berlanjut
               }
            }
         }
         return State.DRAW; // Tidak ada sel kosong, ini adalah hasil seri
      }
   }

   /**
    * Menggambar papan itu sendiri pada kanvas grafis yang diberikan.
    * Ini menggambar garis grid dan kemudian meminta setiap sel untuk menggambar isinya.
    * @param g Konteks Graphics untuk menggambar.
    */
   public void paint(Graphics g) {
      // Menggambar garis grid
      g.setColor(COLOR_GRID); // Mengatur warna garis grid
      for (int row = 1; row < ROWS; ++row) {
         // Menggambar garis horizontal
         g.fillRoundRect(0, Cell.SIZE * row - GRID_WIDTH_HALF,
                 CANVAS_WIDTH - 1, GRID_WIDTH,
                 GRID_WIDTH, GRID_WIDTH);
      }
      for (int col = 1; col < COLS; ++col) {
         // Menggambar garis vertikal
         g.fillRoundRect(Cell.SIZE * col - GRID_WIDTH_HALF, 0 + Y_OFFSET,
                 GRID_WIDTH, CANVAS_HEIGHT - 1,
                 GRID_WIDTH, GRID_WIDTH);
      }

      // Menggambar semua sel di papan
      for (int row = 0; row < ROWS; ++row) {
         for (int col = 0; col < COLS; ++col) {
            cells[row][col].paint(g); // Meminta setiap sel untuk menggambar isinya sendiri
         }
      }
   }
}