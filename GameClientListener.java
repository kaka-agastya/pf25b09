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
     */
    void onConnected(String message);

    /**
     * Dipanggil ketika klien terputus dari server.
     */
    void onDisconnected(String message);

    /**
     * Dipanggil ketika server menetapkan peran pemain (misalnya, "X" atau "O").
     */
    void onPlayerAssigned(String role);

    /**
     * Dipanggil ketika giliran dalam game berubah.
     */
    void onTurnChange(boolean isMyTurn, String playerSymbol);

    /**
     * Dipanggil ketika pergerakan lawan diterima dari server.
     */
    void onMoveReceived(String playerSymbol, int row, int col);

    /**
     * Dipanggil ketika game berakhir (menang, kalah, atau seri).
     */
    void onGameOver(State finalState);

    /**
     * Dipanggil ketika pergerakan yang dilakukan klien dianggap tidak valid oleh server.
     */
    void onInvalidMove(String message);

    /**
     * Dipanggil untuk pesan umum yang diterima dari server.
     */
    void onMessage(String message);

    /**
     * Dipanggil jika terjadi kesalahan saat mencoba terhubung ke server.
     */
    void onConnectionError(String message);
}