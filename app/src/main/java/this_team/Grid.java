package this_team;

import org.jogamp.java3d.*;
import org.jogamp.vecmath.*;

public class Grid extends ObjectManager {
    private static final int ROWS = 15;
    private static final int COLS = 15;
    private static final float SIZE = 8.0f;
    private static final float CELL_SIZE = SIZE / COLS;
    private Switch gridSwitch;
    private boolean isVisible = true;

    public Grid() {
        objTG.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
        createGrid();
    }

    private void createGrid() {
        Appearance lineApp = new Appearance();
        ColoringAttributes ca = new ColoringAttributes(new Color3f(0f, 1f, 1f), ColoringAttributes.FASTEST);
        lineApp.setColoringAttributes(ca);
        
        LineArray lines = new LineArray((ROWS + COLS + 2) * 2, LineArray.COORDINATES);
        float halfSize = SIZE / 2;
        int index = 0;
        
        // Draw horizontal grid lines
        for (int i = 0; i <= ROWS; i++) {
            float y = (i * CELL_SIZE) - halfSize;
            lines.setCoordinate(index++, new Point3f(-halfSize, y, 0.01f));
            lines.setCoordinate(index++, new Point3f(halfSize, y, 0.01f));
        }
        
        // Draw vertical grid lines
        for (int j = 0; j <= COLS; j++) {
            float x = (j * CELL_SIZE) - halfSize;
            lines.setCoordinate(index++, new Point3f(x, -halfSize, 0.01f));
            lines.setCoordinate(index++, new Point3f(x, halfSize, 0.01f));
        }
        
        Shape3D gridShape = new Shape3D(lines, lineApp);
        
        // Add Switch node for visibility control
        gridSwitch = new Switch();
        gridSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
        gridSwitch.addChild(gridShape);
        gridSwitch.setWhichChild(Switch.CHILD_ALL); // Initially visible
        
        objTG.addChild(gridSwitch);
    }

    public void toggleVisibility() {
        isVisible = !isVisible;
        gridSwitch.setWhichChild(isVisible ? Switch.CHILD_ALL : Switch.CHILD_NONE);
    }

    public float getCellSize() {
        return CELL_SIZE;
    }
    
    public float getSize() {
        return SIZE;
    }
}