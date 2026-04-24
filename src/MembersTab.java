

import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.table.*;

public class MembersTab {

    public static JPanel build() {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Table Setup ---
        String[] cols = {"ID", "Name", "Phone", "Email", "Address", "Joined"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = UIHelper.styledTable(model);

        // Column widths
        int[] widths = {40, 160, 120, 200, 200, 100};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Status bar
        JLabel statusBar = new JLabel("  Total Members: 0");
        statusBar.setFont(new Font("SansSerif", Font.ITALIC, 12));

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // --- Buttons ---
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        btns.setBackground(new Color(230, 240, 255));
        btns.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(180, 200, 220)));

        JButton btnAdd     = UIHelper.makeBtn("+ Add Member",  new Color(34, 120, 60));
        JButton btnEdit    = UIHelper.makeBtn("✏ Edit",         new Color(180, 120, 0));
        JButton btnDelete  = UIHelper.makeBtn("- Delete",       new Color(178, 34, 34));
        JButton btnHistory = UIHelper.makeBtn("📋 His History", new Color(50, 100, 180));
        JButton btnRefresh = UIHelper.makeBtn("↺ Refresh",      new Color(80, 80, 80));

        btns.add(btnAdd);
        btns.add(btnEdit);
        btns.add(btnDelete);
        btns.add(btnHistory);
        btns.add(btnRefresh);

        JPanel south = new JPanel(new BorderLayout());
        south.add(btns, BorderLayout.CENTER);
        south.add(statusBar, BorderLayout.SOUTH);
        panel.add(south, BorderLayout.SOUTH);

  
        loadMembers(model, statusBar);

        // --- Button Actions ---

        btnAdd.addActionListener(e -> addMemberDialog(null, model, statusBar));

        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { UIHelper.msg("Please select a member to edit."); return; }
            editMemberDialog(null, model, statusBar,
                (int)    model.getValueAt(row, 0),   // id
                (String) model.getValueAt(row, 1),   // name
                (String) model.getValueAt(row, 2),   // phone
                (String) model.getValueAt(row, 3),   // email
                (String) model.getValueAt(row, 4));  // address
        });

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { UIHelper.msg("Please select a member."); return; }
            int id = (int) model.getValueAt(row, 0);
            if (UIHelper.confirm("Delete member: \"" + model.getValueAt(row, 1) + "\"?")) {
                DBHelper.runSQL("DELETE FROM members WHERE id=" + id);
                loadMembers(model, statusBar);
            }
        });

    
        btnHistory.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { UIHelper.msg("Please select a member to view history."); return; }
            TransactionsTab.showMemberHistory(
                (int)    model.getValueAt(row, 0), 
                (String) model.getValueAt(row, 1)); 
        });

        btnRefresh.addActionListener(e -> loadMembers(model, statusBar));

        return panel;
    }

    public static void loadMembers(DefaultTableModel model, JLabel statusBar) {
        model.setRowCount(0);
        try {
            ResultSet rs = DBHelper.conn.createStatement()
                .executeQuery("SELECT * FROM members ORDER BY id");
            int count = 0;
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("phone"),
                    rs.getString("email"),
                    rs.getString("address"),
                    rs.getString("joined")
                });
                count++;
            }
            statusBar.setText("  Total Members: " + count);
        } catch (SQLException e) {
            UIHelper.msg("Load Error: " + e.getMessage());
        }
    }


    static void addMemberDialog(JFrame parent, DefaultTableModel model, JLabel statusBar) {
        JDialog d = new JDialog(parent, "Add New Member", true);
        d.setSize(420, 340);
        d.setLocationRelativeTo(parent);
        d.setLayout(new BorderLayout());
        d.add(UIHelper.dialogHeader("Add New Member", new Color(34, 120, 60)), BorderLayout.NORTH);

        JTextField tfName    = new JTextField();
        JTextField tfPhone   = new JTextField();
        JTextField tfEmail   = new JTextField();
        JTextField tfAddress = new JTextField();

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 12));
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 10, 25));
        form.add(UIHelper.boldLabel("Name *:"));  form.add(tfName);
        form.add(UIHelper.boldLabel("Phone:"));   form.add(tfPhone);
        form.add(UIHelper.boldLabel("Email:"));   form.add(tfEmail);
        form.add(UIHelper.boldLabel("Address:")); form.add(tfAddress);
        d.add(form, BorderLayout.CENTER);

        JButton saveBtn = UIHelper.makeBtn("Save", new Color(34, 120, 60));
        JPanel bp = new JPanel(); bp.add(saveBtn);
        d.add(bp, BorderLayout.SOUTH);

        saveBtn.addActionListener(e -> {
            if (tfName.getText().trim().isEmpty()) {
                UIHelper.msg("Name is required!"); return;
            }
            try {
                PreparedStatement ps = DBHelper.conn.prepareStatement(
                    "INSERT INTO members(name, phone, email, address, joined) VALUES(?, ?, ?, ?, ?)");
                ps.setString(1, tfName.getText().trim());
                ps.setString(2, tfPhone.getText().trim());
                ps.setString(3, tfEmail.getText().trim());
                ps.setString(4, tfAddress.getText().trim());
                ps.setString(5, LocalDate.now().toString()); 
                ps.executeUpdate();
                UIHelper.msg("Member added successfully!");
                loadMembers(model, statusBar);
                d.dispose();
            } catch (SQLException ex) { UIHelper.msg("Error: " + ex.getMessage()); }
        });

        d.setVisible(true);
    }

    static void editMemberDialog(JFrame parent, DefaultTableModel model, JLabel statusBar,
            int id, String name, String phone, String email, String address) {
        JDialog d = new JDialog(parent, "Edit Member", true);
        d.setSize(420, 340);
        d.setLocationRelativeTo(parent);
        d.setLayout(new BorderLayout());
        d.add(UIHelper.dialogHeader("Edit Member", new Color(180, 120, 0)), BorderLayout.NORTH);

        
        JTextField tfName    = new JTextField(name);
        JTextField tfPhone   = new JTextField(phone   == null ? "" : phone);
        JTextField tfEmail   = new JTextField(email   == null ? "" : email);
        JTextField tfAddress = new JTextField(address == null ? "" : address);

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 12));
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 10, 25));
        form.add(UIHelper.boldLabel("Name *:"));  form.add(tfName);
        form.add(UIHelper.boldLabel("Phone:"));   form.add(tfPhone);
        form.add(UIHelper.boldLabel("Email:"));   form.add(tfEmail);
        form.add(UIHelper.boldLabel("Address:")); form.add(tfAddress);
        d.add(form, BorderLayout.CENTER);

        JButton updateBtn = UIHelper.makeBtn("Update", new Color(180, 120, 0));
        JPanel bp = new JPanel(); bp.add(updateBtn);
        d.add(bp, BorderLayout.SOUTH);

        updateBtn.addActionListener(e -> {
            if (tfName.getText().trim().isEmpty()) {
                UIHelper.msg("Name is required!"); return;
            }
            try {
                PreparedStatement ps = DBHelper.conn.prepareStatement(
                    "UPDATE members SET name=?, phone=?, email=?, address=? WHERE id=?");
                ps.setString(1, tfName.getText().trim());
                ps.setString(2, tfPhone.getText().trim());
                ps.setString(3, tfEmail.getText().trim());
                ps.setString(4, tfAddress.getText().trim());
                ps.setInt(5, id);
                ps.executeUpdate();
                UIHelper.msg("Member updated!");
                loadMembers(model, statusBar);
                d.dispose();
            } catch (SQLException ex) { UIHelper.msg("Error: " + ex.getMessage()); }
        });

        d.setVisible(true);
    }
}
