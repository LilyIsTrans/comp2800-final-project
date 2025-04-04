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
    
    // Physics simulation: position represents the center of mass.
    private Vector3f position = new Vector3f(0, SIZE + TABLE_HEIGHT, 0);
    private Vector3f velocity = new Vector3f();
    private Vector3f angularVelocity = new Vector3f();
    private boolean isRolling = false;
    
    // Timer for detecting stable contact.
    private long contactStartTime = 0;

    // Fields used for mouse control.
    public double startX, startY;
    
    // Fields for smooth settling interpolation.
    private boolean isSettling = false;
    private Transform3D settleStartTransform = new Transform3D();
    private Transform3D settleTargetTransform = new Transform3D();
    private long settleStartTime = 0;
    private static final long SETTLING_DURATION = 500; // in ms

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

    // Resets the rolling state.
    public void resetRoll() {
        isRolling = true;
        isSettling = false;
        contactStartTime = 0;
    }

    // Physics update method that lets gravity do most of the work.
    public void updatePhysics(float deltaTime) {
        // If we are already in the smooth settling phase, interpolate the rotation.
        if (isSettling) {
            smoothSettle();
            return;
        }
        
        if (!isRolling) return;
        
        // Apply gravity.
        velocity.y -= 9.8f * deltaTime;
        
        // Update linear motion (center-of-mass).
        position.x += velocity.x * deltaTime;
        position.y += velocity.y * deltaTime;
        position.z += velocity.z * deltaTime;
        
        // Update rotation using angular velocity.
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
        
        // Soft collision detection using center-of-mass.
        float restingCenterY = TABLE_HEIGHT + SIZE;
        if (position.y <= restingCenterY + 0.01f) {
            if (position.y < restingCenterY) {
                float penetration = restingCenterY - position.y;
                position.y += penetration * 0.5f; // partial correction
                velocity.y = -velocity.y * 0.3f;  // soft bounce
                float frictionFactor = 0.85f;
                velocity.x *= frictionFactor;
                velocity.z *= frictionFactor;
                angularVelocity.scale(0.8f);
            }
            
            // Check if the dice is nearly motionless.
            if (velocity.length() < 0.1f && angularVelocity.length() < 0.2f) {
                if (contactStartTime == 0) {
                    contactStartTime = System.currentTimeMillis();
                } else if (System.currentTimeMillis() - contactStartTime > 500) {
                    beginSmoothSettle();
                }
            } else {
                contactStartTime = 0;
            }
        } else {
            contactStartTime = 0;
        }
        
        // Boundary checks for table edges.
        float tableBound = TABLE_SIZE - SIZE;
        if (Math.abs(position.x) > tableBound) {
            position.x = Math.signum(position.x) * tableBound;
            velocity.x = -velocity.x * 0.4f;
            velocity.z *= 0.7f;
        }
        if (Math.abs(position.z) > tableBound) {
            position.z = Math.signum(position.z) * tableBound;
            velocity.z = -velocity.z * 0.4f;
            velocity.x *= 0.7f;
        }
        
        updatePositionTransform();
    }
    
    // Begins the smooth settling phase.
    private void beginSmoothSettle() {
        float restingCenterY = TABLE_HEIGHT + SIZE;
        position.y = restingCenterY;
        velocity.set(0, 0, 0);
        angularVelocity.set(0, 0, 0);
        
        int settledFace = findClosestFaceToVector(new Vector3f(0, 1, 0));
        diceTG.getTransform(settleStartTransform);
        settleTargetTransform = getFaceTransform(settledFace);
        
        settleStartTime = System.currentTimeMillis();
        isSettling = true;
        System.out.println("Dice settling on face: " + settledFace);
    }
    
    // Smoothly interpolates between the current and target transforms.
    private void smoothSettle() {
        long now = System.currentTimeMillis();
        float t = (now - settleStartTime) / (float) SETTLING_DURATION;
        if (t >= 1.0f) {
            diceTG.setTransform(settleTargetTransform);
            isSettling = false;
            isRolling = false;
        } else {
            Transform3D interp = new Transform3D();
            Transform3D temp = new Transform3D(settleStartTransform);
            temp.mul(1.0 - t);
            Transform3D temp2 = new Transform3D(settleTargetTransform);
            temp2.mul(t);
            interp.add(temp, temp2);
            diceTG.setTransform(interp);
        }
        // Lock the dice's vertical position.
        position.y = TABLE_HEIGHT + SIZE;
        updatePositionTransform();
    }
    
    // Returns a transform aligning the specified face upward.
    private Transform3D getFaceTransform(int faceValue) {
        Transform3D transform = new Transform3D();
        transform.setIdentity();
        switch (faceValue) {
            case 1:
                transform.rotX(-Math.PI / 2);
                break;
            case 2:
                transform.rotX(Math.PI / 2);
                break;
            case 3:
                transform.rotZ(-Math.PI / 2);
                break;
            case 4:
                transform.rotZ(Math.PI / 2);
                break;
            case 5:
                // No rotation needed.
                break;
            case 6:
                transform.rotX(Math.PI);
                break;
        }
        return transform;
    }
    
    // Roll method with controlled initial conditions.
    public int roll() {
        isRolling = true;
        isSettling = false;
        contactStartTime = 0;
        
        int targetFace = random.nextInt(6) + 1;
        position.set(
            (random.nextFloat() * 2 - 1) * (TABLE_SIZE * 0.5f),
            SIZE + TABLE_HEIGHT + 1.5f,
            (random.nextFloat() * 2 - 1) * (TABLE_SIZE * 0.5f)
        );
        velocity.set(
            (random.nextFloat() * 2 - 1) * 1.5f,
            -2.0f,
            (random.nextFloat() * 2 - 1) * 1.5f
        );
        angularVelocity.set(
            random.nextFloat() * 8 - 4,
            random.nextFloat() * 8 - 4,
            random.nextFloat() * 8 - 4
        );
        
        return targetFace;
    }
    
    // Updates the transform based on the center-of-mass.
    private void updatePositionTransform() {
        Transform3D posTransform = new Transform3D();
        posTransform.setTranslation(position);
        positionTG.setTransform(posTransform);
    }
    
    // Determines which face is most nearly upward.
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
    
    // Immediately aligns the dice to the given face.
    public void setFace(int faceValue) {
        Transform3D transform = getFaceTransform(faceValue);
        diceTG.setTransform(transform);
        currentFace = faceValue;
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
        Point3d eye = new Point3d(0.0, 10.0, 0.0);
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
        
        JPanel panel = new JPanel(new GridLayout(2, 4));
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
        
        frame.add(panel, BorderLayout.SOUTH);
        
        Timer physicsTimer = new Timer(16, e -> dice.updatePhysics(0.016f));
        physicsTimer.start();
        
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
}