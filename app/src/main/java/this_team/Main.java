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

  public static void main(String[] args) throws FileNotFoundException {
    frame = new JFrame("Ludo Game");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().add(new Main());
    frame.setSize(800, 800);
    frame.setVisible(true);
  }

  public Main() throws FileNotFoundException {
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

    BranchGroup scene = createScene();
    universe = new SimpleUniverse(canvas);
    camera.setSchedulingBounds(new BoundingSphere(new Point3d(0, 0, 0), Double.POSITIVE_INFINITY));
    setupTopDownCamera();
    scene.compile();

    universe.addBranchGraph(scene);

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

    setLayout(new BorderLayout());
    add(canvas, BorderLayout.CENTER);

    positionLabel = new JLabel("Begin game - press SPACE to roll");
    add(positionLabel, BorderLayout.SOUTH);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    String chosen_item = e.getActionCommand();	 
    switch (chosen_item) {
      case "Exit":
        System.exit(0);
        break;
      case "Start/Reset Game":
        gameLogic.resetGame();
        positionLabel.setText(
          gameLogic.getCurrentTeam().getTeamName() +
          "'s turn - press SPACE"
        );
        break;
    }
  }

  private BranchGroup createScene() throws FileNotFoundException {
    BranchGroup scene = new BranchGroup();
    TransformGroup sceneTG = new TransformGroup();
    sceneTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
    sceneTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
    sceneTG.setCapability(Node.ENABLE_PICK_REPORTING);

    Transform3D boardTransform = new Transform3D();
    sceneTG.addChild(LudoBoard.create_board(boardTransform));

    grid = new Grid();
    sceneTG.addChild(grid.position_Object());

    Team[] teams = {
      new RedTeam(grid.getSize(), grid.getCellSize()),
      new YellowTeam(grid.getSize(), grid.getCellSize())
    };

    gameLogic = new GameLogic(teams);

    for (Team team : teams) {
      sceneTG.addChild(team.getTransformGroup());
    }

    Light ambient = new DirectionalLight(new Color3f(1f, 1f, 1f), new Vector3f(0.3f, 0.1f, -1));
    ambient.setInfluencingBounds(new BoundingSphere(new Point3d(0, 0, 0), 100));
    sceneTG.addChild(ambient);

    scene.addChild(sceneTG);
    return scene;
  }

  @Override
  public void keyPressed(KeyEvent e) {
    // Handle piece selection (1-4 keys)
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
          gameLogic.rollDice();
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
        }
        else if (gameLogic.isNoMovesState()) {
          gameLogic.forceTurnEnd();
          
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
            "'s turn - press SPACE"
          );
        }
        break;

      case KeyEvent.VK_G:
        grid.toggleVisibility();
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
}