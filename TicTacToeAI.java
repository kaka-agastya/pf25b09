// package TicTacToeAI; // Asumsi paket jika tidak ada di file.

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Kelas TicTacToeAI berisi algoritma kecerdasan buatan (AI) untuk bermain Tic-Tac-Toe.
 * Ini menentukan langkah terbaik yang harus dilakukan AI berdasarkan state papan saat ini,
 * dengan prioritas untuk memenangkan game, memblokir lawan, mengambil posisi strategis,
 * atau memilih langkah acak jika tidak ada pilihan yang lebih baik.
 */
public class TicTacToeAI {
    private Random random = new Random(); // Digunakan untuk memilih langkah acak

    /**
     * Mencari langkah terbaik yang harus diambil oleh AI pada papan permainan saat ini.
     * Algoritma ini memiliki prioritas sebagai berikut:
     * 1. Menang: Jika AI bisa menang dalam satu langkah.
     * 2. Blokir: Jika lawan bisa menang dalam satu langkah, AI akan memblokirnya.
     * 3. Tengah: Ambil posisi tengah (jika kosong).
     * 4. Sudut: Ambil posisi sudut (jika kosong).
     * 5. Sisi: Ambil posisi sisi acak (jika kosong).
     * @param currentBoard Objek Board yang merepresentasikan papan permainan saat ini.
     * @param aiPlayerSeed Simbol (Seed) AI (CROSS atau NOUGHT).
     * @return Array int[] berukuran 2 ({baris, kolom}) dari langkah terbaik, atau {-1, -1} jika tidak ada langkah valid.
     */
    public int[] findBestMove(Board currentBoard, Seed aiPlayerSeed) {
        // Menentukan simbol lawan
        Seed opponentSeed = (aiPlayerSeed == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;

        // Strategi 1: Cek apakah AI bisa menang dalam satu langkah
        // AI mencoba setiap sel kosong dengan simbolnya sendiri
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                if (currentBoard.cells[r][c].content == Seed.NO_SEED) { // Jika sel kosong
                    currentBoard.cells[r][c].content = aiPlayerSeed; // Coba letakkan simbol AI
                    // Periksa apakah langkah ini menghasilkan kemenangan bagi AI
                    if (currentBoard.stepGame(aiPlayerSeed, r, c) == (aiPlayerSeed == Seed.CROSS ? State.CROSS_WON : State.NOUGHT_WON)) {
                        currentBoard.cells[r][c].content = Seed.NO_SEED; // Batalkan perubahan pada papan
                        return new int[]{r, c}; // Mengembalikan langkah kemenangan
                    }
                    currentBoard.cells[r][c].content = Seed.NO_SEED; // Batalkan perubahan pada papan
                }
            }
        }

        // Strategi 2: Cek apakah lawan bisa menang dalam satu langkah dan blokir
        // AI mencoba setiap sel kosong dengan simbol lawan
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                if (currentBoard.cells[r][c].content == Seed.NO_SEED) { // Jika sel kosong
                    currentBoard.cells[r][c].content = opponentSeed; // Coba letakkan simbol lawan
                    // Periksa apakah langkah ini akan membuat lawan menang
                    if (currentBoard.stepGame(opponentSeed, r, c) == (opponentSeed == Seed.CROSS ? State.CROSS_WON : State.NOUGHT_WON)) {
                        currentBoard.cells[r][c].content = Seed.NO_SEED; // Batalkan perubahan pada papan
                        return new int[]{r, c}; // Mengembalikan langkah untuk memblokir lawan
                    }
                    currentBoard.cells[r][c].content = Seed.NO_SEED; // Batalkan perubahan pada papan
                }
            }
        }

        // Strategi 3: Ambil posisi tengah jika kosong
        if (currentBoard.cells[1][1].content == Seed.NO_SEED) {
            return new int[]{1, 1}; // Mengembalikan posisi tengah
        }

        // Strategi 4: Ambil posisi sudut jika kosong
        int[][] corners = {{0, 0}, {0, 2}, {2, 0}, {2, 2}}; // Koordinat semua sudut
        for (int[] corner : corners) {
            if (currentBoard.cells[corner[0]][corner[1]].content == Seed.NO_SEED) { // Jika sudut kosong
                return corner; // Mengembalikan posisi sudut
            }
        }

        // Strategi 5: Ambil posisi sisi acak jika tidak ada pilihan di atas
        List<int[]> emptyCells = new ArrayList<>(); // List untuk menyimpan semua sel kosong
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                if (currentBoard.cells[r][c].content == Seed.NO_SEED) {
                    emptyCells.add(new int[]{r, c}); // Menambahkan sel kosong ke list
                }
            }
        }
        if (!emptyCells.isEmpty()) {
            // Mengembalikan sel kosong acak dari list
            return emptyCells.get(random.nextInt(emptyCells.size()));
        }

        return new int[]{-1, -1}; // Mengembalikan {-1, -1} jika tidak ada langkah valid (papan penuh)
    }
}