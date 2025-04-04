package this_team;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.Random;

public class HomeScreen extends JPanel {
  private JButton newGameButton;
  private JButton instructionsButton;
  private NebulaStarBackgroundPanel backgroundPanel;

  public HomeScreen() {
    // Use BorderLayout so the background fills the screen.
    setLayout(new BorderLayout());
    
    // Create our new dynamic background panel.
    backgroundPanel = new NebulaStarBackgroundPanel();
    backgroundPanel.setLayout(new GridBagLayout());
    add(backgroundPanel, BorderLayout.CENTER);
    
    // Create an overlay panel for the buttons.
    JPanel overlayPanel = new JPanel(new GridBagLayout());
    overlayPanel.setBackground(new Color(255, 255, 255, 200)); // White with some transparency.
    
    // Create buttons with black text.
    newGameButton = new JButton("New Game");
    newGameButton.setFont(new Font("Arial", Font.BOLD, 20));
    newGameButton.setForeground(Color.BLACK);
    
    instructionsButton = new JButton("Instructions");
    instructionsButton.setFont(new Font("Arial", Font.BOLD, 20));
    instructionsButton.setForeground(Color.BLACK);
    
    // Position the buttons in a vertical column.
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(10, 10, 10, 10);
    gbc.gridx = 0;
    gbc.gridy = 0;
    overlayPanel.add(newGameButton, gbc);
    gbc.gridy = 1;
    overlayPanel.add(instructionsButton, gbc);
    
    // Add the overlay panel in the center.
    backgroundPanel.add(overlayPanel, new GridBagConstraints());
    
    // Set up the instructions button action.
    instructionsButton.addActionListener(e -> {
      JOptionPane.showMessageDialog(
        HomeScreen.this,
        "Instructions:\n\n1. Press SPACE to roll the dice.\n2. Press keys 1-4 to select a piece.\n3. Press ENTER to move.\n\nEnjoy the game!",
        "Instructions",
        JOptionPane.INFORMATION_MESSAGE
      );
    });
  }

  // Allows an external class to attach a listener for the "New Game" button.
  public void addNewGameListener(ActionListener listener) {
    newGameButton.addActionListener(listener);
  }

  // New background panel combining stars and nebula effects.
  private class NebulaStarBackgroundPanel extends JPanel {
    // Star settings.
    private static final int NUM_STARS = 150;
    private Star[] stars;
    
    // Nebula blob settings.
    private static final int NUM_NEBULAS = 10;
    private Nebula[] nebulas;
    
    private Timer timer;
    private Random rand;

    public NebulaStarBackgroundPanel() {
      rand = new Random();
      stars = new Star[NUM_STARS];
      for (int i = 0; i < NUM_STARS; i++) {
        stars[i] = new Star(
          rand.nextInt(1920), // x position (will scale)
          rand.nextInt(1080), // y position
          1 + rand.nextInt(3), // star size
          rand.nextFloat()     // brightness between 0 and 1
        );
      }
      
      nebulas = new Nebula[NUM_NEBULAS];
      for (int i = 0; i < NUM_NEBULAS; i++) {
        int x = rand.nextInt(1920);
        int y = rand.nextInt(1080);
        int radius = 100 + rand.nextInt(200);
        Color[] colors = new Color[] {
          new Color(255, 105, 180, 150), // hot pink
          new Color(138, 43, 226, 150),  // blue violet
          new Color(65, 105, 225, 150)   // royal blue
        };
        nebulas[i] = new Nebula(x, y, radius, colors);
      }
      
      // Timer to update star brightness and nebula drift/color interpolation.
      timer = new Timer(100, e -> {
        // Update stars brightness.
        for (Star star : stars) {
          star.brightness += (rand.nextFloat() - 0.5f) * 0.1f;
          star.brightness = Math.max(0.2f, Math.min(1.0f, star.brightness));
        }
        // Update nebula drift and gradually update colors.
        for (Nebula nebula : nebulas) {
          nebula.x += rand.nextInt(3) - 1;
          nebula.y += rand.nextInt(3) - 1;
          nebula.progress += 0.02;  // Increment progress; 0.01 * 100 ticks = 10 sec
          if (nebula.progress >= 1.0) {
            nebula.startColor = nebula.targetColor;
            nebula.targetColor = nebula.colors[rand.nextInt(nebula.colors.length)];
            nebula.progress = 0;
          }
        }
        repaint();
      });
      timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2d = (Graphics2D) g;
      int w = getWidth();
      int h = getHeight();
      
      // Fill background with a dark base.
      g2d.setColor(Color.BLACK);
      g2d.fillRect(0, 0, w, h);
      
      // Draw stars.
      for (Star star : stars) {
        int x = (int) ((double) star.x / 1920 * w);
        int y = (int) ((double) star.y / 1080 * h);
        g2d.setColor(new Color(1f, 1f, 1f, star.brightness));
        g2d.fillOval(x, y, star.size, star.size);
      }
      
      // Draw nebula blobs using radial gradients.
      for (Nebula nebula : nebulas) {
        int centerX = (int) ((double) nebula.x / 1920 * w);
        int centerY = (int) ((double) nebula.y / 1080 * h);
        int radius = (int) ((double) nebula.radius / 1920 * w);
        
        float[] dist = { 0.0f, 1.0f };
        // Calculate the blended color gradually over 10 seconds.
        Color blendedColor = blendColors(nebula.startColor, nebula.targetColor, nebula.progress);
        Color[] colors = { blendedColor, new Color(blendedColor.getRed(), blendedColor.getGreen(), blendedColor.getBlue(), 0) };
        RadialGradientPaint rgp = new RadialGradientPaint(new Point2D.Float(centerX, centerY), radius, dist, colors);
        g2d.setPaint(rgp);
        g2d.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
      }
    }
    
    // Helper method to linearly interpolate between two colors.
    private Color blendColors(Color c1, Color c2, double t) {
      int r = (int) (c1.getRed() + t * (c2.getRed() - c1.getRed()));
      int g = (int) (c1.getGreen() + t * (c2.getGreen() - c1.getGreen()));
      int b = (int) (c1.getBlue() + t * (c2.getBlue() - c1.getBlue()));
      int a = (int) (c1.getAlpha() + t * (c2.getAlpha() - c1.getAlpha()));
      return new Color(r, g, b, a);
    }
    
    // Star class for twinkling stars.
    private class Star {
      int x, y, size;
      float brightness;
      
      public Star(int x, int y, int size, float brightness) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.brightness = brightness;
      }
    }
    
    // Nebula class representing a colorful blob that gradually changes color.
    private class Nebula {
      int x, y, radius;
      Color[] colors;
      Color startColor;
      Color targetColor;
      double progress;  // Value between 0 and 1
      
      public Nebula(int x, int y, int radius, Color[] colors) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.colors = colors;
        this.startColor = colors[rand.nextInt(colors.length)];
        this.targetColor = colors[rand.nextInt(colors.length)];
        this.progress = 0;
      }
    }
  }
}