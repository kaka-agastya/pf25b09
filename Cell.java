// package Cell; // Asumsi paket jika tidak ada di file.

import java.awt.*;

/**
 * Kelas Cell memodelkan satu sel individual pada papan permainan Tic-Tac-Toe.
 * Setiap sel memiliki posisi (baris dan kolom) dan konten (simbol pemain atau kosong).
 */
public class Cell {

   public static final int SIZE = 120; // Ukuran (lebar/tinggi) sel dalam piksel
   public static final int PADDING = SIZE / 5; // Padding simbol di dalam sel
   public static final int SEED_SIZE = SIZE - PADDING * 2; // Ukuran simbol (X atau O)

   // Atribut: konten sel dan posisinya
   Seed content; // Isi sel: CROSS, NOUGHT, atau NO_SEED
   int row, col; // Koordinat baris dan kolom sel

   /**
    * Konstruktor untuk Cell.
    * Menginisialisasi sel dengan baris dan kolom yang ditentukan
    * dan mengatur konten awalnya ke NO_SEED (kosong).
    * @param row Baris sel.
    * @param col Kolom sel.
    */
   public Cell(int row, int col) {
      this.row = row;
      this.col = col;
      content = Seed.NO_SEED; // Sel awalnya kosong
   }

   /**
    * Mengatur ulang konten sel ke NO_SEED (kosong), siap untuk game baru.
    */
   public void newGame() {
      content = Seed.NO_SEED;
   }

   /**
    * Menggambar konten sel (X atau O) pada konteks grafis yang diberikan.
    * @param g Konteks Graphics untuk menggambar.
    */
   public void paint(Graphics g) {
      // Menghitung koordinat X dan Y untuk menggambar simbol di dalam sel
      int x1 = col * SIZE + PADDING;
      int y1 = row * SIZE + PADDING;
      // Hanya menggambar jika sel tidak kosong
      if (content == Seed.CROSS || content == Seed.NOUGHT) {
         // Menggambar gambar simbol (CROSS atau NOUGHT) di dalam sel
         // Ukuran gambar disesuaikan dengan SYMBOL_SIZE
         g.drawImage(content.getImage(), x1, y1, SEED_SIZE, SEED_SIZE, null);
      }
   }
}