package this_team;

import java.util.HashMap;
import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.geometry.*;
import org.jogamp.vecmath.*;
import this_team.MaterialManager;

public class PawnObject extends ObjectManager {
    private static HashMap<String, Integer> colorCounters = new HashMap<>();
    private final String colorID;
    private final int pawnNum;
    
    private TransformGroup moveTG;
    private final String uid;
    private TransformGroup animateTG;

    public PawnObject(String teamColor) {
        synchronized (PawnObject.class) {
            int count = colorCounters.getOrDefault(teamColor, 0);
            colorCounters.put(teamColor, ++count);
            pawnNum = count;                          // Synchronous piece count
        }
        colorID = teamColor.toLowerCase();
        uid = generateUID(); 
        objTG.addChild(create_Object());
    }

    // Core rendering method
    @Override
    protected Node create_Object() {
        // Spherical body (using high-density mesh)
        Sphere pawnCore = new Sphere(
            0.075f,
            Sphere.GENERATE_NORMALS | Sphere.GENERATE_TEXTURE_COORDS,
            128, 
            MaterialManager.set_Appearance(getPawnTextureKey())
        );
        
        // Shadow disc (translucent)
        Cylinder shadowDisc = new Cylinder(
            0.095f, 0.005f,
            Cylinder.GENERATE_NORMALS | Cylinder.GENERATE_TEXTURE_COORDS,
            64, 1, generateShadowAppearance()
        );
        
        // Composite deformation node
        TransformGroup compositeTG = new TransformGroup();
        Transform3D shadowOffset = new Transform3D();
        shadowOffset.setTranslation(new Vector3d(0, -0.075, 0));
        compositeTG.addChild(new TransformGroup(shadowOffset).addChild(shadowDisc));
        compositeTG.addChild(pawnCore);
        
        pawnCore.getShape().setUserData(uid);
        return compositeTG;
    }

    // Animation system
    public void initMoveAnimation(Vector3d[] pathPoints) {
        animateTG = new TransformGroup();
        animateTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        
        Alpha pathAlpha = new Alpha(-1, 5000); // 5 second loop animation
        PositionPathInterpolator pathInterp = new PositionPathInterpolator(
            pathAlpha, animateTG, buildPathControlPoints(pathPoints)
        );
        pathInterp.setSchedulingBounds(new BoundingSphere());
        animateTG.addChild(pathInterp);
    }

    private float[] buildPathControlPoints(Vector3d[] path) {
        float[] knots = new float[path.length * 3];
        for (int i = 0; i < path.length; i++) {
            knots[i*3] = (float)path[i].x;
            knots[i*3+1] = (float)path[i].y;
            knots[i*3+2] = (float)path[i].z;
        }
        return knots;
    }

    private Appearance generateShadowAppearance() {
        Appearance app = new Appearance();
        Material mat = new Material();
        mat.setDiffuseColor(new Color3f(0.2f, 0.2f, 0.2f));
        mat.setTransparency(0.5f);
        app.setMaterial(mat);
        app.setTransparencyAttributes(
            new TransparencyAttributes(TransparencyAttributes.BLENDED, 0.5f)
        );
        return app;
    }

    public void initMoveController(Vector3d startPos) {
        moveTG = new TransformGroup();
        moveTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        Transform3D initT3d = new Transform3D();
        initT3d.setTranslation(startPos);
        moveTG.setTransform(initT3d);
        moveTG.addChild(create_Object());
    }

    @Override
    protected Node create_Object(float l, float h, float b) { 
        Box base = new Box(l, h, b, 
            Primitive.GENERATE_TEXTURE_COORDS | Primitive.GENERATE_NORMALS, 
            MaterialManager.set_Appearance(getCurrentMaterialKey())
        );
        base.getShape(Box.TOP).setAppearance(generateTopAppearance());
        return base;
    }

    // Property access method
    public TransformGroup getMoveController() { 
        return moveTG; 
    }

    private String getPawnTextureKey() {
        return colorID + "_texture"; 
    }

    private String getCurrentMaterialKey() { 
        return colorID + "_base_material";
    }

    private String generateUID() {
        return "pawn_" + colorID + "_" + pawnNum + "_" + System.nanoTime();
    }

    public static boolean isPawn(Node node) {
        return node.getUserData() != null && 
               node.getUserData().toString().startsWith("pawn_");
    }
}
