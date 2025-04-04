package this_team;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.java3d.utils.universe.ViewingPlatform;
import org.jogamp.vecmath.*;
import org.jogamp.java3d.utils.geometry.Box;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DiceRollDialog extends JDialog {
    // Scene components
    private Canvas3D canvas;
    private SimpleUniverse universe;
    private BranchGroup scene;
    private Dice dice;
    private Timer physicsTimer; // Timer for updating dice physics
    
    public DiceRollDialog(Frame owner) {
        super(owner, "3D Dice Roll", true);
        initComponents();
    }
    
    private void initComponents() {
        // Create and add the Canvas3D
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        canvas = new Canvas3D(config);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(canvas, BorderLayout.CENTER);
        
        // Create a SimpleUniverse with the canvas
        universe = new SimpleUniverse(canvas);
        
        // Create the scene (similar to Dice.java main)
        scene = new BranchGroup();
        
        // Add background
        Background bg = new Background(new Color3f(0.2f, 0.2f, 0.4f));
        bg.setApplicationBounds(new BoundingSphere(new Point3d(), 100));
        scene.addChild(bg);
        
        // Add directional light
        DirectionalLight light = new DirectionalLight(
            new Color3f(1f, 1f, 1f), new Vector3f(-1f, -1f, -1f));
        light.setInfluencingBounds(new BoundingSphere(new Point3d(), 100));
        scene.addChild(light);
        
        // Create the table (using same dimensions as Dice.java)
        Appearance tableApp = new Appearance();
        tableApp.setMaterial(new Material(
            new Color3f(0.6f, 0.3f, 0.0f),
            new Color3f(0, 0, 0),
            new Color3f(0.6f, 0.3f, 0.0f),
            new Color3f(0, 0, 0),
            80.0f
        ));
        Box table = new Box(Dice.TABLE_SIZE, Dice.TABLE_HEIGHT, Dice.TABLE_SIZE, tableApp);
        scene.addChild(table);
        
        // Create the dice instance and add it to the scene.
        dice = new Dice();
        scene.addChild(dice);
        
        // Setup the viewing platform (camera) as in Dice.java main.
        ViewingPlatform viewingPlatform = universe.getViewingPlatform();
        TransformGroup vpGroup = viewingPlatform.getViewPlatformTransform();
        Transform3D viewTransform = new Transform3D();
        viewTransform.setTranslation(new Vector3f(0.0f, 5.0f, 0.0f));
        Point3d eye = new Point3d(0.0, 25.0, 0.0);
        Point3d center = new Point3d(0.0, 0.0, 0.0);
        Vector3d up = new Vector3d(0.0, 0.0, -1.0);
        viewTransform.lookAt(eye, center, up);
        viewTransform.invert();
        vpGroup.setTransform(viewTransform);
        
        // Add the scene to the universe.
        universe.addBranchGraph(scene);
        
        // Create a control panel with only a "Roll Dice" button.
        JPanel controlPanel = new JPanel(new FlowLayout());
        JButton rollBtn = new JButton("Roll Dice");
        rollBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dice.resetRoll();
                int result = dice.roll();
                System.out.println("Roll initiated; final face will be: " + result);
            }
        });
        controlPanel.add(rollBtn);
        getContentPane().add(controlPanel, BorderLayout.SOUTH);
        
        // Create a timer that calls updatePhysics on the dice every 16 ms.
        physicsTimer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dice.updatePhysics(0.016f);
            }
        });
        physicsTimer.start();
        
        // Set the dialog properties.
        setSize(800, 600);
        setLocationRelativeTo(getOwner());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    public static void main(String[] args) {
        // Launch the DiceRollDialog from a test frame.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame testFrame = new JFrame("Dice Roll Tester");
                testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                testFrame.setSize(300, 100);
                JButton launchBtn = new JButton("Open Dice Roller");
                launchBtn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        DiceRollDialog dialog = new DiceRollDialog(testFrame);
                        dialog.setVisible(true);
                    }
                });
                testFrame.getContentPane().setLayout(new FlowLayout());
                testFrame.getContentPane().add(launchBtn);
                testFrame.setLocationRelativeTo(null);
                testFrame.setVisible(true);
            }
        });
    }
}