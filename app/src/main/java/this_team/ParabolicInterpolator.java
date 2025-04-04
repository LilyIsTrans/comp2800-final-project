package this_team;

import org.jogamp.java3d.*;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Quat4f;
import org.jogamp.vecmath.Vector3f;

import java.util.Iterator;

public class ParabolicInterpolator extends RotPosScalePathInterpolator {
    private Quat4f rot;
    private static final int steps = 60;
    private float scale;


    private ParabolicInterpolator(Alpha alpha, TransformGroup target, Transform3D axisOfTransform, float[] knots, Point3f[] positions, Quat4f[] rots, float[] scales) {

        super(alpha, target, axisOfTransform, knots, rots, positions, scales);
        rot = rots[0];
        scale = scales[0];
    }

    public static ParabolicInterpolator make(Alpha alpha, TransformGroup target, Transform3D axisOfTransform, Point3f start, Point3f end, float height, Quat4f rot, float scale) {
        float[] knots = new float[steps];
        Point3f[] positions = new Point3f[steps];
        Point3f middle = new Point3f(start);
        middle.add(end);
        middle.scale(0.5f);
        middle.add(new Vector3f(0, 0, 1));

        for (int i = 0; i < steps; ++i) {
            knots[i] = (float)i / (steps - 1);
            positions[i] = f(knots[i], start, end, middle);
        }

        Quat4f[] rots = new Quat4f[knots.length];
        float[] scales = new float[knots.length];
        for (int i = 0; i < knots.length; ++i) {
            rots[i] = new Quat4f(rot);
            scales[i] = scale;
        }


        return new ParabolicInterpolator(alpha, target, axisOfTransform, knots, positions, rots, scales);
    }

    public void setStartEnd(Alpha alpha, Point3f start, Point3f end) {
        float[] knots = new float[steps];
        Point3f[] positions = new Point3f[steps];
        Point3f middle = new Point3f(start);
        middle.add(end);
        middle.scale(0.5f);
        middle.add(new Vector3f(0, 0, 1));

        for (int i = 0; i < steps; ++i) {
            knots[i] = (float)i / (steps - 1);
            positions[i] = f(knots[i], start, end, middle);
        }

        Quat4f[] rots = new Quat4f[knots.length];
        float[] scales = new float[knots.length];
        for (int i = 0; i < knots.length; ++i) {
            rots[i] = new Quat4f(rot);
            scales[i] = scale;
        }
        this.setPathArrays(knots, rots, positions, scales);
        this.setAlpha(alpha);



    }

    private static Point3f f(float t, Point3f start, Point3f end, Point3f middle) {
        Point3f A = new Point3f(start);
        Point3f B = new Point3f(middle);
        Point3f C = new Point3f(end);

        A.scale(1-3*t+2*t*t);
        B.scale(4*t-4*t*t);
        C.scale(2*t*t-t);

        A.add(B);
        A.add(C);

        return A;

    }

    @Override
    public void processStimulus(Iterator<WakeupCriterion> criteria) {
        super.processStimulus(criteria);
        if (this.getAlpha().finished()) {
            System.err.println(this.getAlpha().getLoopCount());
            Point3f end = new Point3f();
            this.getPosition(steps - 1, end);
            this.setStartEnd(new Alpha(-1, 1000), end, end);

            this.wakeupOn(this.defaultWakeupCriterion);
        }

    }
}
