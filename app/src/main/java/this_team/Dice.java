package this_team;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.image.TextureLoader;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class Dice extends BranchGroup {
    private TransformGroup diceTG;
    private float size;
    private Alpha rollAlpha;
    private RotationInterpolator rotator;
    private Random random = new Random();
    private int currentFace = 5; // Default starting face
    
    // For mouse rotation
    private double startX, startY;
    
    // For WASD movement
    private static boolean wPressed = false;
    private static boolean aPressed = false;
    private static boolean sPressed = false;
    private static boolean dPressed = false;

    public Dice(float size) {
        this.size = size;
        diceTG = new TransformGroup();
        diceTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        diceTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        createDice();
        setupAnimation();
        this.addChild(diceTG);
        this.compile();
    }
    
    private void createDice() {
        // Create all 6 faces
        String[] faceTextures = {
            "./src/resources/dice/face1.png",
            "./src/resources/dice/face2.png", 
            "./src/resources/dice/face3.png",
            "./src/resources/dice/face4.png",
            "./src/resources/dice/face5.png",
            "./src/resources/dice/face6.png"
        };
        
        // Front (1), Back (2), Left (3), Right (4), Top (5), Bottom (6)
        Point3f[][] faceCoords = {
            {new Point3f(-size,-size,size), new Point3f(size,-size,size), new Point3f(size,size,size), new Point3f(-size,size,size)},
            {new Point3f(size,-size,-size), new Point3f(-size,-size,-size), new Point3f(-size,size,-size), new Point3f(size,size,-size)},
            {new Point3f(-size,-size,-size), new Point3f(-size,-size,size), new Point3f(-size,size,size), new Point3f(-size,size,-size)},
            {new Point3f(size,-size,size), new Point3f(size,-size,-size), new Point3f(size,size,-size), new Point3f(size,size,size)},
            {new Point3f(-size,size,size), new Point3f(size,size,size), new Point3f(size,size,-size), new Point3f(-size,size,-size)},
            {new Point3f(-size,-size,-size), new Point3f(size,-size,-size), new Point3f(size,-size,size), new Point3f(-size,-size,size)}
        };

        for (int i = 0; i < 6; i++) {
            diceTG.addChild(createFace(faceCoords[i], faceTextures[i]));
        }
    }
    
    private void setupAnimation() {
        rollAlpha = new Alpha(1, 2000); 
        Transform3D yAxis = new Transform3D();
        yAxis.rotY(0); // Initial axis (will be updated)
        
        rotator = new RotationInterpolator(rollAlpha, diceTG, yAxis, 0.0f, (float)Math.PI*8);
        rotator.setSchedulingBounds(new BoundingSphere(new Point3d(0,0,0), 100));
        this.addChild(rotator);
    }
    
    private Shape3D createFace(Point3f[] coords, String textureFile) {
        QuadArray quad = new QuadArray(4, GeometryArray.COORDINATES | GeometryArray.TEXTURE_COORDINATE_2);
        quad.setCoordinates(0, coords);
        
        TexCoord2f[] texCoords = {
            new TexCoord2f(0.0f, 0.0f),
            new TexCoord2f(1.0f, 0.0f),
            new TexCoord2f(1.0f, 1.0f),
            new TexCoord2f(0.0f, 1.0f)
        };
        quad.setTextureCoordinates(0, 0, texCoords);
        
        Appearance app = new Appearance();
        TextureLoader loader = new TextureLoader(textureFile, null);
        app.setTexture(loader.getTexture());
        
        Material material = new Material();
        material.setDiffuseColor(new Color3f(1f, 1f, 1f));
        material.setLightingEnable(true);
        app.setMaterial(material);
        
        return new Shape3D(quad, app);
    }
    
    public int roll() {
        int result = random.nextInt(6) + 1;
        rollToFace(result);
        return result;
    }
    
    public void rollToFace(int targetFace) {
        Transform3D currentTransform = new Transform3D();
        diceTG.getTransform(currentTransform);
        
        int fullRotations = random.nextInt(4) + 3;
        float targetAngle = (float)(fullRotations * 2 * Math.PI);
        
        Transform3D rotationAxis = new Transform3D();
        switch(targetFace) {
            case 1: break;
            case 2: rotationAxis.rotX(Math.PI); break;
            case 3: rotationAxis.rotY(Math.PI/2); break;
            case 4: rotationAxis.rotY(-Math.PI/2); break;
            case 5: rotationAxis.setIdentity(); break;
            case 6: rotationAxis.rotX(-Math.PI/2); break;
        }
        
        setFace(targetFace);
        diceTG.setTransform(currentTransform);
        
        rotator.setAxisOfRotation(rotationAxis);
        rotator.setMinimumAngle(0.0f);
        rotator.setMaximumAngle(targetAngle);
        
        rollAlpha.setStartTime(System.currentTimeMillis());
        currentFace = targetFace;
    }
    
    public void setFace(int faceValue) {
        Transform3D transform = new Transform3D();
        transform.setIdentity();
        
        switch (faceValue) {
            case 1: break;
            case 2: transform.rotX(Math.PI); break;
            case 3: transform.rotY(Math.PI/2); break;
            case 4: transform.rotY(-Math.PI/2); break;
            case 5: transform.rotX(Math.PI/2); break;
            case 6: transform.rotX(-Math.PI/2); break;
        }
        
        diceTG.setTransform(transform);
        currentFace = faceValue;
    }
    
    public void printCurrentRotation() {
        Transform3D currentTransform = new Transform3D();
        diceTG.getTransform(currentTransform);
        
        Vector3d axis = new Vector3d();
        double angle = 0;
        currentTransform.get(axis);
        
        System.out.printf("Current rotation: axis [%.2f, %.2f, %.2f], angle: %.2f radians (%.2f degrees)%n",
                axis.x, axis.y, axis.z, angle, Math.toDegrees(angle));
    }
    
    public static void main(String[] args) {
        // Setup frame
        JFrame frame = new JFrame("3D Dice");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
    
        // Create 3D canvas
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        Canvas3D canvas = new Canvas3D(config);
        frame.add(canvas, BorderLayout.CENTER);
    
        // Create scene with black background
        BranchGroup scene = new BranchGroup();
        Background bg = new Background(new Color3f(0, 0, 0));
        bg.setApplicationBounds(new BoundingSphere(new Point3d(0, 0, 0), 100));
        scene.addChild(bg);
    
        // Add lighting
        DirectionalLight light = new DirectionalLight(
            new Color3f(1, 1, 1), new Vector3f(0, -1, -1));
        light.setInfluencingBounds(new BoundingSphere(new Point3d(0, 0, 0), 100));
        scene.addChild(light);
    
        // Create dice and add it to the scene
        Dice dice = new Dice(0.8f);
        scene.addChild(dice);
    
        // Setup universe
        SimpleUniverse universe = new SimpleUniverse(canvas);
        universe.getViewingPlatform().setNominalViewingTransform();
        
        // Use the built-in view platform transform group
        TransformGroup viewTG = universe.getViewingPlatform().getViewPlatformTransform();
    
        // Mouse controls
        canvas.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                dice.startX = e.getX();
                dice.startY = e.getY();
            }
            
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    dice.printCurrentRotation();
                }
            }
        });
    
        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                double x = e.getX();
                double y = e.getY();
                
                double dx = x - dice.startX;
                double dy = y - dice.startY;
                
                Transform3D currentTransform = new Transform3D();
                viewTG.getTransform(currentTransform);
                
                Transform3D xRot = new Transform3D();
                Transform3D yRot = new Transform3D();
                
                yRot.rotY(-dx * 0.01);
                xRot.rotX(-dy * 0.01);
                
                yRot.mul(xRot);
                currentTransform.mul(yRot);
                viewTG.setTransform(currentTransform);
                
                dice.startX = x;
                dice.startY = y;
            }
        });
        
        // Keyboard controls for WASD movement
        canvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W: wPressed = true; break;
                    case KeyEvent.VK_A: aPressed = true; break;
                    case KeyEvent.VK_S: sPressed = true; break;
                    case KeyEvent.VK_D: dPressed = true; break;
                }
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W: wPressed = false; break;
                    case KeyEvent.VK_A: aPressed = false; break;
                    case KeyEvent.VK_S: sPressed = false; break;
                    case KeyEvent.VK_D: dPressed = false; break;
                }
            }
        });
        canvas.setFocusable(true);
        canvas.requestFocus();
        
        // Movement timer
        Timer movementTimer = new Timer(16, e -> {
            if (wPressed || aPressed || sPressed || dPressed) {
                Transform3D currentTransform = new Transform3D();
                viewTG.getTransform(currentTransform);
                
                Vector3f translation = new Vector3f();
                float moveSpeed = 0.05f;
                
                if (wPressed) translation.z -= moveSpeed; // Forward
                if (sPressed) translation.z += moveSpeed; // Backward
                if (aPressed) translation.x -= moveSpeed; // Left
                if (dPressed) translation.x += moveSpeed; // Right
                
                Transform3D translationTransform = new Transform3D();
                translationTransform.setTranslation(translation);
                currentTransform.mul(translationTransform);
                
                viewTG.setTransform(currentTransform);
            }
        });
        movementTimer.start();
        
        // Reset view button
        JButton resetViewBtn = new JButton("Reset View");
        resetViewBtn.addActionListener(e -> {
            Transform3D identity = new Transform3D();
            viewTG.setTransform(identity);
            universe.getViewingPlatform().setNominalViewingTransform();
        });
        
        // Add test buttons for rolling to different faces
        JPanel panel = new JPanel(new GridLayout(2, 4));
        for (int i = 1; i <= 6; i++) {
            final int face = i;
            JButton btn = new JButton("Face " + face);
            btn.addActionListener(e -> {
                System.out.println("Setting face " + face + " up");
                dice.setFace(face);
            });
            panel.add(btn);
        }
        
        // Add roll button
        JButton rollBtn = new JButton("Random Roll");
        rollBtn.addActionListener(e -> {
            int result = dice.roll();
            System.out.println("Rolled: " + result);
        });
        panel.add(rollBtn);
        panel.add(resetViewBtn);
        
        frame.add(panel, BorderLayout.SOUTH);
        
        // Final setup
        scene.compile();
        universe.addBranchGraph(scene);
        frame.setSize(600, 600);
        frame.setVisible(true);
    }

    public TransformGroup getTransformGroup() {
        return diceTG;
    }
}







