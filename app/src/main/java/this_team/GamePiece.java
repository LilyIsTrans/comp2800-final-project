package this_team;

import org.jogamp.java3d.*;
import org.jogamp.java3d.loaders.Scene;
import org.jogamp.java3d.loaders.objectfile.ObjectFile;
import org.jogamp.vecmath.*;

import java.io.FileNotFoundException;

public class GamePiece {
    private TransformGroup pieceTG;
    private Transform3D transform;
    private Appearance baseApp;
    private Appearance highlightedApp;

    public GamePiece(float cellSize, Color3f clr) throws FileNotFoundException {

        // Use MaterialManager to create an Appearance with the red material.
        baseApp = MaterialManager.set_Appearance(clr);
        highlightedApp = MaterialManager.highlight_Appearance(clr);

        ObjectFile pawn = new ObjectFile();
        Scene pawnScene = pawn.load("./src/resources/pawn.obj");
        BranchGroup pawnGroup = pawnScene.getSceneGroup();
        Shape3D pawnShape = (Shape3D) pawnGroup.getChild(0);
        pawnShape.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);

        pawnShape.setAppearance(baseApp);
        
        // Create a sphere to represent the game piece.
        //Sphere pieceSphere = new Sphere(cellSize / 3, Sphere.GENERATE_NORMALS | Sphere.ENABLE_PICK_REPORTING, 100, pieceApp);
        
        // Set up the TransformGroup for moving the piece.
        pieceTG = new TransformGroup();
        pieceTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        // pieceTG.addChild(pieceSphere);
        pieceTG.addChild(pawnGroup);

        
        transform = new Transform3D();
        transform.rotX(Math.PI / 2);
        transform.setScale(1.0d / 4);


        pieceTG.setTransform(transform);

    }
    
    public TransformGroup getTransformGroup() {
        return pieceTG;
    }
    
    public void moveTo(float x, float y, float z) {
        Vector3f pos = new Vector3f(x, y, z);
//        Point3f origin = new Point3f(0, 0, 0);
//        Point3f destination = new Point3f(x, y, z);
//        transform.transform(origin);
//        transform.transform(destination);
//        ParabolicInterpolator interp = ParabolicInterpolator.make(new Alpha(1, 100),
//                pieceTG,
//                new Transform3D(),
//                (origin),
//                (destination),
//                1.0f);
        transform.setTranslation(pos);
        pieceTG.setTransform(transform);
        System.out.println("GamePiece moved to: (" + x + ", " + y + ", " + z + ")");
    }

    public void highlight() {
        Shape3D piece = (Shape3D) ((BranchGroup)(pieceTG.getChild(0))).getChild(0);
        piece.setAppearance(highlightedApp);
    }

    public void unhighlight() {
        Shape3D piece = (Shape3D) ((BranchGroup)(pieceTG.getChild(0))).getChild(0);
        piece.setAppearance(baseApp);
    }
}