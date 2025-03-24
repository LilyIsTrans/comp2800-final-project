package this_team;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.java3d.utils.universe.ViewingPlatform;
import org.jogamp.vecmath.*;

import javax.swing.*;
import java.awt.*;

public class LudoGame extends JPanel {
    private static final long serialVersionUID = 1L;
    private static JFrame frame;
    private static int obj_Num = 2;
    private static ObjectManager[] objects = new ObjectManager[obj_Num];

    public static BranchGroup create_Scene() {
        BranchGroup sceneBG = new BranchGroup();
        TransformGroup sceneTG = new TransformGroup();

        // Create the board with a coordinate grid (e.g., 15x15 cells on a 1.5x1.5 board)
        Board board = new Board(1.5, 1.5, 15, 15);

        // Add the board to the scene
        sceneTG.addChild(board.position_Object());

        // Create a game piece and add it to the board
        GamePiece piece = new GamePiece(board, 5.5, 5.5); // Start at (0, 0)
        board.addGamePiece(piece);
        
        // Create the RectangleBox and add it to the scene
        objects[0] = new RectangleBox();
        sceneTG.addChild(objects[0].position_Object());

        sceneBG.addChild(sceneTG);
        sceneBG.addChild(MaterialManager.add_Lights(MaterialManager.White, 1));
        return sceneBG;
    }

    public LudoGame(BranchGroup sceneBG) {
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        Canvas3D canvas = new Canvas3D(config);

        SimpleUniverse su = new SimpleUniverse(canvas);    // create a SimpleUniverse

        // Set up the camera for a top-down view
        ViewingPlatform viewingPlatform = su.getViewingPlatform();
        TransformGroup cameraTG = viewingPlatform.getViewPlatformTransform();
        Transform3D viewTransform = new Transform3D();

        // Define the eye position high above the board.
        // For a top-down view, we'll use (0, 10, 0) assuming the board is centered at (0,0,0)
        Point3d eye = new Point3d(0.0, 5.0, 0.0);
        // Center the view at the board's center
        Point3d center = new Point3d(0.0, 0.0, 0.0);
        // Define the up vector. When looking straight down, you can set this so that the view
        // is rotated appropriately. Here, (0, 0, -1) means the top of the view will point to negative Z.
        Vector3d up = new Vector3d(0.0, 0.0, -1.0);

        // Create the viewing transform using lookAt, then invert it for Java3D.
        viewTransform.lookAt(eye, center, up);
        viewTransform.invert();
        cameraTG.setTransform(viewTransform);

        sceneBG.compile();                               // optimize the BranchGroup
        su.addBranchGraph(sceneBG);                      // attach the scene to SimpleUniverse

        setLayout(new BorderLayout());
        add("Center", canvas);
        frame.setSize(800, 800);                         // set the size of the JFrame
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        frame = new JFrame("Ludo Game");
        frame.getContentPane().add(new LudoGame(create_Scene()));  // create an instance of the class
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}