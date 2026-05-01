import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.table.*;

public class TransactionsTab {


    public static JPanel build() {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Table Setup 
        String[] cols = { "Txn ID", "Book Title", "Member Name", "Borrow Date", "Return Date", "Status" };
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
                    setBackground("Borrowed".equals(t.getModel().getValueAt(row, 5))
                            ? new Color(255, 240, 200) 
                            : new Color(220, 255, 220)); 
                }
                return this;
            }
        });

        // Column widths
        int[] widths = { 60, 230, 170, 110, 110, 90 };
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JLabel statusBar = new JLabel("  Total Transactions: 0");
        statusBar.setFont(new Font("SansSerif", Font.ITALIC, 12));

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Buttons 
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        btns.setBackground(new Color(230, 240, 255));
        btns.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(180, 200, 220)));

        JButton btnIssue = UIHelper.makeBtn("📤 Issue Book", new Color(200, 100, 0));
        JButton btnReturn = UIHelper.makeBtn("📥 Return Book", new Color(50, 100, 180));
        JButton btnRefresh = UIHelper.makeBtn("↺ Refresh", new Color(80, 80, 80));

        btns.add(btnIssue);
        btns.add(btnReturn);
        btns.add(btnRefresh);

        JPanel south = new JPanel(new BorderLayout());
        south.add(btns, BorderLayout.CENTER);
        south.add(statusBar, BorderLayout.SOUTH);
        panel.add(south, BorderLayout.SOUTH);

        loadTransactions(model, statusBar);

        // Button Actions 
        btnIssue.addActionListener(e -> issueBookDialog(model, statusBar));

        btnReturn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                UIHelper.msg("Please select a transaction to return.");
                return;
            }
            if ("Returned".equals(model.getValueAt(row, 5))) {
                UIHelper.msg("This book is already returned!");
                return;
            }
            int txnId = (int) model.getValueAt(row, 0);
            String title = (String) model.getValueAt(row, 1);
            if (UIHelper.confirm("Mark as returned:\n\"" + title + "\"?")) {
                returnBook(txnId, model, statusBar);
            }
        });

        btnRefresh.addActionListener(e -> loadTransactions(model, statusBar));

        return panel;
    }
    public static void loadTransactions(DefaultTableModel model, JLabel statusBar) {
        model.setRowCount(0);
        try {
            String sql = "SELECT t.id, b.title, m.name, t.borrow_date, " +
                    "COALESCE(t.return_date, 'Not Returned') as return_date, t.status " +
                    "FROM transactions t " +
                    "JOIN books   b ON t.book_id   = b.id " +
                    "JOIN members m ON t.member_id = m.id " +
                    "ORDER BY t.id DESC";
            ResultSet rs = DBHelper.conn.createStatement().executeQuery(sql);
            int count = 0;
            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("name"),
                        rs.getString("borrow_date"),
                        rs.getString("return_date"),
                        rs.getString("status")
                });
                count++;
            }
            statusBar.setText("  Total Transactions: " + count);
        } catch (SQLException e) {
            UIHelper.msg("Load Error: " + e.getMessage());
        }
    }

    static void issueBookDialog(DefaultTableModel txnModel, JLabel statusBar) {
        JDialog dialog = new JDialog((JFrame) null, "Issue Book to Member", true);
        dialog.setSize(480, 320);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new BorderLayout());

        JLabel hdr = new JLabel("  Issue Book to Member", SwingConstants.LEFT);
        hdr.setFont(new Font("SansSerif", Font.BOLD, 16));
        hdr.setForeground(Color.WHITE);
        hdr.setOpaque(true);
        hdr.setBackground(new Color(200, 100, 0));
        hdr.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 0));
        dialog.add(hdr, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 14));
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 10, 25));

        // Member dropdown
        JComboBox<String> memberBox = new JComboBox<>();
        java.util.List<Integer> memberIds = new java.util.ArrayList<>();
        try {
            ResultSet rs = DBHelper.conn.createStatement()
                    .executeQuery("SELECT id, name FROM members ORDER BY name");
            while (rs.next()) {
                memberBox.addItem(rs.getString("name"));
                memberIds.add(rs.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Book dropdown (only Available)
        JComboBox<String> bookBox = new JComboBox<>();
        java.util.List<Integer> bookIds = new java.util.ArrayList<>();
        try {
            ResultSet rs = DBHelper.conn.createStatement().executeQuery(
                    "SELECT id, title FROM books WHERE status='Available' ORDER BY title");
            while (rs.next()) {
                bookBox.addItem(rs.getString("title"));
                bookIds.add(rs.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JTextField dateField = new JTextField(LocalDate.now().toString());

        form.add(UIHelper.boldLabel("Select Member:"));
        form.add(memberBox);
        form.add(UIHelper.boldLabel("Select Book:"));
        form.add(bookBox);
        form.add(UIHelper.boldLabel("Borrow Date:"));
        form.add(dateField);
        dialog.add(form, BorderLayout.CENTER);

        JButton issueBtn = UIHelper.makeBtn("Issue Book", new Color(200, 100, 0));
        JPanel btnPanel = new JPanel();
        btnPanel.add(issueBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        issueBtn.addActionListener(e -> {
            if (memberIds.isEmpty()) {
                UIHelper.msg("No members found! Add members first.");
                return;
            }
            if (bookIds.isEmpty()) {
                UIHelper.msg("No available books found!");
                return;
            }

            int memberId = memberIds.get(memberBox.getSelectedIndex());
            int bookId = bookIds.get(bookBox.getSelectedIndex());
            String date = dateField.getText().trim();

            try {
            
                PreparedStatement ps = DBHelper.conn.prepareStatement(
                        "INSERT INTO transactions (book_id, member_id, borrow_date, status) VALUES (?,?,?,'Borrowed')");
                ps.setInt(1, bookId);
                ps.setInt(2, memberId);
                ps.setString(3, date);
                ps.executeUpdate();

             
                DBHelper.runSQL("UPDATE books SET status='Issued' WHERE id=" + bookId);

                UIHelper.msg("Book issued successfully!\n" +
                        "Member: " + memberBox.getSelectedItem() +
                        "\nBook: " + bookBox.getSelectedItem() +
                        "\nDate: " + date);
                loadTransactions(txnModel, statusBar);
                dialog.dispose();
            } catch (SQLException ex) {
                UIHelper.msg("Error: " + ex.getMessage());
            }
        });

        dialog.setVisible(true);
    }

 
    static void returnBook(int txnId, DefaultTableModel txnModel, JLabel statusBar) {
        String today = LocalDate.now().toString();
        try {
            ResultSet rs = DBHelper.conn.createStatement()
                    .executeQuery("SELECT book_id FROM transactions WHERE id=" + txnId);
            if (rs.next()) {
                int bookId = rs.getInt("book_id");

                PreparedStatement ps = DBHelper.conn.prepareStatement(
                        "UPDATE transactions SET status='Returned', return_date=? WHERE id=?");
                ps.setString(1, today);
                ps.setInt(2, txnId);
                ps.executeUpdate();

                DBHelper.runSQL("UPDATE books SET status='Available' WHERE id=" + bookId);

                UIHelper.msg("Book returned successfully!\nReturn Date: " + today);
                loadTransactions(txnModel, statusBar);
            }
        } catch (SQLException e) {
            UIHelper.msg("Error: " + e.getMessage());
        }
    }

    public static void showMemberHistory(int memberId, String memberName) {
        JDialog dialog = new JDialog((JFrame) null, "History: " + memberName, true);
        dialog.setSize(700, 380);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new BorderLayout());

        JLabel hdr = new JLabel("  Borrow History: " + memberName, SwingConstants.LEFT);
        hdr.setFont(new Font("SansSerif", Font.BOLD, 15));
        hdr.setForeground(Color.WHITE);
        hdr.setOpaque(true);
        hdr.setBackground(new Color(50, 100, 180));
        hdr.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 0));
        dialog.add(hdr, BorderLayout.NORTH);

        String[] cols = { "Txn ID", "Book Title", "Author", "Borrow Date", "Return Date", "Status" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = UIHelper.styledTable(model);

        try {
            PreparedStatement ps = DBHelper.conn.prepareStatement(
                    "SELECT t.id, b.title, b.author, t.borrow_date, t.return_date, t.status " +
                            "FROM transactions t JOIN books b ON t.book_id = b.id " +
                            "WHERE t.member_id = ? ORDER BY t.borrow_date DESC");
            ps.setInt(1, memberId);
            ResultSet rs = ps.executeQuery();
            int count = 0;
            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("borrow_date"),
                        rs.getString("return_date") == null ? "Not Returned" : rs.getString("return_date"),
                        rs.getString("status")
                });
                count++;
            }
            JLabel info = new JLabel("  Total borrowed: " + count + " book(s)");
            info.setFont(new Font("SansSerif", Font.ITALIC, 12));
            info.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 0));
            dialog.add(info, BorderLayout.SOUTH);
        } catch (SQLException e) {
            UIHelper.msg("Error: " + e.getMessage());
        }

        dialog.add(new JScrollPane(table), BorderLayout.CENTER);
        dialog.setVisible(true);
    }
}
