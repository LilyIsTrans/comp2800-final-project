package this_team;

import org.jogamp.java3d.Alpha;
import org.jogamp.java3d.PositionPathInterpolator;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;

public class ParabolicInterpolator extends PositionPathInterpolator {
    private ParabolicInterpolator(Alpha alpha, TransformGroup target, Transform3D axisOfTransform, float[] knots, Point3f[] positions) {
        super(alpha, target, axisOfTransform, knots, positions);
    }

    public static ParabolicInterpolator make(Alpha alpha, TransformGroup target, Transform3D axisOfTransform, Point3f start, Point3f end, float height) {
        final int steps = 60;
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



        return new ParabolicInterpolator(alpha, target, axisOfTransform, knots, positions);
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
}
