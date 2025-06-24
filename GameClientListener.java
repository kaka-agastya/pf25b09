// package GameClientListener; // Asumsi paket jika tidak ada di file.

/**
 * Antarmuka GameClientListener mendefinisikan serangkaian metode callback
 * yang harus diimplementasikan oleh kelas mana pun yang ingin mendengarkan
 * peristiwa (event) yang berasal dari GameClient.
 * Ini memungkinkan decoupling antara GameClient dan komponen UI/logika game.
 */
public interface GameClientListener {
    /**
     * Dipanggil ketika klien berhasil terhubung ke server.
     * @param message Pesan status koneksi.
     */
    void onConnected(String message);

    /**
     * Dipanggil ketika klien terputus dari server.
     * @param message Pesan status putus koneksi.
     */
    void onDisconnected(String message);

    /**
     * Dipanggil ketika server menetapkan peran pemain (misalnya, "X" atau "O").
     * @param role Peran pemain yang ditetapkan.
     */
    void onPlayerAssigned(String role);

    /**
     * Dipanggil ketika giliran dalam game berubah.
     * @param isMyTurn True jika sekarang giliran pemain lokal, false sebaliknya.
     * @param playerSymbol Simbol pemain yang sedang giliran (bisa null jika tidak relevan).
     */
    void onTurnChange(boolean isMyTurn, String playerSymbol);

    /**
     * Dipanggil ketika pergerakan lawan diterima dari server.
     * @param playerSymbol Simbol pemain yang melakukan pergerakan.
     * @param row Baris pergerakan.
     * @param col Kolom pergerakan.
     */
    void onMoveReceived(String playerSymbol, int row, int col);

    /**
     * Dipanggil ketika game berakhir (menang, kalah, atau seri).
     * @param finalState State akhir dari game.
     */
    void onGameOver(State finalState);

    /**
     * Dipanggil ketika pergerakan yang dilakukan klien dianggap tidak valid oleh server.
     * @param message Pesan yang menjelaskan mengapa pergerakan tidak valid.
     */
    void onInvalidMove(String message);

    /**
     * Dipanggil untuk pesan umum yang diterima dari server.
     * @param message Pesan dari server.
     */
    void onMessage(String message);

    /**
     * Dipanggil jika terjadi kesalahan saat mencoba terhubung ke server.
     * @param message Pesan yang menjelaskan kesalahan koneksi.
     */
    void onConnectionError(String message);
}