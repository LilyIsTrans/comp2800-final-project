package this_team;

import java.util.HashMap;
import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.vecmath.*;
import LudoProject.MaterialManager;

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
    protected Node create_Object() {      // Implement the parent abstract method
        Appearance pawnApp = MaterialManager.set_Appearance(colorID); // Dynamic material acquisition
        Sphere pawnGeom = new Sphere(0.08f, 
            Sphere.GENERATE_NORMALS | Sphere.GENERATE_TEXTURE_COORDS, 
            64, pawnApp);
        
        Shape3D pawnShape = pawnGeom.getShape();
        pawnShape.setUserData(generateUID());        // Sets an object unique identifier
        pawnShape.setCapability(Shape3D.ALLOW_PICKABLE_READ);// Enable the pick function
        
        return pawnShape;
    }

    private String generateUID() {
        return "pawn_" + colorID + "_" + pawnNum;    
    }
}
