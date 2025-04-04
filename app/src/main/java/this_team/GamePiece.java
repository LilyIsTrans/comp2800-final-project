package this_team;

import com.jogamp.opengl.math.Matrix4f;
import com.jogamp.opengl.math.Vec3f;
import org.jogamp.java3d.*;
import org.jogamp.java3d.loaders.Scene;
import org.jogamp.java3d.loaders.objectfile.ObjectFile;
import org.jogamp.vecmath.*;

import java.io.FileNotFoundException;
import javax.swing.Timer;

public class GamePiece  {
    private TransformGroup subTG;
    private TransformGroup pieceTG;
    private Transform3D pieceTransform;
    private Transform3D subTransform;
    private Appearance baseApp;
    private Appearance highlightedApp;
    private Transform3D rootTransform;
    private BranchGroup sceneBG;
    private ParabolicInterpolator interp;
    private Point3f oldPos = new Point3f();

    public GamePiece(float cellSize, Color3f clr, Transform3D rootTransform, BranchGroup sceneBG) throws FileNotFoundException {

        this.rootTransform = rootTransform;
        // Use MaterialManager to create an Appearance with the red material.
        baseApp = MaterialManager.set_Appearance(clr);
        highlightedApp = MaterialManager.highlight_Appearance(clr);
        this.sceneBG = sceneBG;

        ObjectFile pawn = new ObjectFile();
        Scene pawnScene = pawn.load("./src/resources/pawn.obj");
        BranchGroup pawnGroup = pawnScene.getSceneGroup();
        Shape3D pawnShape = (Shape3D) pawnGroup.getChild(0);
        pawnShape.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);

        pawnShape.setAppearance(baseApp);

        pieceTG = new TransformGroup();
        pieceTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        // pieceTG.addChild(pieceSphere);

        subTG = new TransformGroup();
        subTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        subTG.addChild(pawnGroup);

        pieceTG.addChild(subTG);


        pieceTransform = new Transform3D();
        pieceTransform.rotX(Math.PI / 2);
        Transform3D temp = new Transform3D();
        temp.rotZ(Math.PI / 2);
        pieceTransform.mul(temp);
        temp.rotX(Math.PI / 2);
        pieceTransform.mul(temp);

        subTransform = new Transform3D();

        subTG.setTransform(subTransform);

        pieceTG.setTransform(pieceTransform);

        Quat4f hope = new Quat4f();
        AxisAngle4f beans = new AxisAngle4f(new Vector3f(1, 0, 0), (float) (-Math.PI / 2));
        hope.set(beans);



        interp = ParabolicInterpolator.make(new Alpha(), subTG, pieceTransform, new Point3f(), new Point3f(), -1.0f, hope, 0.25f);
        interp.setSchedulingBounds(new BoundingSphere(new Point3d(), Double.POSITIVE_INFINITY));
        interp.setEnable(true);
        sceneBG.addChild(interp);
    }
    
    public TransformGroup getTransformGroup() {
        return pieceTG;
    }
    
    public void moveTo(float x, float y, float z) {
        //currentTransform.get(oldPos);
        interp.setEnable(false);
        interp.setStartEnd(new Alpha(1, 1000), oldPos, new Point3f(x, y, z));
        oldPos = new Point3f(x, y, z);
        interp.setEnable(true);
//        Timer beep = new Timer(2000, e -> this.interp.setStartEnd(new Alpha(-1, 1000), this.oldPos, this.oldPos));
//        beep.setRepeats(false);
//        beep.start();





        //Vector3f pos = new Vector3f(x, y, z);
        //transform.setTranslation(pos);
        //pieceTG.setTransform(transform);
        System.out.println("GamePiece moved to: (" + x + ", " + y + ", " + z + ")");

    }

    public void highlight() {
        Shape3D piece = (Shape3D) ((BranchGroup) ((TransformGroup)(pieceTG.getChild(0))).getChild(0)).getChild(0);
        piece.setAppearance(highlightedApp);
    }

    public void unhighlight() {
        Shape3D piece = (Shape3D) ((BranchGroup) ((TransformGroup)(pieceTG.getChild(0))).getChild(0)).getChild(0);
        piece.setAppearance(baseApp);
    }


}