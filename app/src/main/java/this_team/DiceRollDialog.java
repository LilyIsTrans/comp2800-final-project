package this_team;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.behaviors.vp.OrbitBehavior;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.*;

import javax.swing.*;
import java.awt.*;

public class DiceRollDialog extends JDialog {
    private Canvas3D canvas;
    private SimpleUniverse universe;
    private Dice dice;
    private TransformGroup diceTG;
    private RotationInterpolator rotator;
    private Alpha rollAlpha;
    private int forcedFaceValue = -1;
    
    public DiceRollDialog(Frame owner) {
        super(owner, "Dice Roll", true);
        initComponents();
        setupTestControls();
    }
    
    private void initComponents() {
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        canvas = new Canvas3D(config);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(canvas, BorderLayout.CENTER);
        
        universe = new SimpleUniverse(canvas);
        
        OrbitBehavior orbit = new OrbitBehavior(canvas, OrbitBehavior.REVERSE_ALL);
        orbit.setSchedulingBounds(new BoundingSphere(new Point3d(0, 0, 0), 100.0));
        universe.getViewingPlatform().setViewPlatformBehavior(orbit);
        
        BranchGroup rootBranchGroup = new BranchGroup();
        rootBranchGroup.setCapability(BranchGroup.ALLOW_DETACH);
        
        Background bg = new Background(new Color3f(1f, 1f, 1f));
        bg.setApplicationBounds(new BoundingSphere(new Point3d(0, 0, 0), 100.0));
        rootBranchGroup.addChild(bg);
        
        dice = new Dice(0.5f);
        diceTG = dice.getTransformGroup();
        
        TransformGroup diceHolder = new TransformGroup();
        diceHolder.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        diceHolder.addChild(dice);
        
        rootBranchGroup.addChild(diceHolder);
        
        rollAlpha = new Alpha(-1, 2000);
        rotator = new RotationInterpolator(rollAlpha, diceTG);
        rotator.setSchedulingBounds(new BoundingSphere(new Point3d(0, 0, 0), 100.0));
        
        dice.addChild(rotator);
        
        rootBranchGroup.compile();
        universe.addBranchGraph(rootBranchGroup);
        universe.getViewingPlatform().setNominalViewingTransform();
        
        setSize(500, 500);
        setLocationRelativeTo(getOwner());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    private void setupTestControls() {
        JPanel controlPanel = new JPanel(new GridLayout(1, 0));
        
        for (int i = 1; i <= 6; i++) {
            JButton btn = new JButton("Roll " + i);
            final int face = i;
            btn.addActionListener(e -> rollSpecificFace(face));
            controlPanel.add(btn);
        }
        
        JButton randomBtn = new JButton("Random Roll");
        randomBtn.addActionListener(e -> rollRandom());
        controlPanel.add(randomBtn);
        
        JButton spaceBtn = new JButton("SPACE Key Test");
        spaceBtn.addActionListener(e -> simulateSpacePress());
        controlPanel.add(spaceBtn);
        
        getContentPane().add(controlPanel, BorderLayout.SOUTH);
    }
    
    public void rollSpecificFace(int face) {
        setForcedFaceValue(face);
        startRoll();
    }
    
    public void rollRandom() {
        setForcedFaceValue(-1);
        startRoll();
    }
    
    public void simulateSpacePress() {
        int randomFace = (Math.random() < 0.2) ? 6 : (int)(Math.random() * 5) + 1;
        rollSpecificFace(randomFace);
        System.out.println("SPACE pressed - rolled: " + randomFace);
    }
    
    private void startRoll() {
        if (forcedFaceValue > 0) {
            Transform3D randomRotation = new Transform3D();
            randomRotation.rotX(Math.random() * Math.PI * 2);
            randomRotation.rotY(Math.random() * Math.PI * 2);
            randomRotation.rotZ(Math.random() * Math.PI * 2);
            diceTG.setTransform(randomRotation);
        }
        rollAlpha.setStartTime(System.currentTimeMillis());
    }
    
    public void setForcedFaceValue(int faceValue) {
        this.forcedFaceValue = faceValue;
        if (faceValue > 0) {
            dice.setFace(faceValue);
        }
    }
    
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame testFrame = new JFrame("Dice Roll Tester");
            testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            testFrame.setSize(300, 100);
            
            JButton launchBtn = new JButton("Open Dice Tester");
            launchBtn.addActionListener(e -> {
                DiceRollDialog tester = new DiceRollDialog(testFrame);
                tester.setVisible(true);
            });
            
            testFrame.add(launchBtn);
            testFrame.setVisible(true);
        });
    }
}