import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ArrayList; // NEW
import java.util.List;      // NEW


/**
 * Utility class for managing database connections and user authentication.
 * Now also includes methods for game move storage and retrieval for multiplayer.
 */
public class DatabaseManager {


    // Konfigurasi Database - PASTIKAN SESUAI DENGAN KREDENSIAL DATABASE ANDA
    private static final String DB_HOST = "mysql-tictactoee-kakagastya2-ecdb.c.aivencloud.com";
    private static final String DB_PORT = "28606";
    private static final String DB_NAME = "defaultdb";
    private static final String DB_USER = "avnadmin";
    private static final String DB_PASS = "AVNS_bgOBYQNuctKG0ErN3KQ";


    /**
     * Nested class to represent a single game move.
     */
    public static class Move { // Make this static nested class
        public String gameId;
        public int moveNumber;
        public String playerUsername;
        public String playerSeed; // "X" or "O"
        public int row;
        public int col;
        // public java.sql.Timestamp timestamp; // Optional, if needed


        public Move(String gameId, int moveNumber, String playerUsername, String playerSeed, int row, int col) {
            this.gameId = gameId;
            this.moveNumber = moveNumber;
            this.playerUsername = playerUsername;
            this.playerSeed = playerSeed;
            this.row = row;
            this.col = col;
        }


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
     * Retrieves the user's password from the database.
     * Uses PreparedStatement to prevent SQL Injection.
     * Accepts command-line arguments to override database credentials.
     *
     * @param uName The username to check.
     * @param args Command-line arguments for database override.
     * @return The password string if found, empty string otherwise.
     * @throws ClassNotFoundException If the JDBC driver is not found.
     */
    public static String getPassword(String uName, String[] args) throws ClassNotFoundException {
        String pass = "";
        String host = DB_HOST;
        String port = DB_PORT;
        String databaseName = DB_NAME;
        String userName = DB_USER;
        String dbPassword = DB_PASS;


        // Logic for parsing command-line arguments to override DB credentials
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


        if (host == null || port == null || databaseName == null) {
            System.out.println("DatabaseManager: Host, port, database information is required.");
            return "";
        }


        // Ensure JDBC driver is loaded
        Class.forName("com.mysql.cj.jdbc.Driver");
        String sqlQuery = "SELECT password FROM gameuser WHERE username = ?";


        try (Connection connection =
                     DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + databaseName + "?sslmode=require", userName, dbPassword);
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {


            preparedStatement.setString(1, uName);


            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    pass = resultSet.getString("password");
                } else {
                    System.out.println("DatabaseManager: Username '" + uName + "' not found in database.");
                    pass = "";
                }
            }
        } catch (SQLException e) {
            System.out.println("DatabaseManager: Connection or query failed: " + e.getMessage());
            e.printStackTrace();
            pass = "";
        }
        return pass;
    }


    /**
     * Inserts a game move into the 'moves' table.
     * @param gameId Unique ID of the game.
     * @param moveNumber Sequence number of the move within the game.
     * @param playerUsername Username of the player who made the move.
     * @param playerSeed 'X' or 'O' symbol.
     * @param row Row coordinate.
     * @param col Column coordinate.
     * @throws SQLException If a database access error occurs.
     * @throws ClassNotFoundException If the JDBC driver is not found.
     */
    public static void insertMove(String gameId, int moveNumber, String playerUsername, String playerSeed, int row, int col) throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String sqlInsert = "INSERT INTO moves (game_id, move_number, player_username, player_seed, row_coord, col_coord) VALUES (?, ?, ?, ?, ?, ?)";


        try (Connection connection =
                     DriverManager.getConnection("jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?sslmode=require", DB_USER, DB_PASS);
             PreparedStatement preparedStatement = connection.prepareStatement(sqlInsert)) {


            preparedStatement.setString(1, gameId);
            preparedStatement.setInt(2, moveNumber);
            preparedStatement.setString(3, playerUsername);
            preparedStatement.setString(4, playerSeed);
            preparedStatement.setInt(5, row);
            preparedStatement.setInt(6, col);

            preparedStatement.executeUpdate();
            System.out.println("DatabaseManager: Inserted move: " + gameId + " - " + moveNumber);


        } catch (SQLException e) {
            System.err.println("DatabaseManager: Error inserting move: " + e.getMessage());
            throw e; // Re-throw to be handled by caller
        }
    }


    /**
     * Fetches new game moves from the 'moves' table for a specific game.
     * @param gameId Unique ID of the game.
     * @param lastMoveNumber The last move number seen by this client. Fetches moves greater than this.
     * @return A list of new Move objects, ordered by moveNumber.
     * @throws SQLException If a database access error occurs.
     * @throws ClassNotFoundException If the JDBC driver is not found.
     */
    public static List<Move> fetchMoves(String gameId, int lastMoveNumber) throws SQLException, ClassNotFoundException {
        List<Move> newMoves = new ArrayList<>();
        Class.forName("com.mysql.cj.jdbc.Driver");
        String sqlSelect = "SELECT game_id, move_number, player_username, player_seed, row_coord, col_coord FROM moves WHERE game_id = ? AND move_number > ? ORDER BY move_number ASC";


        try (Connection connection =
                     DriverManager.getConnection("jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?sslmode=require", DB_USER, DB_PASS);
             PreparedStatement preparedStatement = connection.prepareStatement(sqlSelect)) {


            preparedStatement.setString(1, gameId);
            preparedStatement.setInt(2, lastMoveNumber);


            try (ResultSet resultSet = preparedStatement.executeQuery()) {
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
            throw e; // Re-throw to be handled by caller
        }
        return newMoves;
    }


    /**
     * Deletes all moves for a specific game from the 'moves' table.
     * This is useful for resetting a game or cleaning up.
     * @param gameId Unique ID of the game to clear.
     * @throws SQLException If a database access error occurs.
     * @throws ClassNotFoundException If the JDBC driver is not found.
     */
    public static void clearGameMoves(String gameId) throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String sqlDelete = "DELETE FROM moves WHERE game_id = ?";


        try (Connection connection =
                     DriverManager.getConnection("jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?sslmode=require", DB_USER, DB_PASS);
             PreparedStatement preparedStatement = connection.prepareStatement(sqlDelete)) {


            preparedStatement.setString(1, gameId);
            preparedStatement.executeUpdate();
            System.out.println("DatabaseManager: Cleared moves for game ID: " + gameId);


        } catch (SQLException e) {
            System.err.println("DatabaseManager: Error clearing game moves: " + e.getMessage());
            throw e;
        }
    }
}



