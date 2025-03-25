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

    public PawnObject(String teamColor) {
        synchronized (PawnObject.class) { 
            int count = colorCounters.getOrDefault(teamColor, 0);
            colorCounters.put(teamColor, ++count);
            pawnNum = count;               // Generate chess serial number
        }
        colorID = teamColor.toLowerCase();
        objTG.addChild(create_Object());   
    }

@Override  
    protected Node create_Object() { // 消除无用参数的方法版本[^3]
        // 根据材料ID=4实现纹理与几何参数解耦[^4]
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
            generateBaseAppearance());
        base.getShape(Box.TOP).setAppearance(generateTopAppearance());
        return base;
    }
    private String generateUID() {
        return "pawn_" + System.currentTimeMillis() + "_" + this.hashCode();
    }
    private Appearance generatePawnTexture() {
        return MaterialManager.set_Appearance(
            this.colorID + "_texture"); 
    }
    private Appearance generateBaseAppearance() {
        return MaterialManager.set_Appearance(getCurrentMaterialKey());
    }
}
