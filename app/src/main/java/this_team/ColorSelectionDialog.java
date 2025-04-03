package this_team;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class ColorSelectionDialog extends JDialog implements ActionListener {
  private JComboBox<String> colorComboBox;
  private JButton selectButton;
  private String selectedColor = null;

  // Constructor accepts a list of colors already chosen so they can be filtered out.
  public ColorSelectionDialog(JFrame parent, String title, List<String> usedColors) {
    super(parent, title, true);
    setLayout(new BorderLayout());

    JPanel panel = new JPanel(new FlowLayout());
    panel.add(new JLabel("Select color:"));

    String[] allColors = { "Red", "Yellow", "Blue", "Green" };
    List<String> availableColors = new ArrayList<>();
    for (String color : allColors) {
      if (!usedColors.contains(color)) {
        availableColors.add(color);
      }
    }

    colorComboBox = new JComboBox<>(availableColors.toArray(new String[0]));
    panel.add(colorComboBox);
    add(panel, BorderLayout.CENTER);

    selectButton = new JButton("Select");
    selectButton.addActionListener(this);
    add(selectButton, BorderLayout.SOUTH);

    pack();
    setLocationRelativeTo(parent);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    selectedColor = (String) colorComboBox.getSelectedItem();
    dispose();
  }

  public String getSelectedColor() {
    return selectedColor;
  }
}