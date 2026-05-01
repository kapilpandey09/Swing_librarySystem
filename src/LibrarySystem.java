public class LibrarySystem {

    public static void main(String[] args) {

        DBHelper.connectDatabase();

        DBHelper.createTables();

        LoginFrame.show();
    }
}
