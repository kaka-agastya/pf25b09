import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

public class DatabaseManager {

    private static final String DB_HOST = "mysql-tictactoee-kakagastya2-ecdb.c.aivencloud.com";
    private static final String DB_PORT = "28606";
    private static final String DB_NAME = "defaultdb";
    private static final String DB_USER = "avnadmin";
    private static final String DB_PASS = "AVNS_bgOBYQNuctKG0ErN3KQ";

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
}
