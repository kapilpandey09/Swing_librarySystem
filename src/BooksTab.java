import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.*;

public class BooksTab {
    

    public static JPanel build() {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        
        String[] cols = { "ID", "Title", "Author", "Genre", "Year", "Status" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            } 
        };
        JTable table = UIHelper.styledTable(model);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) {
                    setBackground("Issued".equals(t.getModel().getValueAt(row, 5))
                            ? new Color(255, 220, 220)
                            : Color.WHITE);
                }
                return this;
            }
        });

        int[] widths = { 40, 230, 170, 110, 60, 110 };
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JLabel statusBar = new JLabel("  Total Books: 0");
        statusBar.setFont(new Font("SansSerif", Font.ITALIC, 12));

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        btns.setBackground(new Color(230, 240, 255));
        btns.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(180, 200, 220)));

        JButton btnAdd = UIHelper.makeBtn("+ Add Book", new Color(34, 120, 60));
        JButton btnEdit = UIHelper.makeBtn("✏ Edit", new Color(180, 120, 0));
        JButton btnDelete = UIHelper.makeBtn("- Delete", new Color(178, 34, 34));
        JButton btnRefresh = UIHelper.makeBtn("↺ Refresh", new Color(80, 80, 80));

        btns.add(btnAdd);
        btns.add(btnEdit);
        btns.add(btnDelete);
        btns.add(btnRefresh);

        JPanel south = new JPanel(new BorderLayout());
        south.add(btns, BorderLayout.CENTER);
        south.add(statusBar, BorderLayout.SOUTH);
        panel.add(south, BorderLayout.SOUTH);

    
        loadBooks(model, statusBar);

        
        btnAdd.addActionListener(e -> addBookDialog(null, model, statusBar));

        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                UIHelper.msg("Please select a book to edit.");
                return;
            }
            editBookDialog(null, model, statusBar,
                    (int) model.getValueAt(row, 0), // id
                    (String) model.getValueAt(row, 1), // title
                    (String) model.getValueAt(row, 2), // author
                    (String) model.getValueAt(row, 3), // genre
                    String.valueOf(model.getValueAt(row, 4))); // year
        });

        // Delete Book
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                UIHelper.msg("Please select a book.");
                return;
            }
            int id = (int) model.getValueAt(row, 0);
            String title = (String) model.getValueAt(row, 1);
            try {
                ResultSet rs = DBHelper.conn.createStatement().executeQuery(
                        "SELECT COUNT(*) FROM transactions WHERE book_id = " + id);
                if (rs.next() && rs.getInt(1) > 0) {
                    UIHelper.msg("\"" + title + "\"\nTransactions exist ");
                    return;
                }
                if (UIHelper.confirm("Delete book: \"" + title + "\"?")) {
                    DBHelper.runSQL("DELETE FROM books WHERE id=" + id);
                    loadBooks(model, statusBar);
                }
            } catch (SQLException ex) {
                UIHelper.msg("Error: " + ex.getMessage());
            }
        });

    
        btnRefresh.addActionListener(e -> loadBooks(model, statusBar));

        return panel;
    }

    public static void loadBooks(DefaultTableModel model, JLabel statusBar) {
        model.setRowCount(0); 
        try {
            ResultSet rs = DBHelper.conn.createStatement()
                    .executeQuery("SELECT * FROM books ORDER BY id");
            int count = 0;
            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("genre"),
                        rs.getInt("year"),
                        rs.getString("status")
                });
                count++;
            }
            statusBar.setText("  Total Books: " + count);
        } catch (SQLException e) {
            UIHelper.msg("Load Error: " + e.getMessage());
        }
    }

    static void addBookDialog(JFrame parent, DefaultTableModel model, JLabel statusBar) {
        JDialog d = new JDialog(parent, "Add New Book", true);
        d.setSize(400, 310);
        d.setLocationRelativeTo(parent);
        d.setLayout(new BorderLayout());
        d.add(UIHelper.dialogHeader("Add New Book", new Color(34, 120, 60)), BorderLayout.NORTH);

        // Form fields
        JTextField tfTitle = new JTextField();
        JTextField tfAuthor = new JTextField();
        JTextField tfGenre = new JTextField();
        JTextField tfYear = new JTextField();

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 12));
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 10, 25));
        form.add(UIHelper.boldLabel("Title *:"));
        form.add(tfTitle);
        form.add(UIHelper.boldLabel("Author *:"));
        form.add(tfAuthor);
        form.add(UIHelper.boldLabel("Genre:"));
        form.add(tfGenre);
        form.add(UIHelper.boldLabel("Year:"));
        form.add(tfYear);
        d.add(form, BorderLayout.CENTER);

        JButton saveBtn = UIHelper.makeBtn("Save", new Color(34, 120, 60));
        JPanel bp = new JPanel();
        bp.add(saveBtn);
        d.add(bp, BorderLayout.SOUTH);

        saveBtn.addActionListener(e -> {
            // Validation
            if (tfTitle.getText().trim().isEmpty() || tfAuthor.getText().trim().isEmpty()) {
                UIHelper.msg("Title & Author are required!");
                return;
            }
            int year = 0;
            try {
                if (!tfYear.getText().trim().isEmpty())
                    year = Integer.parseInt(tfYear.getText().trim());
            } catch (NumberFormatException ex) {
                UIHelper.msg("Year must be a number!");
                return;
            }
      
            try {
                PreparedStatement ps = DBHelper.conn.prepareStatement(
                        "INSERT INTO books(title, author, genre, year) VALUES(?, ?, ?, ?)");
                ps.setString(1, tfTitle.getText().trim());
                ps.setString(2, tfAuthor.getText().trim());
                ps.setString(3, tfGenre.getText().trim());
                ps.setInt(4, year);
                ps.executeUpdate();
                UIHelper.msg("Book added successfully!");
                loadBooks(model, statusBar);
                d.dispose();
            } catch (SQLException ex) {
                UIHelper.msg("Error: " + ex.getMessage());
            }
        });

        d.setVisible(true);
    }

    static void editBookDialog(JFrame parent, DefaultTableModel model, JLabel statusBar,
            int id, String title, String author, String genre, String year) {
        JDialog d = new JDialog(parent, "Edit Book", true);
        d.setSize(400, 310);
        d.setLocationRelativeTo(parent);
        d.setLayout(new BorderLayout());
        d.add(UIHelper.dialogHeader("Edit Book", new Color(180, 120, 0)), BorderLayout.NORTH);


        JTextField tfTitle = new JTextField(title);
        JTextField tfAuthor = new JTextField(author);
        JTextField tfGenre = new JTextField(genre);
        JTextField tfYear = new JTextField(year.equals("0") ? "" : year);

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 12));
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 10, 25));
        form.add(UIHelper.boldLabel("Title *:"));
        form.add(tfTitle);
        form.add(UIHelper.boldLabel("Author *:"));
        form.add(tfAuthor);
        form.add(UIHelper.boldLabel("Genre:"));
        form.add(tfGenre);
        form.add(UIHelper.boldLabel("Year:"));
        form.add(tfYear);
        d.add(form, BorderLayout.CENTER);

        JButton updateBtn = UIHelper.makeBtn("Update", new Color(180, 120, 0));
        JPanel bp = new JPanel();
        bp.add(updateBtn);
        d.add(bp, BorderLayout.SOUTH);

        updateBtn.addActionListener(e -> {
            if (tfTitle.getText().trim().isEmpty() || tfAuthor.getText().trim().isEmpty()) {
                UIHelper.msg("Title & Author are required!");
                return;
            }
            int yr = 0;
            try {
                if (!tfYear.getText().trim().isEmpty())
                    yr = Integer.parseInt(tfYear.getText().trim());
            } catch (NumberFormatException ex) {
                UIHelper.msg("Year must be a number!");
                return;
            }
            try {
                PreparedStatement ps = DBHelper.conn.prepareStatement(
                        "UPDATE books SET title=?, author=?, genre=?, year=? WHERE id=?");
                ps.setString(1, tfTitle.getText().trim());
                ps.setString(2, tfAuthor.getText().trim());
                ps.setString(3, tfGenre.getText().trim());
                ps.setInt(4, yr);
                ps.setInt(5, id);
                ps.executeUpdate();
                UIHelper.msg("Book updated!");
                loadBooks(model, statusBar);
                d.dispose();
            } catch (SQLException ex) {
                UIHelper.msg("Error: " + ex.getMessage());
            }
        });

        d.setVisible(true);
    }
}
