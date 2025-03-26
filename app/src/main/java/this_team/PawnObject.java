package this_team;

import java.util.HashMap;
import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.geometry.Primitive;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.vecmath.*;
import this_team.MaterialManager;

public class PawnObject extends ObjectManager {
    private static HashMap<String, Integer> colorCounters = new HashMap<>(); 
    private final String colorID;
    private final int pawnNum;

    private TransformGroup moveTG;
    private final String uid;

    public PawnObject(String teamColor) {
        synchronized (PawnObject.class) { 
            int count = colorCounters.getOrDefault(teamColor, 0);
            colorCounters.put(teamColor, ++count);
            pawnNum = count;               // Generate chess serial number
        }
        colorID = teamColor.toLowerCase();
        uid = "pawn_" + colorID + "_" + pawnNum;
        objTG.addChild(create_Object());   
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
    protected Node create_Object() { 

        Appearance app = MaterialManager.set_Appearance(generatePawnTexture());
        Sphere pawn = new Sphere(0.075f, 
            Sphere.GENERATE_NORMALS | Sphere.GENERATE_TEXTURE_COORDS, 
            64, app);
        pawn.getShape().setUserData(this.generateUID());
        return pawn;
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
    
    public TransformGroup getMoveController() { 
        return moveTG; 
    }
    private Appearance generatePawnTexture() {
        return MaterialManager.set_Appearance(
            colorID + "_texture");                    
    }

    private String getCurrentMaterialKey() { 
        return colorID + "_base_material";
    }


    public static boolean isPawn(Node node) {
        return node.getUserData() != null && 
               node.getUserData().toString().startsWith("pawn_");
    }

}
