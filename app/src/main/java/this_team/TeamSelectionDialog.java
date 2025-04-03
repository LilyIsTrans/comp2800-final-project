package this_team;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class TeamSelectionDialog extends JDialog implements ActionListener {
  private JComboBox<String> teamComboBox;
  private JButton nextButton;
  private Main mainApp;
  private int teamCount;
  
  public TeamSelectionDialog(JFrame parent, Main mainApp) {
    super(parent, "Team Selection", true);
    this.mainApp = mainApp;
    setLayout(new BorderLayout());
    
    JPanel panel = new JPanel(new FlowLayout());
    panel.add(new JLabel("Select number of teams:"));
    teamComboBox = new JComboBox<>(new String[] { "2 Teams", "3 Teams", "4 Teams" });
    panel.add(teamComboBox);
    add(panel, BorderLayout.CENTER);
    
    nextButton = new JButton("Next");
    nextButton.addActionListener(this);
    add(nextButton, BorderLayout.SOUTH);
    
    pack();
    setLocationRelativeTo(parent);
  }
  
  @Override
  public void actionPerformed(ActionEvent e) {
    String selection = (String) teamComboBox.getSelectedItem();
    if ("3 Teams".equals(selection)) {
      teamCount = 3;
    } else if ("4 Teams".equals(selection)) {
      teamCount = 4;
    } else {
      teamCount = 2;
    }
    dispose();
    // Now prompt for each player's color
    String[] selectedColors = new String[teamCount];
    List<String> usedColors = new ArrayList<>();


    for (int i = 0; i < teamCount; i++) {
      ColorSelectionDialog csd = new ColorSelectionDialog(mainApp.getFrame(), 
          "Player " + (i + 1) + ", select your color:", usedColors);
      csd.setVisible(true);
      String color = csd.getSelectedColor();
      selectedColors[i] = color;
      usedColors.add(color);
    }
    // Once you have the colors, pass them to your main app
    mainApp.startGameWithTeams(teamCount, selectedColors);
  }
}