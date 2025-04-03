package this_team;

import javax.swing.*;
import java.awt.*;

public class SplashScreen extends JWindow {
  public SplashScreen() {
    // Create a custom panel for the splash screen.
    JPanel splashPanel = new JPanel() {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Enable anti-aliasing for smooth text.
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Set background to white.
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // Define colors: drop shadow and Facebook-like blue.
        Color shadow = new Color(0, 0, 0, 80); // semi-transparent black
        Color fbBlue = new Color(24, 119, 242);  // Facebook-like blue
        
        // Define fonts. If you have a custom font, load it with Font.createFont() if needed.
        Font titleFont = new Font("SansSerif", Font.BOLD, 48);
        Font subtitleFont = new Font("SansSerif", Font.PLAIN, 36);
        
        // Text to display.
        String line1 = "COMP2800";
        String line2 = "team12";
        
        // Prepare FontMetrics for positioning.
        FontMetrics fmTitle = g2d.getFontMetrics(titleFont);
        FontMetrics fmSubtitle = g2d.getFontMetrics(subtitleFont);
        
        // Calculate positions for the title text.
        int titleWidth = fmTitle.stringWidth(line1);
        int titleHeight = fmTitle.getHeight();
        int xTitle = (getWidth() - titleWidth) / 2;
        int yTitle = (getHeight() - titleHeight - fmSubtitle.getHeight() - 10) / 2 + fmTitle.getAscent();
        
        // Draw drop shadow for title.
        g2d.setFont(titleFont);
        g2d.setColor(shadow);
        g2d.drawString(line1, xTitle + 3, yTitle + 3);
        // Draw title text.
        g2d.setColor(fbBlue);
        g2d.drawString(line1, xTitle, yTitle);
        
        // Calculate positions for the subtitle text.
        int subtitleWidth = fmSubtitle.stringWidth(line2);
        int xSubtitle = (getWidth() - subtitleWidth) / 2;
        int ySubtitle = yTitle + titleHeight + 10;
        
        // Draw drop shadow for subtitle.
        g2d.setFont(subtitleFont);
        g2d.setColor(shadow);
        g2d.drawString(line2, xSubtitle + 2, ySubtitle + 2);
        // Draw subtitle text.
        g2d.setColor(fbBlue);
        g2d.drawString(line2, xSubtitle, ySubtitle);
      }
    };
    
    splashPanel.setPreferredSize(new Dimension(400, 300));
    getContentPane().add(splashPanel);
    pack();
    setLocationRelativeTo(null);
  }

  public void showSplash() {
    setVisible(true);
    // Timer to close the splash screen after 3 seconds.
    Timer timer = new Timer(3000, e -> {
      dispose();
      SwingUtilities.invokeLater(this::launchMainApplication);
    });
    timer.setRepeats(false);
    timer.start();
  }
  
  private void launchMainApplication() {
    JFrame frame = new JFrame("Ludo Game");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    try {
      // Pass the frame to Main so that initializeMenu() can properly attach the menu bar.
      frame.getContentPane().add(new Main(frame));
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    frame.setSize(800, 800);
    frame.setVisible(true);
  }
}