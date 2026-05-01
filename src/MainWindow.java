import java.awt.*;
import javax.swing.*;

public class MainWindow {

    public static void show() {
        JFrame frame = new JFrame("Library Management System");
        frame.setSize(1000, 640);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        
        JLabel titleLabel = new JLabel("  Library Management System", SwingConstants.LEFT);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setOpaque(true); 
        titleLabel.setBackground(new Color(25, 60, 120));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 0));
        frame.add(titleLabel, BorderLayout.NORTH);

      
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 14));

        tabs.addTab("📚  Books",        BooksTab.build());        
        tabs.addTab("👤  Members",      MembersTab.build());     
        tabs.addTab("📋  Transactions", TransactionsTab.build()); 

        frame.add(tabs, BorderLayout.CENTER);
        frame.setVisible(true);
    }
}
