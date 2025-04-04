package this_team;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.image.TextureLoader;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.java3d.utils.universe.ViewingPlatform;
import org.jogamp.vecmath.*;
import org.jogamp.java3d.utils.geometry.Box;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class Dice extends BranchGroup {
    private static final float SIZE = 0.8f;
    private static final float TABLE_HEIGHT = 0.05f;
    private static final float TABLE_SIZE = 2.0f; // Changed from 1.0f to 2.0f

    private final TransformGroup diceTG;
    private final TransformGroup positionTG;
    private final Random random = new Random();
    private int currentFace;
    private double startX, startY;
    
    // Physics simulation
    private Vector3f position = new Vector3f(0, SIZE + TABLE_HEIGHT, 0);
    private Vector3f velocity = new Vector3f();
    private Vector3f angularVelocity = new Vector3f();
    private boolean isRolling = false;
    private int targetFace = 1;
    
    // Timer for resting contact on table
    private long contactStartTime = 0;  // Time when dice first contacts the table with low speeds

    public Dice() {
        diceTG = new TransformGroup();
        diceTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        createDice();
        
        positionTG = new TransformGroup();
        positionTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        positionTG.addChild(diceTG);
        this.addChild(positionTG);
        
        updatePositionTransform();
        this.compile();
    }

    private void createDice() {
        String[] faceTextures = {
            "./src/resources/dice/face1.png",
            "./src/resources/dice/face2.png", 
            "./src/resources/dice/face3.png",
            "./src/resources/dice/face4.png",
            "./src/resources/dice/face5.png",
            "./src/resources/dice/face6.png"
        };

        Point3f[][] faceCoords = {
            { new Point3f(-SIZE, -SIZE, SIZE), new Point3f(SIZE, -SIZE, SIZE), new Point3f(SIZE, SIZE, SIZE), new Point3f(-SIZE, SIZE, SIZE) },
            { new Point3f(SIZE, -SIZE, -SIZE), new Point3f(-SIZE, -SIZE, -SIZE), new Point3f(-SIZE, SIZE, -SIZE), new Point3f(SIZE, SIZE, -SIZE) },
            { new Point3f(-SIZE, -SIZE, -SIZE), new Point3f(-SIZE, -SIZE, SIZE), new Point3f(-SIZE, SIZE, SIZE), new Point3f(-SIZE, SIZE, -SIZE) },
            { new Point3f(SIZE, -SIZE,  SIZE), new Point3f(SIZE, -SIZE, -SIZE), new Point3f(SIZE, SIZE, -SIZE), new Point3f(SIZE, SIZE,  SIZE) },
            { new Point3f(-SIZE, SIZE,  SIZE), new Point3f(SIZE, SIZE,  SIZE), new Point3f(SIZE, SIZE, -SIZE), new Point3f(-SIZE, SIZE, -SIZE) },
            { new Point3f(-SIZE, -SIZE, -SIZE), new Point3f(SIZE, -SIZE, -SIZE), new Point3f(SIZE, -SIZE,  SIZE), new Point3f(-SIZE, -SIZE,  SIZE) }
        };

        for (int i = 0; i < 6; i++) {
            diceTG.addChild(createFace(faceCoords[i], faceTextures[i]));
        }
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

    // Add this method to allow resetting the rolling state
public void resetRoll() {
    isRolling = true;
    contactStartTime = 0;
}

// Modify the roll() method
public int roll() {
    // Reset all state for a new roll
    isRolling = true;
    contactStartTime = 0;
    
    targetFace = random.nextInt(6) + 1;
    
    position.set(
        (random.nextFloat() * 2 - 1) * (TABLE_SIZE - SIZE),
        (SIZE + TABLE_HEIGHT) * 2.0f,
        (random.nextFloat() * 2 - 1) * (TABLE_SIZE - SIZE)
    );
    
    velocity.set(
        (random.nextFloat() * 2 - 1) * 3f,
        0,
        (random.nextFloat() * 2 - 1) * 3f
    );
    
    angularVelocity.set(
        random.nextFloat() * 10 - 5,
        random.nextFloat() * 10 - 5,
        random.nextFloat() * 10 - 5
    );
    
    return targetFace;
}

// Modify the physics update to not interfere with new rolls
public void updatePhysics(float deltaTime) {
    if (!isRolling) return;
    
    // Update linear motion
    velocity.y -= 9.8f * deltaTime; // Gravity
    position.x += velocity.x * deltaTime;
    position.y += velocity.y * deltaTime;
    position.z += velocity.z * deltaTime;
    
    // Update rotation
    Transform3D currentTransform = new Transform3D();
    diceTG.getTransform(currentTransform);
    float angle = angularVelocity.length() * deltaTime;
    if (angle > 0) {
        Vector3f axis = new Vector3f(angularVelocity);
        axis.normalize();
        Transform3D deltaRotation = new Transform3D();
        deltaRotation.setRotation(new AxisAngle4f(axis.x, axis.y, axis.z, angle));
        currentTransform.mul(deltaRotation);
        diceTG.setTransform(currentTransform);
    }
    
    // Collision with table
    float diceMinY = getDiceMinY();
    if (diceMinY < TABLE_HEIGHT) {
        float penetration = TABLE_HEIGHT - diceMinY;
        position.y += penetration;
        
        velocity.y = -velocity.y * 0.5f;
        
        float dampingFactor = 0.85f;
        if (velocity.length() < 0.5f) {
            dampingFactor = 0.7f;
        }
        velocity.x *= dampingFactor;
        velocity.z *= dampingFactor;
        angularVelocity.scale(dampingFactor);
        
        boolean isMovingSlowly = velocity.length() < 0.05f && angularVelocity.length() < 0.1f;
        boolean isCloseToTable = position.y - SIZE < TABLE_HEIGHT + 0.01f;
        
        if (isMovingSlowly && isCloseToTable) {
            if (contactStartTime == 0) {
                contactStartTime = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - contactStartTime > 300) {
                velocity.set(0, 0, 0);
                angularVelocity.set(0, 0, 0);
                position.y = TABLE_HEIGHT + SIZE;
                
                Vector3f up = new Vector3f(0, 1, 0);
                int settledFace = findClosestFaceToVector(up);
                setFace(settledFace);
                
                // Only set isRolling to false if we're actually settling
                // Don't set it false if a new roll has been initiated
                if (velocity.length() == 0 && angularVelocity.length() == 0) {
                    isRolling = false;
                }
            }
        } else {
            contactStartTime = 0;
        }
    } else {
        contactStartTime = 0;
    }
    
    // Boundary checks
    float tableBound = TABLE_SIZE - SIZE;
    if (Math.abs(position.x) > tableBound) {
        position.x = Math.signum(position.x) * tableBound;
        velocity.x = -velocity.x * 0.5f;
    }
    if (Math.abs(position.z) > tableBound) {
        position.z = Math.signum(position.z) * tableBound;
        velocity.z = -velocity.z * 0.5f;
    }
    
    updatePositionTransform();
}
    
    // Computes the minimum Y coordinate of the dice in world space by transforming its eight vertices.
    private float getDiceMinY() {
        // Combine the position and dice transforms
        Transform3D fullTransform = new Transform3D();
        positionTG.getTransform(fullTransform);
        Transform3D diceTransform = new Transform3D();
        diceTG.getTransform(diceTransform);
        fullTransform.mul(diceTransform);
        
        float minY = Float.MAX_VALUE;
        for (int ix = -1; ix <= 1; ix += 2) {
            for (int iy = -1; iy <= 1; iy += 2) {
                for (int iz = -1; iz <= 1; iz += 2) {
                    Point3f vertex = new Point3f(ix * SIZE, iy * SIZE, iz * SIZE);
                    fullTransform.transform(vertex);
                    if (vertex.y < minY) {
                        minY = vertex.y;
                    }
                }
            }
        }
        return minY;
    }
    
    private void updatePositionTransform() {
        Transform3D posTransform = new Transform3D();
        posTransform.setTranslation(position);
        positionTG.setTransform(posTransform);
    }
    
    private int findClosestFaceToVector(Vector3f worldVec) {
        Transform3D diceTransform = new Transform3D();
        diceTG.getTransform(diceTransform);
        
        Vector3f localVec = new Vector3f(worldVec);
        diceTransform.invert();
        diceTransform.transform(localVec);
        
        Vector3f[] faceNormals = {
            new Vector3f(0, 0, 1), new Vector3f(0, 0, -1),
            new Vector3f(-1, 0, 0), new Vector3f(1, 0, 0),
            new Vector3f(0, 1, 0), new Vector3f(0, -1, 0)
        };
        
        int bestFace = 1;
        float bestDot = -1;
        for (int i = 0; i < 6; i++) {
            float dot = faceNormals[i].dot(localVec);
            if (dot > bestDot) {
                bestDot = dot;
                bestFace = i + 1;
            }
        }
        return bestFace;
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
    
    // --- Modified setFace() to align with the reset view ---
    public void setFace(int faceValue) {
        Transform3D transform = new Transform3D();
        transform.setIdentity();
        switch (faceValue) {
            case 1:
                // Face 1 (front, normal (0,0,1)) becomes up by rotating -90° about X.
                transform.rotX(-Math.PI/2);
                break;
            case 2:
                // Face 2 (back, normal (0,0,-1)) becomes up by rotating +90° about X.
                transform.rotX(Math.PI/2);
                break;
            case 3:
                // Face 3 (left, normal (-1,0,0)) becomes up by rotating -90° about Z.
                transform.rotZ(-Math.PI/2);
                break;
            case 4:
                // Face 4 (right, normal (1,0,0)) becomes up by rotating +90° about Z.
                transform.rotZ(Math.PI/2);
                break;
            case 5:
                // Face 5 (top, normal (0,1,0)) is already up.
                break;
            case 6:
                // Face 6 (bottom, normal (0,-1,0)) becomes up by rotating 180° about X.
                transform.rotX(Math.PI);
                break;
        }
        diceTG.setTransform(transform);
        currentFace = faceValue;
    }
    // ------------------------------------------------------
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("3D Dice with Physics");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        
        Canvas3D canvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
        frame.add(canvas, BorderLayout.CENTER);
        
        BranchGroup scene = new BranchGroup();
        
        Background bg = new Background(new Color3f(0.2f, 0.2f, 0.4f));
        bg.setApplicationBounds(new BoundingSphere(new Point3d(), 100));
        scene.addChild(bg);
        
        DirectionalLight light = new DirectionalLight(
            new Color3f(1, 1, 1), new Vector3f(-1, -1, -1));
        light.setInfluencingBounds(new BoundingSphere(new Point3d(), 100));
        scene.addChild(light);
        
        Appearance tableApp = new Appearance();
        tableApp.setMaterial(new Material(
            new Color3f(0.6f, 0.3f, 0.0f),
            new Color3f(0, 0, 0),
            new Color3f(0.6f, 0.3f, 0.0f),
            new Color3f(0, 0, 0),
            80.0f
        ));
        Box table = new Box(TABLE_SIZE, TABLE_HEIGHT, TABLE_SIZE, tableApp);
        scene.addChild(table);
        
        Dice dice = new Dice();
        scene.addChild(dice);
        
        SimpleUniverse universe = new SimpleUniverse(canvas);
        
        // Set up the camera for top-down view
        ViewingPlatform viewingPlatform = universe.getViewingPlatform();
        TransformGroup vpGroup = viewingPlatform.getViewPlatformTransform();
        
        Transform3D viewTransform = new Transform3D();
        viewTransform.setTranslation(new Vector3f(0.0f, 5.0f, 0.0f));
        
        Point3d eye = new Point3d(0.0, 10.0, 0.0);
        Point3d center = new Point3d(0.0, 0.0, 0.0);
        Vector3d up = new Vector3d(0.0, 0.0, -1.0);
        viewTransform.lookAt(eye, center, up);
        viewTransform.invert();
        
        vpGroup.setTransform(viewTransform);
        
        universe.addBranchGraph(scene);
        
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
                double x = e.getX(), y = e.getY();
                double dx = x - dice.startX, dy = y - dice.startY;
                Transform3D currentTransform = new Transform3D();
                vpGroup.getTransform(currentTransform);
                Transform3D xRot = new Transform3D(), yRot = new Transform3D();
                yRot.rotY(-dx * 0.01);
                xRot.rotX(-dy * 0.01);
                currentTransform.mul(yRot);
                currentTransform.mul(xRot);
                vpGroup.setTransform(currentTransform);
                dice.startX = x;
                dice.startY = y;
            }
        });
        
        // WASD controls
        canvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                Transform3D currentTransform = new Transform3D();
                vpGroup.getTransform(currentTransform);
                Vector3d translation = new Vector3d();
                currentTransform.get(translation);
                Matrix3d rotation = new Matrix3d();
                currentTransform.get(rotation);
                double moveStep = 0.1;
                Vector3d delta = new Vector3d();
                switch(e.getKeyChar()) {
                    case 'w': delta.set(0, 0, -moveStep); break;
                    case 's': delta.set(0, 0, moveStep); break;
                    case 'a': delta.set(-moveStep, 0, 0); break;
                    case 'd': delta.set(moveStep, 0, 0); break;
                    case 'q': delta.set(0, moveStep, 0); break;
                    case 'e': delta.set(0, -moveStep, 0); break;
                    default: break;
                }
                rotation.transform(delta);
                translation.add(delta);
                currentTransform.setTranslation(translation);
                vpGroup.setTransform(currentTransform);
            }
        });
        canvas.setFocusable(true);
        canvas.requestFocus();
        
        JPanel panel = new JPanel(new GridLayout(2, 4));
        for (int i = 1; i <= 6; i++) {
            final int face = i;
            JButton btn = new JButton("Face " + face);
            btn.addActionListener(e -> dice.setFace(face));
            panel.add(btn);
        }
        
        JButton rollBtn = new JButton("Roll");
        rollBtn.addActionListener(e -> {
            dice.resetRoll();
            int result = dice.roll();
            System.out.println("Roll initiated; final face will be: " + result);
        });
        panel.add(rollBtn);
        
        JButton resetViewBtn = new JButton("Reset View");
        resetViewBtn.addActionListener(e -> {
            Transform3D viewTransformReset = new Transform3D();
            viewTransformReset.setTranslation(new Vector3f(0.0f, 5.0f, 0.0f));
            Point3d eyeReset = new Point3d(0.0, 10.0, 0.0);
            Point3d centerReset = new Point3d(0.0, 0.0, 0.0);
            Vector3d upReset = new Vector3d(0.0, 0.0, -1.0);
            viewTransformReset.lookAt(eyeReset, centerReset, upReset);
            viewTransformReset.invert();
            vpGroup.setTransform(viewTransformReset);
        });
        panel.add(resetViewBtn);
        
        // --- Added Test Camera Angle Button ---
        JButton testCameraAngleBtn = new JButton("Test Camera Angle");
        testCameraAngleBtn.addActionListener(e -> {
            Transform3D viewTransformTest = new Transform3D();
            Point3d eyeReset = new Point3d(0.0, 0.0, 10.0);
            Point3d centerReset = new Point3d(0.0, 0.0, 0.0);
            Vector3d upReset = new Vector3d(0.0, 1.0, 0.0);
            viewTransformTest.lookAt(eyeReset, centerReset, upReset);
            viewTransformTest.invert();
            vpGroup.setTransform(viewTransformTest);
        });
        panel.add(testCameraAngleBtn);
        // -------------------------------------
        
        frame.add(panel, BorderLayout.SOUTH);
        
        Timer physicsTimer = new Timer(16, e -> dice.updatePhysics(0.016f));
        physicsTimer.start();
        
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
}