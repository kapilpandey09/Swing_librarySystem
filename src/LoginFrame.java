import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class LoginFrame {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    public static void show() {


        JFrame frame = new JFrame("Library System - Login");
        frame.setSize(420, 480);
        frame.setLayout(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

    
        JPanel header = new JPanel(null);
        header.setBounds(0, 0, 420, 160);
        header.setBackground(new Color(25, 60, 120));

        JLabel iconLabel = new JLabel("📚", SwingConstants.CENTER);
        iconLabel.setBounds(0, 20, 420, 50);
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 36));

        JLabel titleLabel = new JLabel("Library Management System", SwingConstants.CENTER);
        titleLabel.setBounds(0, 80, 420, 30);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);

        header.add(iconLabel);
        header.add(titleLabel);


        JPanel formPanel = new JPanel(null);
        formPanel.setBounds(30, 175, 355, 240);
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createLineBorder(new Color(25, 60, 120), 2, true));

    
        JLabel userLabel = new JLabel("Username");
        userLabel.setBounds(40, 25, 100, 25);
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        JTextField userField = new JTextField();
        userField.setBounds(40, 50, 270, 32);

        JLabel passLabel = new JLabel("Password");
        passLabel.setBounds(40, 95, 100, 25);
        passLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        JPasswordField passField = new JPasswordField();
        passField.setBounds(40, 120, 270, 32);

        JLabel messageLabel = new JLabel("");
        messageLabel.setBounds(40, 160, 270, 25);
        messageLabel.setForeground(new Color(200, 0, 0));
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));

        JButton submitButton = new JButton("Login");
        submitButton.setBounds(100, 193, 150, 35);
        submitButton.setBackground(new Color(25, 60, 120));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);
        submitButton.setBorderPainted(false);
        submitButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        submitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        formPanel.add(userLabel);
        formPanel.add(userField);
        formPanel.add(passLabel);
        formPanel.add(passField);
        formPanel.add(messageLabel);
        formPanel.add(submitButton);

   
        JLabel footerLabel = new JLabel("Admin Access Only", SwingConstants.CENTER);
        footerLabel.setBounds(0, 425, 420, 20);
        footerLabel.setForeground(new Color(150, 150, 150));
        footerLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));

   
        frame.add(header);
        frame.add(formPanel);
        frame.add(footerLabel);

        frame.setVisible(true);


        ActionListener doLogin = e -> {
            String user = userField.getText().trim();
            String pass = new String(passField.getPassword());

            if (user.equals(ADMIN_USERNAME) && pass.equals(ADMIN_PASSWORD)) {
                frame.dispose();
                MainWindow.show();
            } else {
                messageLabel.setText(" Incorrect Username or Password!");
                passField.setText("");
                passField.requestFocus();
            }
        };

        submitButton.addActionListener(doLogin);
        passField.addActionListener(doLogin);
        userField.addActionListener(e -> passField.requestFocus());
    }
}