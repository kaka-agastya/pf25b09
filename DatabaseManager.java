// package DatabaseManager; // Asumsi paket jika tidak ada di file.

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

/**
 * Kelas ini mengelola semua interaksi dengan database MySQL.
 * Ini bertanggung jawab untuk otentikasi pengguna dan untuk
 * menyimpan/mengambil riwayat langkah game dalam mode multiplayer berbasis database.
 */
public class DatabaseManager {

    // Konstanta untuk detail koneksi database
    private static final String DB_HOST = "mysql-tictactoee-kakagastya2-ecdb.c.aivencloud.com";
    private static final String DB_PORT = "28606";
    private static final String DB_NAME = "defaultdb";
    private static final String DB_USER = "avnadmin";
    private static final String DB_PASS = "AVNS_bgOBYQNuctKG0ErN3KQ";

    /**
     * Kelas inner statis yang merepresentasikan satu langkah (move) dalam game.
     * Digunakan untuk menyimpan dan mengambil data pergerakan dari database.
     */
    public static class Move {
        public String gameId;         // ID unik untuk game
        public int moveNumber;        // Nomor urut langkah dalam game
        public String playerUsername; // Username pemain yang melakukan langkah
        public String playerSeed;     // Simbol pemain ("X" atau "O")
        public int row;               // Koordinat baris langkah
        public int col;               // Koordinat kolom langkah

        /**
         * Konstruktor untuk objek Move.
         * @param gameId ID unik game.
         * @param moveNumber Nomor langkah dalam game.
         * @param playerUsername Username pemain.
         * @param playerSeed Simbol pemain ("X" atau "O").
         * @param row Koordinat baris.
         * @param col Koordinat kolom.
         */
        public Move(String gameId, int moveNumber, String playerUsername, String playerSeed, int row, int col) {
            this.gameId = gameId;
            this.moveNumber = moveNumber;
            this.playerUsername = playerUsername;
            this.playerSeed = playerSeed;
            this.row = row;
            this.col = col;
        }

        /**
         * Mengembalikan representasi string dari objek Move.
         * Berguna untuk logging atau debugging.
         * @return String representasi langkah.
         */
        @Override
        public String toString() {
            return "Move{" +
                    "gameId='" + gameId + '\'' +
                    ", moveNumber=" + moveNumber +
                    ", playerUsername='" + playerUsername + '\'' +
                    ", playerSeed='" + playerSeed + '\'' +
                    ", row=" + row +
                    ", col=" + col +
                    '}';
        }
    }

    /**
     * Mengambil password dari database untuk username yang diberikan.
     * Detail koneksi database bisa di-override melalui argumen baris perintah.
     * @param uName Username yang ingin dicari password-nya.
     * @param args Argumen baris perintah, bisa berisi -host, -username, -password, -database, -port.
     * @return Password jika ditemukan, string kosong jika tidak atau terjadi error.
     * @throws ClassNotFoundException Jika driver JDBC MySQL tidak ditemukan.
     */
    public static String getPassword(String uName, String[] args) throws ClassNotFoundException {
        String pass = "";
        String host = DB_HOST;
        String port = DB_PORT;
        String databaseName = DB_NAME;
        String userName = DB_USER;
        String dbPassword = DB_PASS;

        // Memparsing argumen baris perintah untuk meng-override detail koneksi
        for (int i = 0; i < args.length; i++) {
            if (i + 1 < args.length) {
                switch (args[i].toLowerCase(Locale.ROOT)) {
                    case "-host": host = args[++i]; break;
                    case "-username": userName = args[++i]; break;
                    case "-password": dbPassword = args[++i]; break;
                    case "-database": databaseName = args[++i]; break;
                    case "-port": port = args[++i]; break;
                }
            }
        }

        // Memeriksa apakah informasi koneksi dasar tersedia
        if (host == null || port == null || databaseName == null) {
            System.out.println("DatabaseManager: Host, port, database information is required.");
            return "";
        }

        // Memuat driver JDBC MySQL
        Class.forName("com.mysql.cj.jdbc.Driver");
        // Query SQL untuk mengambil password berdasarkan username
        String sqlQuery = "SELECT password FROM gameuser WHERE username = ?";

        // Menggunakan try-with-resources untuk memastikan koneksi dan statement ditutup
        try (Connection connection =
                     DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + databaseName + "?sslmode=require", userName, dbPassword);
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, uName); // Mengatur parameter username di query

            // Mengeksekusi query dan memproses hasilnya
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    pass = resultSet.getString("password"); // Mengambil password jika username ditemukan
                } else {
                    System.out.println("DatabaseManager: Username '" + uName + "' not found in database.");
                    pass = ""; // Username tidak ditemukan
                }
            }
        } catch (SQLException e) {
            // Menangani error SQL (koneksi atau query gagal)
            System.out.println("DatabaseManager: Connection or query failed: " + e.getMessage());
            e.printStackTrace();
            pass = "";
        }
        return pass; // Mengembalikan password yang ditemukan atau string kosong
    }

    /**
     * Memasukkan detail langkah pemain ke dalam database.
     * Digunakan dalam mode multiplayer untuk mencatat pergerakan.
     * @param gameId ID unik game.
     * @param moveNumber Nomor langkah dalam game.
     * @param playerUsername Username pemain yang melakukan langkah.
     * @param playerSeed Simbol pemain ("X" atau "O").
     * @param row Koordinat baris langkah.
     * @param col Koordinat kolom langkah.
     * @throws SQLException Jika terjadi error SQL saat menyisipkan data.
     * @throws ClassNotFoundException Jika driver JDBC MySQL tidak ditemukan.
     */
    public static void insertMove(String gameId, int moveNumber, String playerUsername, String playerSeed, int row, int col) throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        // Query SQL untuk menyisipkan langkah baru
        String sqlInsert = "INSERT INTO moves (game_id, move_number, player_username, player_seed, row_coord, col_coord) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection =
                     DriverManager.getConnection("jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?sslmode=require", DB_USER, DB_PASS);
             PreparedStatement preparedStatement = connection.prepareStatement(sqlInsert)) {

            // Mengatur parameter untuk query insert
            preparedStatement.setString(1, gameId);
            preparedStatement.setInt(2, moveNumber);
            preparedStatement.setString(3, playerUsername);
            preparedStatement.setString(4, playerSeed);
            preparedStatement.setInt(5, row);
            preparedStatement.setInt(6, col);

            preparedStatement.executeUpdate(); // Mengeksekusi query
            System.out.println("DatabaseManager: Inserted move: " + gameId + " - " + moveNumber);

        } catch (SQLException e) {
            System.err.println("DatabaseManager: Error inserting move: " + e.getMessage());
            throw e; // Meneruskan exception untuk ditangani oleh pemanggil
        }
    }

    /**
     * Mengambil daftar langkah baru dari database untuk game tertentu.
     * Ini mencari langkah dengan moveNumber yang lebih besar dari lastMoveNumber yang diberikan.
     * Digunakan untuk menyinkronkan state game di mode multiplayer.
     * @param gameId ID unik game.
     * @param lastMoveNumber Nomor langkah terakhir yang diketahui.
     * @return List objek Move baru.
     * @throws SQLException Jika terjadi error SQL saat mengambil data.
     * @throws ClassNotFoundException Jika driver JDBC MySQL tidak ditemukan.
     */
    public static List<Move> fetchMoves(String gameId, int lastMoveNumber) throws SQLException, ClassNotFoundException {
        List<Move> newMoves = new ArrayList<>(); // List untuk menyimpan langkah-langkah baru
        Class.forName("com.mysql.cj.jdbc.Driver");
        // Query SQL untuk mengambil langkah-langkah berdasarkan gameId dan moveNumber
        String sqlSelect = "SELECT game_id, move_number, player_username, player_seed, row_coord, col_coord FROM moves WHERE game_id = ? AND move_number > ? ORDER BY move_number ASC";

        try (Connection connection =
                     DriverManager.getConnection("jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?sslmode=require", DB_USER, DB_PASS);
             PreparedStatement preparedStatement = connection.prepareStatement(sqlSelect)) {

            preparedStatement.setString(1, gameId);
            preparedStatement.setInt(2, lastMoveNumber);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // Mengiterasi hasil query dan membuat objek Move untuk setiap baris
                while (resultSet.next()) {
                    newMoves.add(new Move(
                            resultSet.getString("game_id"),
                            resultSet.getInt("move_number"),
                            resultSet.getString("player_username"),
                            resultSet.getString("player_seed"),
                            resultSet.getInt("row_coord"),
                            resultSet.getInt("col_coord")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("DatabaseManager: Error fetching moves: " + e.getMessage());
            throw e; // Meneruskan exception
        }
        return newMoves; // Mengembalikan list langkah baru
    }

    /**
     * Menghapus semua langkah game dari database untuk game ID tertentu.
     * Ini digunakan saat game baru dibuat untuk memastikan papan bersih.
     * @param gameId ID unik game yang langkah-langkahnya akan dihapus.
     * @throws SQLException Jika terjadi error SQL saat menghapus data.
     * @throws ClassNotFoundException Jika driver JDBC MySQL tidak ditemukan.
     */
    public static void clearGameMoves(String gameId) throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        // Query SQL untuk menghapus langkah-langkah
        String sqlDelete = "DELETE FROM moves WHERE game_id = ?";

        try (Connection connection =
                     DriverManager.getConnection("jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?sslmode=require", DB_USER, DB_PASS);
             PreparedStatement preparedStatement = connection.prepareStatement(sqlDelete)) {

            preparedStatement.setString(1, gameId); // Mengatur parameter gameId
            preparedStatement.executeUpdate(); // Mengeksekusi query
            System.out.println("DatabaseManager: Cleared moves for game ID: " + gameId);

        } catch (SQLException e) {
            System.err.println("DatabaseManager: Error clearing game moves: " + e.getMessage());
            throw e;
        }
    }
}