import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class UIHelper {

    public static JButton makeBtn(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(130, 36));
        return btn;
    }

    public static JTable styledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(173, 216, 230));
        table.setGridColor(new Color(210, 225, 240));


        
        JTableHeader h = table.getTableHeader();
        h.setFont(new Font("SansSerif", Font.BOLD, 13));
        h.setBackground(new Color(70, 130, 180));
        h.setForeground(Color.WHITE);
        h.setPreferredSize(new Dimension(0, 32));

        return table;
    }


    public static JLabel boldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        return l;
    }

 
    public static JLabel dialogHeader(String text, Color bg) {
        JLabel l = new JLabel("  " + text, SwingConstants.LEFT);
        l.setFont(new Font("SansSerif", Font.BOLD, 16));
        l.setForeground(Color.WHITE);
        l.setOpaque(true);
        l.setBackground(bg);
        l.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 0));
        return l;
    }

 
    public static void msg(String text) {
        JOptionPane.showMessageDialog(null, text);
    }


    public static boolean confirm(String text) {
        return JOptionPane.showConfirmDialog(null, text, "Confirm",
            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
