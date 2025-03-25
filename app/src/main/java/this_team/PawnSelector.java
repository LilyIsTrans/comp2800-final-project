package this_team;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.picking.PickCanvas;
import org.jogamp.java3d.utils.picking.PickResult;
import org.jogamp.java3d.utils.picking.PickTool;
import org.jogamp.vecmath.Point3d;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class PawnSelector implements MouseListener {
    private Canvas3D canvas3D;
    private BranchGroup sceneBG;
    private PickCanvas pickCanvas;
    private TransformGroup selectedPawnTG;
    private Appearance highlightApp;     

    public PawnSelector(Canvas3D canvas, BranchGroup sceneRoot) {
        this.canvas3D = canvas;
        this.sceneBG = sceneRoot;
        initPickingSystem();
        createHighlightMaterial();
    }

    private void initPickingSystem() {
        pickCanvas = new PickCanvas(canvas3D, sceneBG);
        pickCanvas.setMode(PickTool.GEOMETRY_INTERSECT_INFO);
        pickCanvas.setTolerance(4.0f);
        PickTool.setCapabilities(sceneBG, PickTool.BOUNDS | PickTool.GEOMETRY);
    }

    private void createHighlightMaterial() {
        highlightApp = new Appearance();
        Material material = new Material();
        material.setEmissiveColor(1.0f, 0.0f, 0.0f); 
        material.setLightingEnable(true);
        highlightApp.setMaterial(material);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Point3d mousePos = new Point3d(e.getX(), e.getY(), 0);
        pickCanvas.setShapeLocation(mousePos);

        PickResult[] results = pickCanvas.pickAllSorted();
        for (PickResult result : results) {
            Node node = result.getNode(PickResult.PRIMITIVE);
            if (node != null && isPawn(node)) {
                handlePawnSelection(node);
                break; 
            }
        }
    }

    private boolean isPawn(Node node) {
        return node.getUserData() != null && 
               node.getUserData().toString().startsWith("pawn_");
    }

    private void handlePawnSelection(Node pawnNode) {

        if (selectedPawnTG != null) {
            selectedPawnTG.getChild(0).setAppearance(getDefaultPawnAppearance());
        }

        TransformGroup currentTG = (TransformGroup) pawnNode.getParent();
        currentTG.getChild(0).setAppearance(highlightApp);
        selectedPawnTG = currentTG;

        System.out.println("Selected Pawn: " + pawnNode.getUserData());
    }

    private Appearance getDefaultPawnAppearance() {
        return MaterialManager.set_Appearance(Color.GRAY); 
    }

    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}
