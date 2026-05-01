import java.io.File;
import java.sql.*;
import javax.swing.JOptionPane;

public class DBHelper {

    public static Connection conn;

    public static void connectDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            String dbPath = System.getProperty("user.dir") + File.separator + "library.db";
            System.out.println("DB Path: " + dbPath);
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            conn.createStatement().execute("PRAGMA foreign_keys = ON");
            System.out.println("Database Connected!");
        } catch (ClassNotFoundException | SQLException e) {
            JOptionPane.showMessageDialog(null, "DB Error: " + e.getMessage());
            System.exit(1);
        }
    }

    public static void createTables() {
        try {
            Statement s = conn.createStatement();

            s.execute("CREATE TABLE IF NOT EXISTS books (" +
                "id      INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title   TEXT NOT NULL," +
                "author  TEXT NOT NULL," +
                "genre   TEXT," +
                "year    INTEGER," +
                "status  TEXT DEFAULT 'Available')");

            s.execute("CREATE TABLE IF NOT EXISTS members (" +
                "id      INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name    TEXT NOT NULL," +
                "phone   TEXT," +
                "email   TEXT," +
                "address TEXT," +
                "joined  TEXT)");

            s.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                "id          INTEGER PRIMARY KEY AUTOINCREMENT," +
                "book_id     INTEGER NOT NULL," +
                "member_id   INTEGER NOT NULL," +
                "borrow_date TEXT NOT NULL," +
                "return_date TEXT," +       
                "status      TEXT DEFAULT 'Borrowed'," +
                "FOREIGN KEY(book_id)   REFERENCES books(id)," +
                "FOREIGN KEY(member_id) REFERENCES members(id))");

            System.out.println("All 3 tables ready!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Table Error: " + e.getMessage());
        }
    }

    public static void runSQL(String sql) {
        try {
            conn.createStatement().executeUpdate(sql);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "SQL Error: " + e.getMessage());
        }
    }
}
