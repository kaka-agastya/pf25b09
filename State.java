// package State; // Asumsi paket jika tidak ada di file.

/**
 * Enum State mendefinisikan berbagai kemungkinan state atau kondisi permainan Tic-Tac-Toe.
 * Ini mencakup state ketika permainan sedang berlangsung, seri,
 * atau ketika salah satu pemain menang.
 */
public enum State {
    // Definisi konstanta enum dengan nama tampilan masing-masing
    PLAYING("Playing"),       // Permainan sedang berlangsung
    DRAW("It's a Draw"),      // Hasil seri
    CROSS_WON("'X' Won"),     // Pemain 'X' menang
    NOUGHT_WON("'O' Won");    // Pemain 'O' menang

    // Atribut pribadi untuk menyimpan nama tampilan dari setiap state
    private String displayName;

    /**
     * Konstruktor pribadi untuk enum State.
     * Menginisialisasi displayName untuk setiap state.
     */
    private State(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Mengembalikan nama tampilan dari state ini.
     */
    public String getDisplayName() {
        return displayName;
    }
}