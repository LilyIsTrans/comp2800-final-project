// create the baord 

package this_team;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.geometry.Box;
import org.jogamp.java3d.utils.image.TextureLoader;
import org.jogamp.vecmath.*;

public class LudoBoard extends ObjectManager {
    private static final float SIZE = 8.0f;
    private final Switch boardSwitch;
    private boolean isVisible = true;

    public LudoBoard() {
        objTG.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
        this.boardSwitch = new Switch();
        boardSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
        createTexturedBoard();
    }

    private void createTexturedBoard() {
        // 1. Create the box appearance with texture
        Appearance boardApp = MaterialManager.set_Appearance(new Color3f(1f, 1f, 1f)); // White base
    
        // 2. Load and apply texture
        TextureLoader loader = new TextureLoader("src/resources/ludo.jpg", null);
        Texture texture = loader.getTexture();
        boardApp.setTexture(texture);
        
        // 3. Configure texture attributes
        TextureAttributes texAttr = new TextureAttributes();
        texAttr.setTextureMode(TextureAttributes.MODULATE); // Blend texture with material color
        boardApp.setTextureAttributes(texAttr);
    
        float sizeAdjustment = 1.02f; // 2% larger to cover grid edges
        float halfSize = (SIZE / 2) * sizeAdjustment; 
        

        Box boardBox = new Box(halfSize, halfSize, 0.3f,
                             Box.GENERATE_TEXTURE_COORDS | 
                             Box.GENERATE_NORMALS, 
                             boardApp);

        // 3. Position the board
        Transform3D pos = new Transform3D();
        pos.setTranslation(new Vector3f(0f, 0f, -0.5f));
        TransformGroup boardTG = new TransformGroup(pos);
        boardTG.addChild(boardBox);

        // 4. Add to switch
        boardSwitch.addChild(boardTG);
        boardSwitch.setWhichChild(isVisible ? Switch.CHILD_ALL : Switch.CHILD_NONE);
        objTG.addChild(boardSwitch);
    }

    public void toggleVisibility() {
        isVisible = !isVisible;
        boardSwitch.setWhichChild(isVisible ? Switch.CHILD_ALL : Switch.CHILD_NONE);
    }
}


