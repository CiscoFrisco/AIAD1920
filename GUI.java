import javax.swing.JFrame;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Arrays;

public class GUI {

    // public Map map;

    public static void main(String[] args) {
        JFrame frame = new JFrame("HideNSeek");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        frame.add(panel);

        JLabel selMapLabel = new JLabel("\"Basic\" Map Selected!", JLabel.CENTER);
        panel.add(selMapLabel);


        // Map menu
        String[] map = { "Basic", "Walls", "Open" };
        JComboBox mapMenu = new JComboBox(map);
        panel.add(mapMenu);
        changeMap(mapMenu, selMapLabel);

        frame.setVisible(true);
    }

    public static void changeMap(JComboBox mapMenu, JLabel selMapLabel) {

        mapMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedMap = mapMenu.getSelectedIndex();

                switch(selectedMap){
                    case 0:
                        selMapLabel.setText("\"Basic\" Map Selected!");
                        break;
                    case 1:
                        selMapLabel.setText("\"Walls\" Map Selected!");
                        break;
                    case 2:
                        selMapLabel.setText("\"Open\" Map Selected!");
                        break;
                    default:
                        break;
                }
            }
        });
    }
}