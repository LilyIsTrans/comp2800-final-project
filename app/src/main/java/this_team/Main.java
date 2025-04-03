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
  private static JFrame frame;
  private Grid grid;
  private GameLogic gameLogic;
  private int selectedPieceIndex = 0;
  private JLabel positionLabel;
  private Canvas3D canvas;
  private SimpleUniverse universe;
  private Camera camera;
  private Team[] selectedTeams;
  private BranchGroup scene;
  private TransformGroup sceneTG;

  public static void main(String[] args) throws FileNotFoundException {
    frame = new JFrame("Ludo Game");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().add(new Main());
    frame.setSize(800, 800);
    frame.setVisible(true);
  }

  public Main() throws FileNotFoundException {
    initialize3DScene();
    initializeUI();
    initializeMenu();
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
  }

  private void initializeUI() {
    setLayout(new BorderLayout());
    add(canvas, BorderLayout.CENTER);
    positionLabel = new JLabel("Press 'Start Game' to begin");
    add(positionLabel, BorderLayout.SOUTH);
  }

  private void initializeMenu() {
    Menu menu = new Menu("Menu");
    MenuItem startGameItem = new MenuItem("Start Game");
    startGameItem.addActionListener(this);
    menu.add(startGameItem);

    MenuItem exitItem = new MenuItem("Exit");
    exitItem.addActionListener(this);
    menu.add(exitItem);

    MenuBar menuBar = new MenuBar();
    menuBar.add(menu);
    frame.setMenuBar(menuBar);
  }

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

      gameLogic = new GameLogic(selectedTeams);
      gameLogic.resetGame();
      positionLabel.setText(
        gameLogic.getCurrentTeam().getTeamName() + 
        "'s turn - press SPACE"
      );
        
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
      case "Start Game":
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
        if (gameLogic.isWaitingForRoll()) {
          gameLogic.getCurrentTeam().unhighlightPiece(selectedPieceIndex);
          gameLogic.handleTurn();
          gameLogic.getCurrentTeam().highlightPiece(selectedPieceIndex);
          
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
              (gameLogic.isWaitingForMove() ? " - select piece (1-4)" : "")
            );
          }
        }
        break;

      case KeyEvent.VK_ENTER:
        if (gameLogic.isWaitingForMove()) {
          gameLogic.getCurrentTeam().unhighlightPiece(selectedPieceIndex);
          gameLogic.moveSelectedPiece(selectedPieceIndex);
        } 
        else if (gameLogic.isNoMovesState()) {
          gameLogic.forceTurnEnd();
        }
        
        if (gameLogic.getWinningTeam() != null) {
          positionLabel.setText(
            gameLogic.getWinningTeam().getTeamName() + 
            " WINS THE GAME! Press 'Start Game' to play again"
          );
          return;
        }
        
        gameLogic.getCurrentTeam().unhighlightPiece(selectedPieceIndex);
        positionLabel.setText(
          gameLogic.getCurrentTeam().getTeamName() +
          (gameLogic.isWaitingForRoll() ? "'s turn - press SPACE" : " - select piece (1-4)")
        );
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