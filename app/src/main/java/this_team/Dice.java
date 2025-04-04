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
    static final float TABLE_HEIGHT = 0.f;
    // Table size increased by 70%
    static final float TABLE_SIZE = 5.0f;
    // Settling duration is now 500 ms (half the previous duration)
    private static final long SETTLING_DURATION = 500; 

    private final TransformGroup diceTG;
    private final TransformGroup positionTG;
    private final Random random = new Random();
    private int currentFace;
    
    private Vector3f position = new Vector3f(0, SIZE + TABLE_HEIGHT, 0);
    private Vector3f velocity = new Vector3f();
    private Vector3f angularVelocity = new Vector3f();
    private boolean isRolling = false;
    
    // For smooth settling
    private boolean isSettling = false;
    private Transform3D settleStartTransform = new Transform3D();
    private long contactStartTime = 0;
    private Transform3D settleTargetTransform = new Transform3D();
    private long settleStartTime = 0;
    private Vector3f settleAxis = new Vector3f();
    private float settleTotalAngle = 0;

    public double startX, startY;
    
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

    public void resetRoll() {
        isRolling = true;
        isSettling = false;
        contactStartTime = 0;
    }

    public int roll() {
        resetRoll();
        
        int targetFace = random.nextInt(6) + 1;
        
        position.set(
            (random.nextFloat() * 2 - 1) * (TABLE_SIZE * 0.3f),
            SIZE + TABLE_HEIGHT + 2.0f,
            (random.nextFloat() * 2 - 1) * (TABLE_SIZE * 0.3f)
        );
        
        velocity.set(
            (random.nextFloat() * 2 - 1) * 2.5f,
            -3.5f,
            (random.nextFloat() * 2 - 1) * 2.5f
        );
        
        float spinX = (random.nextFloat() * 10 - 5) * (velocity.z > 0 ? 1 : -1);
        float spinZ = (random.nextFloat() * 10 - 5) * (velocity.x > 0 ? -1 : 1);
        angularVelocity.set(
            spinX,
            random.nextFloat() * 15 - 7.5f,
            spinZ
        );
        
        angularVelocity.x += velocity.z * 2;
        angularVelocity.z -= velocity.x * 2;
        
        return targetFace;
    }

    public void updatePhysics(float deltaTime) {
        if (isSettling) {
            smoothSettle();
            return;
        }
        
        if (!isRolling) return;
        
        velocity.y -= 10.5f * deltaTime;
        
        position.x += velocity.x * deltaTime;
        position.y += velocity.y * deltaTime;
        position.z += velocity.z * deltaTime;
        
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
        
        float restingCenterY = TABLE_HEIGHT + SIZE;
        if (position.y <= restingCenterY + 0.01f) {
            if (position.y < restingCenterY) {
                float penetration = restingCenterY - position.y;
                position.y += penetration * 0.7f;
                velocity.y = -velocity.y * 0.5f;
                float frictionFactor = 0.92f;
                velocity.x *= frictionFactor;
                velocity.z *= frictionFactor;
                angularVelocity.scale(0.9f);
                
                if (Math.abs(velocity.x) > 0.1f) {
                    angularVelocity.z += velocity.x * 0.5f;
                }
                if (Math.abs(velocity.z) > 0.1f) {
                    angularVelocity.x -= velocity.z * 0.5f;
                }
            }
            
            // Immediately initiate smooth settle when thresholds are met
            if (velocity.length() < 0.2f && angularVelocity.length() < 0.3f) {
                if (!isSettling) {
                    beginSmoothSettle();
                }
            } else {
                contactStartTime = 0;
            }
        } else {
            contactStartTime = 0;
        }
        
        float tableBound = TABLE_SIZE - SIZE;
        if (Math.abs(position.x) > tableBound) {
            position.x = Math.signum(position.x) * tableBound;
            velocity.x = -velocity.x * 0.6f;
            velocity.z *= 0.85f;
        }
        if (Math.abs(position.z) > tableBound) {
            position.z = Math.signum(position.z) * tableBound;
            velocity.z = -velocity.z * 0.6f;
            velocity.x *= 0.85f;
        }
        
        updatePositionTransform();
    }
    
    private void beginSmoothSettle() {
        float restingCenterY = TABLE_HEIGHT + SIZE;
        position.y = restingCenterY;
        velocity.set(0, 0, 0);
        angularVelocity.set(0, 0, 0);
        
        currentFace = findClosestFaceToVector(new Vector3f(0, 1, 0));
        int settledFace = currentFace;
        diceTG.getTransform(settleStartTransform);
        settleTargetTransform = getFaceTransform(settledFace);
        
        // Calculate rotation needed between current and target
        settleStartTime = System.currentTimeMillis();
        isSettling = true;
        
        // Calculate relative rotation between current and target transforms
        Transform3D delta = new Transform3D();
        delta.mulInverse(settleStartTransform);
        delta.mul(settleTargetTransform);
        
        // Get the rotation axis and angle
        Matrix3d rotationMatrix = new Matrix3d();
        delta.get(rotationMatrix);
        settleAxis.set(0, 1, 0); // Default axis
        settleTotalAngle = 0;
        
        // Convert rotation matrix to axis-angle
        AxisAngle4f axisAngle = new AxisAngle4f();
        axisAngle.set(rotationMatrix);
        settleAxis.set(axisAngle.x, axisAngle.y, axisAngle.z);
        settleTotalAngle = axisAngle.angle;
        
        System.out.println("Dice settling on face: " + settledFace);
        // Reset contact timer so this doesn't trigger repeatedly
        contactStartTime = 0;
    }
    
    private void smoothSettle() {
        long now = System.currentTimeMillis();
        float t = (now - settleStartTime) / (float) SETTLING_DURATION;
        
        if (t >= 1.0f) {
            diceTG.setTransform(settleTargetTransform);
            isSettling = false;
            isRolling = false;  // Mark roll as complete
        } else {
            // Calculate current rotation angle
            float currentAngle = settleTotalAngle * t;
            
            // Create incremental rotation
            Transform3D rotation = new Transform3D();
            rotation.setRotation(new AxisAngle4f(settleAxis.x, settleAxis.y, settleAxis.z, currentAngle));
            
            // Apply rotation to starting transform
            Transform3D currentTransform = new Transform3D(settleStartTransform);
            currentTransform.mul(rotation);
            diceTG.setTransform(currentTransform);
        }
        
        position.y = TABLE_HEIGHT + SIZE;
        updatePositionTransform();
    }
    
    private Transform3D getFaceTransform(int faceValue) {
        Transform3D transform = new Transform3D();
        transform.setIdentity();
        switch (faceValue) {
            case 1: transform.rotX(-Math.PI/2); break;
            case 2: transform.rotX(Math.PI/2); break;
            case 3: transform.rotZ(-Math.PI/2); break;
            case 4: transform.rotZ(Math.PI/2); break;
            case 5: break;
            case 6: transform.rotX(Math.PI); break;
        }
        return transform;
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
    
    public void setFace(int faceValue) {
        Transform3D transform = getFaceTransform(faceValue);
        diceTG.setTransform(transform);
        currentFace = faceValue;
    }
    
    // New method for simulation of 1000 random rolls.
    public void simulate() {
        // Array to count occurrences for faces 1..6 (index 0 is unused)
        int[] counts = new int[7];
        final int NUM_SIMULATIONS = 1000;
        // For each simulation...
        for (int i = 0; i < NUM_SIMULATIONS; i++) {
            // Initiate a roll.
            roll();
            // Simulate the physics update until the dice is no longer rolling or settling.
            // We force the settling phase to complete quickly by adjusting settleStartTime.
            while (isRolling || isSettling) {
                // If in settling phase, fast-forward by forcing the settle time to expire.
                if (isSettling) {
                    settleStartTime = System.currentTimeMillis() - SETTLING_DURATION;
                }
                updatePhysics(0.016f);
            }
            // After simulation is complete, determine the final face.
            int finalFace = findClosestFaceToVector(new Vector3f(0, 1, 0));
            counts[finalFace]++;
        }
        // Print the results in the terminal.
        System.out.println("Simulation complete: " + NUM_SIMULATIONS + " rolls.");
        for (int face = 1; face <= 6; face++) {
            System.out.println("Face " + face + ": " + counts[face]);
        }
    }
    
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
        
        universe.addBranchGraph(scene);
        
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
        
        JPanel panel = new JPanel(new GridLayout(2, 5));
        for (int i = 1; i <= 6; i++) {
            final int face = i;
            JButton btn = new JButton("Face " + face);
            btn.addActionListener(e -> dice.setFace(face));
            panel.add(btn);
        }
        
        JButton rollBtn = new JButton("Random Roll");
        rollBtn.addActionListener(e -> {
            dice.resetRoll();
            int result = dice.roll();
            System.out.println("Roll initiated; final face will be: " + result);
        });
        panel.add(rollBtn);
        
        JButton simulateBtn = new JButton("Simulate");
        simulateBtn.addActionListener(e -> {
            // Run the simulation in a separate thread so as not to block the UI
            new Thread(() -> {
                dice.simulate();
            }).start();
        });
        panel.add(simulateBtn);
        
        JButton resetViewBtn = new JButton("Reset View");
        resetViewBtn.addActionListener(e -> {
            Transform3D viewTransformReset = new Transform3D();
            viewTransformReset.setTranslation(new Vector3f(0.0f, 5.0f, 0.0f));
            Point3d eyeReset = new Point3d(0.0, 25.0, 0.0);
            Point3d centerReset = new Point3d(0.0, 0.0, 0.0);
            Vector3d upReset = new Vector3d(0.0, 0.0, -1.0);
            viewTransformReset.lookAt(eyeReset, centerReset, upReset);
            viewTransformReset.invert();
            vpGroup.setTransform(viewTransformReset);
        });
        panel.add(resetViewBtn);
        
        JButton testCameraAngleBtn = new JButton("Test Camera Angle");
        testCameraAngleBtn.addActionListener(e -> {
            Transform3D viewTransformTest = new Transform3D();
            Point3d eyeReset = new Point3d(0.0, 0.0, 25.0);
            Point3d centerReset = new Point3d(0.0, 0.0, 0.0);
            Vector3d upReset = new Vector3d(0.0, 1.0, 0.0);
            viewTransformTest.lookAt(eyeReset, centerReset, upReset);
            viewTransformTest.invert();
            vpGroup.setTransform(viewTransformTest);
        });
        panel.add(testCameraAngleBtn);
        
        frame.add(panel, BorderLayout.SOUTH);
        
        Timer physicsTimer = new Timer(16, e -> dice.updatePhysics(0.016f));
        physicsTimer.start();
        
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
}