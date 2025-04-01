package this_team;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.behaviors.vp.ViewPlatformBehavior;
import org.jogamp.java3d.utils.universe.ViewingPlatform;
import org.jogamp.vecmath.AxisAngle4d;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Vector3d;

import java.awt.event.*;
import java.util.Iterator;

public class Camera extends ViewPlatformBehavior implements KeyListener, MouseListener, MouseMotionListener {
    private Point3d center;
    private Point3d eye;
    private Vector3d up;
    private Vector3d forward;
    private final double CAMERA_SPEED = 0.1d;
    private final double CAMERA_ROTATION_SPEED = 0.03d;
    private boolean wDown;
    private boolean sDown;
    private boolean aDown;
    private boolean dDown;
    private boolean eDown;
    private boolean qDown;
    private boolean rDown;
    private boolean fDown;
    private boolean mouseDown;
    private int mouseX;
    private int mouseY;
    private int dmouseX;
    private int dmouseY;
    private boolean mouseReady;
    private final double MOUSE_SPEED = 10.0d;
    private int scale;


    Camera(int scale) {
        this.scale = scale;
        eye = new Point3d(0, 0, 15);
        center = new Point3d(0, 0, 0);
        up = new Vector3d(0, 1, 0);
        forward = new Vector3d(0, 0, -1);

    }

    private void setView() {
        ViewingPlatform vp = this.getViewingPlatform();
        TransformGroup cameraTG = vp.getViewPlatformTransform();

        Transform3D viewTransform = new Transform3D();
        viewTransform.lookAt(
                eye,
                center,
                up
        );
        viewTransform.invert();

        cameraTG.setTransform(viewTransform);
    }

    private void nudge(Vector3d direction) {
        Vector3d translation = new Vector3d();
        Vector3d forward = new Vector3d(direction);
        forward.scaleAdd(CAMERA_SPEED, translation);
        center.add(forward);
        eye.add(forward);

        setView();
    }

    private void roll(double radians) {

        AxisAngle4d rotate = new AxisAngle4d(forward, radians);

        Transform3D rotator = new Transform3D();
        rotator.set(rotate);
        rotator.transform(up);

        setView();
    }



    @Override
    public void initialize() {
        setView();
        WakeupCriterion criterion = new WakeupOnElapsedTime(30);
        wakeupOn(criterion);
    }

    @Override
    public void processStimulus(Iterator<WakeupCriterion> iterator) {
        while (iterator.hasNext()) {
            WakeupCriterion criterion = iterator.next();
            if (criterion instanceof WakeupOnElapsedTime)
            {
                if (wDown) {
                    nudge(forward);
                }
                if (sDown) {
                    Vector3d backwards = new Vector3d(forward);
                    backwards.negate();
                    nudge(backwards);
                }
                if (rDown) {
                    nudge(up);
                }
                if (fDown) {
                    Vector3d down = new Vector3d(up);
                    down.negate();
                    nudge(down);
                }
                if (aDown) {
                    Vector3d right = new Vector3d();
                    right.cross(up, forward);
                    nudge(right);

                }
                if (dDown) {
                    Vector3d left = new Vector3d();
                    left.cross(forward, up);
                    nudge(left);
                }
                if (eDown) {
                    roll(CAMERA_ROTATION_SPEED);
                }
                if (qDown) {
                    roll(-CAMERA_ROTATION_SPEED);
                }
                if (mouseDown && mouseReady) {
                    Vector3d right = new Vector3d();
                    right.cross(forward, up);

                    AxisAngle4d rotate1 = new AxisAngle4d(up, -(dmouseX - mouseX) / (double)scale * MOUSE_SPEED);
                    AxisAngle4d rotate2 = new AxisAngle4d(right, -(dmouseY - mouseY) / (double)scale * MOUSE_SPEED);

                    Transform3D rotator1 = new Transform3D();
                    rotator1.set(rotate1);
                    rotator1.normalize();

                    Transform3D rotator2 = new Transform3D();
                    rotator2.set(rotate2);
                    rotator2.normalize();

                    rotator2.transform(forward);

                    rotator1.transform(up);
                    rotator1.transform(forward);


                    center = new Point3d(eye);
                    center.add(forward);

                    mouseY = dmouseY;
                    mouseX = dmouseX;
                    setView();
                }
            }
        }
        wakeupOn(new WakeupOnElapsedTime(30));
    }


    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
                wDown = true;
                break;
            case KeyEvent.VK_S:
                sDown = true;
                break;
            case KeyEvent.VK_A:
                aDown = true;
                break;
            case KeyEvent.VK_D:
                dDown = true;
                break;
            case KeyEvent.VK_E:
                eDown = true;
                break;
            case KeyEvent.VK_Q:
                qDown = true;
                break;
            case KeyEvent.VK_R:
                rDown = true;
                break;
            case KeyEvent.VK_F:
                fDown = true;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
                wDown = false;
                break;
            case KeyEvent.VK_S:
                sDown = false;
                break;
            case KeyEvent.VK_A:
                aDown = false;
                break;
            case KeyEvent.VK_D:
                dDown = false;
                break;
            case KeyEvent.VK_E:
                eDown = false;
                break;
            case KeyEvent.VK_Q:
                qDown = false;
                break;
            case KeyEvent.VK_R:
                rDown = false;
                break;
            case KeyEvent.VK_F:
                fDown = false;
                break;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        mouseDown = true;
        mouseX = e.getXOnScreen();
        mouseY = e.getYOnScreen();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mouseDown = false;
        mouseReady = false;
        mouseX = e.getXOnScreen();
        mouseY = e.getYOnScreen();
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseReady = true;
        //System.err.println("Beep");
        dmouseX = e.getXOnScreen();
        dmouseY = e.getYOnScreen();


    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}
