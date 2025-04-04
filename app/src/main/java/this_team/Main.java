package this_team;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileNotFoundException;

public class Main extends JPanel implements KeyListener, ActionListener {
  private static final long serialVersionUID = 1L;
  public static JFrame frame;
  private Grid grid;
  private GameLogic gameLogic;
  private int selectedPieceIndex = 0;
  private JLabel positionLabel;
  private Canvas3D canvas;
  private SimpleUniverse universe;
  private Camera camera;
  private Team[] selectedTeams;
  private BranchGroup scene;
  private boolean hasRolled = false;
  private TransformGroup sceneTG;
  private DiceRollWrapper diceRollWrapper;

  // CardLayout for view switching
  private CardLayout cardLayout;
  private HomeScreen homeScreen;  // Using the external HomeScreen class
  private JPanel gamePanel;

  public static void main(String[] args) throws FileNotFoundException {
    SwingUtilities.invokeLater(() -> {
      new SplashScreen().showSplash();
    });
  }

  // Constructor - now creates a CardLayout with HomeScreen and Game view.
  public Main(JFrame frame) throws FileNotFoundException {
    Main.frame = frame;
    cardLayout = new CardLayout();
    setLayout(cardLayout);

    // Create and add the home screen.
    homeScreen = new HomeScreen();
    // Attach listener for the "New Game" button on the home screen.
    homeScreen.addNewGameListener(e -> {
      // Open team selection dialog which will eventually call startGameWithTeams(...)
      TeamSelectionDialog dialog = new TeamSelectionDialog(frame, this);
      dialog.setVisible(true);
    });
    add(homeScreen, "home");

    // Create the game panel.
    createGamePanel();

    // Initially show the home screen.
    cardLayout.show(this, "home");

    // Initialize common components for the game view.
    initialize3DScene();
    initializeMenu();
  }

private void createGamePanel() {
  gamePanel = new JPanel(new BorderLayout());
  positionLabel = new JLabel("Press 'New Game' to begin");
  positionLabel.setHorizontalAlignment(SwingConstants.CENTER);
  positionLabel.setFont(new Font("Arial", Font.BOLD, 18));
  // Set text color to white and background to black.
  positionLabel.setForeground(Color.WHITE);
  positionLabel.setOpaque(true);
  positionLabel.setBackground(Color.BLACK);
  gamePanel.add(positionLabel, BorderLayout.SOUTH);
  add(gamePanel, "game");
}

  private void initialize3DScene() throws FileNotFoundException {
    GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
    canvas = new Canvas3D(config);
    canvas.setFocusable(true);
    canvas.requestFocusInWindow();
    canvas.addKeyListener(this);

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    camera = new Camera(screenSize.width);

    canvas.addKeyListener(camera);
    canvas.addMouseListener(camera);
    canvas.addMouseMotionListener(camera);

    universe = new SimpleUniverse(canvas);
    camera.setSchedulingBounds(new BoundingSphere(new Point3d(0, 0, 0), Double.POSITIVE_INFINITY));
    setupTopDownCamera();

    // Add the canvas to the center of the game panel.
    gamePanel.add(canvas, BorderLayout.CENTER);
  }

  private void initializeMenu() {
    Menu menu = new Menu("Menu");
    MenuItem startGameItem = new MenuItem("New Game");
    startGameItem.addActionListener(this);
    menu.add(startGameItem);

    MenuItem exitItem = new MenuItem("Exit");
    exitItem.addActionListener(this);
    menu.add(exitItem);

    MenuBar menuBar = new MenuBar();
    menuBar.add(menu);
    frame.setMenuBar(menuBar);
  }

  // Called by TeamSelectionDialog to start the game.
  public void startGameWithTeams(int teamCount, String[] selectedColors) {
    try {
      if (scene != null) {
        scene.detach();
      }

      BranchGroup newScene = new BranchGroup();
      newScene.setCapability(BranchGroup.ALLOW_DETACH);
      TransformGroup newSceneTG = new TransformGroup();
      newSceneTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
      newSceneTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
      newSceneTG.setCapability(Node.ENABLE_PICK_REPORTING);

      Transform3D boardTransform = new Transform3D();
      newSceneTG.addChild(LudoBoard.create_board(boardTransform));
      grid = new Grid();
      newSceneTG.addChild(grid.position_Object());

      selectedTeams = new Team[teamCount];
      for (int i = 0; i < teamCount; i++) {
        switch (selectedColors[i]) {
          case "Red":
            selectedTeams[i] = new RedTeam(grid.getSize(), grid.getCellSize());
            break;
          case "Yellow":
            selectedTeams[i] = new YellowTeam(grid.getSize(), grid.getCellSize());
            break;
          case "Blue":
            selectedTeams[i] = new BlueTeam(grid.getSize(), grid.getCellSize());
            break;
          case "Green":
            selectedTeams[i] = new GreenTeam(grid.getSize(), grid.getCellSize());
            break;
          default:
            selectedTeams[i] = new RedTeam(grid.getSize(), grid.getCellSize());
        }
        newSceneTG.addChild(selectedTeams[i].getTransformGroup());
      }

      Light ambient = new DirectionalLight(new Color3f(1f, 1f, 1f), new Vector3f(0.3f, 0.1f, -1));
      ambient.setInfluencingBounds(new BoundingSphere(new Point3d(0, 0, 0), 100));
      newSceneTG.addChild(ambient);

      newScene.addChild(newSceneTG);
      newScene.compile();

      scene = newScene;
      sceneTG = newSceneTG;
      universe.addBranchGraph(scene);

      DiceRollWrapper diceRollWrapper = new DiceRollWrapper();
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      int diceWindowWidth = 500;
      int diceWindowHeight = 400;

      int xPosition = screenSize.width - diceWindowWidth - 20;
      int yPosition = 20;

      diceRollWrapper.setSize(diceWindowWidth, diceWindowHeight);
      diceRollWrapper.setLocation(xPosition, yPosition);
      diceRollWrapper.setVisible(true);
      diceRollWrapper.setFocusable(true);

      gameLogic = new GameLogic(diceRollWrapper, selectedTeams);
      gameLogic.resetGame();
      positionLabel.setText(
        gameLogic.getCurrentTeam().getTeamName() + "'s turn - press SPACE"
      );

      // Switch to game view.
      cardLayout.show(this, "game");

    } catch (FileNotFoundException e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(frame,
          "Error loading team resources",
          "Error",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    String chosen_item = e.getActionCommand();
    switch (chosen_item) {
      case "Exit":
        System.exit(0);
        break;
      case "New Game":
        // Open team selection dialog when New Game is selected from the menu.
        TeamSelectionDialog dialog = new TeamSelectionDialog(frame, this);
        dialog.setVisible(true);
        break;
    }
  }

  @Override
  public void keyPressed(KeyEvent e) {
    if (gameLogic == null) return;

    if (e.getKeyCode() >= KeyEvent.VK_1 && e.getKeyCode() <= KeyEvent.VK_4) {
      gameLogic.getCurrentTeam().unhighlightPiece(selectedPieceIndex);
      selectedPieceIndex = e.getKeyCode() - KeyEvent.VK_1;
      gameLogic.getCurrentTeam().highlightPiece(selectedPieceIndex);
      positionLabel.setText(
        gameLogic.getCurrentTeam().getTeamName() +
        " piece " + (selectedPieceIndex + 1) + " selected"
      );
      return;
    }

    switch (e.getKeyCode()) {
        case KeyEvent.VK_SPACE:
        // Only allow a roll if the game is waiting for a roll and we haven't rolled yet this turn.
        if (gameLogic.isWaitingForRoll() && !hasRolled) {
          hasRolled = true; // Mark that we've rolled this turn.
          positionLabel.setText(gameLogic.getCurrentTeam().getTeamName() + " rolling...");
          gameLogic.handleTurn((Void) -> SwingUtilities.invokeLater(() -> {
            if (gameLogic.isNoMovesState()) {
              positionLabel.setText(
                gameLogic.getCurrentTeam().getTeamName() +
                " rolled " + gameLogic.getCurrentDiceValue() +
                " - NO POSSIBLE MOVES (Press ENTER)"
              );
            } else {
              positionLabel.setText(
                gameLogic.getCurrentTeam().getTeamName() +
                " rolled " + gameLogic.getCurrentDiceValue() +
                " - select piece (1-4)"
              );
            }
          }));
        }
        break;

        case KeyEvent.VK_ENTER:
  
        if (gameLogic.isWaitingForMove()) {
          gameLogic.getCurrentTeam().unhighlightPiece(selectedPieceIndex);
          gameLogic.moveSelectedPiece(selectedPieceIndex, success -> {
            SwingUtilities.invokeLater(() -> {
              if (success) {
                positionLabel.setText(gameLogic.getCurrentTeam().getTeamName() + " moved! Press ENTER to continue.");
              } 
              else {
                positionLabel.setText(gameLogic.getCurrentTeam().getTeamName() + " couldn't move! Press ENTER to continue.");
              }
              // After the move, check for win.
              if (gameLogic.getWinningTeam() != null) {
                positionLabel.setText(gameLogic.getWinningTeam().getTeamName() + " WINS! Press 'New Game' to play again.");
              } 
              else {
                gameLogic.handleTurn((Void) -> {
                  if (gameLogic.getWinningTeam() != null) {
                    positionLabel.setText(gameLogic.getWinningTeam().getTeamName() + " WINS! Press 'New Game' to play again.");
                  } else if (gameLogic.isWaitingForRoll()) {
                    positionLabel.setText(gameLogic.getCurrentTeam().getTeamName() + "'s turn - press SPACE");
                  }
                });
              }
            });
          });
        } else if (gameLogic.isNoMovesState() || gameLogic.isTurnComplete()) {
            if (gameLogic.getWinningTeam() != null) {
                positionLabel.setText(gameLogic.getWinningTeam().getTeamName() + " WINS! Press 'New Game' to play again.");
              } 
            gameLogic.forceTurnEnd();
            positionLabel.setText(gameLogic.getCurrentTeam().getTeamName() + "'s turn complete - Press SPACE to roll");
        }
        hasRolled = false; 
        break;
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {}

  @Override
  public void keyTyped(KeyEvent e) {}

  private void setupTopDownCamera() {
    universe.getViewingPlatform().setViewPlatformBehavior(camera);
    camera.setEnable(true);
  }

  public JFrame getFrame() {
    return frame;
  }
}