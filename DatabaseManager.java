import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DatabaseManager {
    private static final String DB_HOST = "mysql-tictactoee-kakagastya2-ecdb.c.aivencloud.com";
    private static final String DB_PORT = "28606";
    private static final String DB_NAME = "defaultdb";
    private static final String DB_USER = "avnadmin";
    private static final String DB_PASS = "AVNS_bgOBYQNuctKG0ErN3KQ";

    public static class Move {
        public String gameId;
        public int moveNumber;
        public String playerUsername;
        public String playerSeed;
        public int row;
        public int col;

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

    public static boolean testConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?sslmode=require",
                    DB_USER, DB_PASS)) {
                System.out.println("DatabaseManager: Connection successful!");
                return true;
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("DatabaseManager: Connection test failed: " + e.getMessage());
            return false;
        }
    }

    public static String getPassword(String uName, String[] args) throws ClassNotFoundException {
        String pass = "";
        String host = DB_HOST;
        String port = DB_PORT;
        String databaseName = DB_NAME;
        String userName = DB_USER;
        String dbPassword = DB_PASS;

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

        Class.forName("com.mysql.cj.jdbc.Driver");
        String sqlQuery = "SELECT password FROM gameuser WHERE username = ?";

        try (Connection connection =
                     DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + databaseName + "?sslmode=require", userName, dbPassword);
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, uName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    pass = resultSet.getString("password");
                    System.out.println("DatabaseManager: Password retrieved for username: " + uName);
                } else {
                    System.out.println("DatabaseManager: Username '" + uName + "' not found in database.");
                }
            }
        } catch (SQLException e) {
            System.err.println("DatabaseManager: Connection or query failed: " + e.getMessage());
            pass = "";
        }
        return pass;
    }

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
            System.out.println("DatabaseManager: Inserted move: gameId=" + gameId + ", moveNumber=" + moveNumber);
        } catch (SQLException e) {
            System.err.println("DatabaseManager: Error inserting move: " + e.getMessage());
            throw e;
        }
    }

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
                System.out.println("DatabaseManager: Fetched " + newMoves.size() + " moves for gameId=" + gameId);
            }
        } catch (SQLException e) {
            System.err.println("DatabaseManager: Error fetching moves: " + e.getMessage());
            throw e;
        }
        return newMoves;
    }

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