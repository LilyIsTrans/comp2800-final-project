package this_team;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.java3d.utils.universe.ViewingPlatform;
import org.jogamp.vecmath.*;
import org.jogamp.java3d.utils.geometry.Box;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.util.function.Consumer;

public class DiceRollWrapper extends JDialog {
    // Scene components
    private Canvas3D canvas;
    private SimpleUniverse universe;
    private BranchGroup scene;
    private Dice dice;
    private Timer physicsTimer; // Timer for updating dice physics

    public DiceRollWrapper() {
        // Change modal flag to false so this dialog is non-modal.
        super((Frame)null, "3D Dice Roll", false);
        initComponents();
    }

    public void rollDiceAsync(Consumer<Integer> callback) {
    new SwingWorker<Integer, Void>() {
        @Override
        protected Integer doInBackground() {
            dice.resetRoll();
            dice.roll();

            while (dice.isDiceRolling()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return dice.getSettledFace();
        }

        @Override
        protected void done() {
            try {
                int result = get();
                callback.accept(result); // pass the result back when done
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }.execute();
}
    
    private void initComponents() {
        // Create and add the Canvas3D.
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        canvas = new Canvas3D(config);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(canvas, BorderLayout.CENTER);
        
        // Create a SimpleUniverse with the canvas.
        universe = new SimpleUniverse(canvas);
        
        // Create the scene (same as in Dice.java main).
        scene = new BranchGroup();
        
        // Background.
        Background bg = new Background(new Color3f(1.0f, 1.0f, 1.0f));
        bg.setApplicationBounds(new BoundingSphere(new Point3d(), 100));
        scene.addChild(bg);
        
        // Directional light.
        DirectionalLight light = new DirectionalLight(
            new Color3f(1f, 1f, 1f), new Vector3f(-1f, -1f, -1f));
        light.setInfluencingBounds(new BoundingSphere(new Point3d(), 100));
        scene.addChild(light);
        
        // Create the table (using the same dimensions as in Dice.java).
        Appearance tableApp = new Appearance();
        tableApp.setMaterial(new Material(
            new Color3f(0.4f, 0.2f, 0.1f),  // Diffuse color: darker brown.
            new Color3f(0, 0, 0),            // Ambient color.
            new Color3f(0.4f, 0.2f, 0.1f),    // Specular color.
            new Color3f(0, 0, 0),            // Emissive color.
            80.0f                          // Shininess.
        ));
        Box table = new Box(Dice.TABLE_SIZE, Dice.TABLE_HEIGHT, Dice.TABLE_SIZE, tableApp);
        scene.addChild(table);
        
        // Create the dice instance and add it to the scene.
        dice = new Dice();
        scene.addChild(dice);
        
        // Setup the viewing platform (camera) exactly as in Dice.java main.
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
        
        // Create a timer to update dice physics (like in Dice.java main).
        physicsTimer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dice.updatePhysics(0.016f);
            }
        });
        physicsTimer.start();
        
        // Set the dialog properties.
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    public int getSettledFace() {
        // Returns the upward face as determined by the current dice orientation.
        return dice.getSettledFace();
    }

    public int rollDice() {
        dice.resetRoll();
        dice.roll(); // starts the animation but doesn't immediately give the final result
        
        // Wait until dice stops rolling
        while (dice.isDiceRolling()) {
            try {
                Thread.sleep(50); // Adjust this sleep time as needed
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        return dice.getSettledFace(); // Now it will always match animation
    }
    

    public boolean isDiceRolling() {
        return dice.isDiceRolling();
    }

    
    
    public static void main(String[] args) {
        // Directly launch the DiceRollWrapper window.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                DiceRollWrapper window = new DiceRollWrapper();
                window.setVisible(true);
            }
        });
    }
}