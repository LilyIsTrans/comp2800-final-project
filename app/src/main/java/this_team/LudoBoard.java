package this_team;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.geometry.Box;
import org.jogamp.java3d.utils.image.TextureLoader;
import org.jogamp.vecmath.*;

public class LudoBoard extends ObjectManager {
  private static final float SIZE = 8.0f;
  private final Switch boardSwitch;
  private boolean isVisible = true;

  private static int obj_Num = 130;
  private static ObjectManager[] objects = new ObjectManager[obj_Num];

  public LudoBoard() {
    objTG.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
    this.boardSwitch = new Switch();
    boardSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
    createTexturedBoard();
  }

  private void createTexturedBoard() {
    Appearance boardApp = MaterialManager.set_Appearance(new Color3f(1f, 1f, 1f));
    TextureLoader loader = new TextureLoader("./src/resources/ludo.jpg", null);
    Texture texture = loader.getTexture();
    boardApp.setTexture(texture);
    
    TextureAttributes texAttr = new TextureAttributes();
    texAttr.setTextureMode(TextureAttributes.MODULATE);
    boardApp.setTextureAttributes(texAttr);

    float sizeAdjustment = 1.02f;
    float halfSize = (SIZE / 2) * sizeAdjustment;
    
    Box boardBox = new Box(halfSize, halfSize, 0.3f,
                         Box.GENERATE_TEXTURE_COORDS | 
                         Box.GENERATE_NORMALS, 
                         boardApp);

    Transform3D pos = new Transform3D();
    pos.setTranslation(new Vector3f(0f, 0f, -0.5f));
    TransformGroup boardTG = new TransformGroup(pos);
    boardTG.addChild(boardBox);

    boardSwitch.addChild(boardTG);
    boardSwitch.setWhichChild(isVisible ? Switch.CHILD_ALL : Switch.CHILD_NONE);
    objTG.addChild(boardSwitch);
  }

  public void toggleVisibility() {
    isVisible = !isVisible;
    boardSwitch.setWhichChild(isVisible ? Switch.CHILD_ALL : Switch.CHILD_NONE);
  }

  public static TransformGroup create_board(Transform3D board_Transform) {
    TransformGroup board = new TransformGroup();
    objects[0] = new RectangleBox();
    
    // [Previous tile creation code remains exactly the same, just updating paths below]
    
    // Color tiles (54-71)
    // Red color tiles (54-59)
    double[][] redColorTiles = {
      {-0.787f, 0.05f, -0.13f}, {-0.787f, 0.05f, 0.005f}, {-0.657f, 0.05f, 0.005f},
      {-0.525f, 0.05f, 0.005f}, {-0.395f, 0.05f, 0.005f}, {-0.265f, 0.05f, 0.005f}
    };
    for (int i = 0; i < redColorTiles.length; i++) {
      objects[i+54] = new ColorTile(new Vector3d(redColorTiles[i][0], redColorTiles[i][1], redColorTiles[i][2]), 0.055f, 0.02f, "./src/resources/red2.jpg");
      objects[0].add_Child(objects[i+54].position_Object());
    }
    
    // Blue color tiles (48-53)
    double[][] blueColorTiles = {
      {-0.135f, 0.05f, 0.792f}, {-0.005f, 0.05f, 0.792f}, {-0.005f, 0.05f, 0.657f},
      {-0.005f, 0.05f, 0.527f}, {-0.005f, 0.05f, 0.397f}, {-0.005f, 0.05f, 0.267f}
    };
    for (int i = 0; i < blueColorTiles.length; i++) {
      objects[i+48] = new ColorTile(new Vector3d(blueColorTiles[i][0], blueColorTiles[i][1], blueColorTiles[i][2]), 0.055f, 0.02f, "./src/resources/nightsky.jpg");
      objects[0].add_Child(objects[i+48].position_Object());
    }
    
    // Green color tiles (60-65)
    double[][] greenColorTiles = {
      {0.13f, 0.05f, -0.788f}, {-0.005f, 0.05f, -0.788f}, {-0.005f, 0.05f, -0.654f},
      {-0.005f, 0.05f, -0.524f}, {-0.005f, 0.05f, -0.394f}, {-0.005f, 0.05f, -0.264f}
    };
    for (int i = 0; i < greenColorTiles.length; i++) {
      objects[i+60] = new ColorTile(new Vector3d(greenColorTiles[i][0], greenColorTiles[i][1], greenColorTiles[i][2]), 0.055f, 0.02f, "./src/resources/green.jpg");
      objects[0].add_Child(objects[i+60].position_Object());
    }
    
    // Yellow color tiles (66-71)
    double[][] yellowColorTiles = {
      {0.783f, 0.05f, 0.135f}, {0.783f, 0.05f, 0.005f}, {0.652f, 0.05f, 0.005f},
      {0.521f, 0.05f, 0.005f}, {0.391f, 0.05f, 0.005f}, {0.261f, 0.05f, 0.005f}
    };
    for (int i = 0; i < yellowColorTiles.length; i++) {
      objects[i+66] = new ColorTile(new Vector3d(yellowColorTiles[i][0], yellowColorTiles[i][1], yellowColorTiles[i][2]), 0.055f, 0.02f, "./src/resources/yellow.jpg");
      objects[0].add_Child(objects[i+66].position_Object());
    }
    
    // Home bases
    // Yellow home base (72-79)
    objects[72] = new ColorTile(new Vector3d(0.47f, 0.05f, 0.48f), 0.075f, 0.05f, "./src/resources/yellow.jpg");
    objects[0].add_Child(objects[72].position_Object());
    objects[73] = new ColorTile(new Vector3d(0.705f, 0.05f, 0.48f), 0.075f, 0.05f, "./src/resources/yellow.jpg");
    objects[0].add_Child(objects[73].position_Object());
    objects[74] = new ColorTile(new Vector3d(0.47f, 0.05f, 0.695f), 0.075f, 0.05f, "./src/resources/yellow.jpg");
    objects[0].add_Child(objects[74].position_Object());
    objects[75] = new ColorTile(new Vector3d(0.705f, 0.05f, 0.695f), 0.075f, 0.05f, "./src/resources/yellow.jpg");
    objects[0].add_Child(objects[75].position_Object());
    objects[76] = new ColorTile(new Vector3d(0.26f, 0.05f, 0.59f), "./src/resources/yellow.jpg", 1, 0.75f);
    objects[0].add_Child(objects[76].position_Object());
    objects[77] = new ColorTile(new Vector3d(0.92f, 0.05f, 0.59f), "./src/resources/yellow.jpg", 1, 0.75f);
    objects[0].add_Child(objects[77].position_Object());
    objects[78] = new ColorTile(new Vector3d(0.515f, 0.05f, 0.265f), "./src/resources/yellow.jpg", 2, 0.42f);
    objects[0].add_Child(objects[78].position_Object());
    objects[79] = new ColorTile(new Vector3d(0.585f, 0.05f, 0.92f), "./src/resources/yellow.jpg", 2, 0.56f);
    objects[0].add_Child(objects[79].position_Object());
    
    // Blue home base (80-87)
    objects[80] = new ColorTile(new Vector3d(-0.7135f, 0.05f, 0.695f), 0.075f, 0.05f, "./src/resources/nightsky.jpg");
    objects[0].add_Child(objects[80].position_Object());
    objects[81] = new ColorTile(new Vector3d(-0.48f, 0.05f, 0.695f), 0.075f, 0.05f, "./src/resources/nightsky.jpg");
    objects[0].add_Child(objects[81].position_Object());
    objects[82] = new ColorTile(new Vector3d(-0.7135f, 0.05f, 0.48f), 0.075f, 0.05f, "./src/resources/nightsky.jpg");
    objects[0].add_Child(objects[82].position_Object());
    objects[83] = new ColorTile(new Vector3d(-0.48f, 0.05f, 0.48f), 0.075f, 0.05f, "./src/resources/nightsky.jpg");
    objects[0].add_Child(objects[83].position_Object());
    objects[84] = new ColorTile(new Vector3d(-0.265f, 0.05f, 0.525f), "./src/resources/nightsky.jpg", 1, 0.42f);
    objects[0].add_Child(objects[84].position_Object());
    objects[85] = new ColorTile(new Vector3d(-0.92, 0.05f, 0.59f), "./src/resources/nightsky.jpg", 1, 0.56f);
    objects[0].add_Child(objects[85].position_Object());
    objects[86] = new ColorTile(new Vector3d(-0.59f, 0.05f, 0.265f), "./src/resources/nightsky.jpg", 2, 0.75f);
    objects[0].add_Child(objects[86].position_Object());
    objects[87] = new ColorTile(new Vector3d(-0.59f, 0.05f, 0.92f), "./src/resources/nightsky.jpg", 2, 0.75f);
    objects[0].add_Child(objects[87].position_Object());
    
    // Red home base (88-95)
    objects[88] = new ColorTile(new Vector3d(-0.48f, 0.05f, -0.482f), 0.075f, 0.05f, "./src/resources/red2.jpg");
    objects[0].add_Child(objects[88].position_Object());
    objects[89] = new ColorTile(new Vector3d(-0.7135f, 0.05f, -0.482f), 0.075f, 0.05f, "./src/resources/red2.jpg");
    objects[0].add_Child(objects[89].position_Object());
    objects[90] = new ColorTile(new Vector3d(-0.7135f, 0.05f, -0.698f), 0.075f, 0.05f, "./src/resources/red2.jpg");
    objects[0].add_Child(objects[90].position_Object());
    objects[91] = new ColorTile(new Vector3d(-0.48f, 0.05f, -0.698f), 0.075f, 0.05f, "./src/resources/red2.jpg");
    objects[0].add_Child(objects[91].position_Object());
    objects[92] = new ColorTile(new Vector3d(-0.265f, 0.05f, -0.59f), "./src/resources/red2.jpg", 1, 0.75f);
    objects[0].add_Child(objects[92].position_Object());
    objects[93] = new ColorTile(new Vector3d(-0.92f, 0.05f, -0.59f), "./src/resources/red2.jpg", 1, 0.75f);
    objects[0].add_Child(objects[93].position_Object());
    objects[94] = new ColorTile(new Vector3d(-0.59f, 0.05f, -0.92f), "./src/resources/red2.jpg", 2, 0.56f);
    objects[0].add_Child(objects[94].position_Object());
    objects[95] = new ColorTile(new Vector3d(-0.515f, 0.05f, -0.265f), "./src/resources/red2.jpg", 2, 0.42f);
    objects[0].add_Child(objects[95].position_Object());
    
    // Green home base (96-103)
    objects[96] = new ColorTile(new Vector3d(0.47f, 0.05f, -0.698f), 0.075f, 0.05f, "./src/resources/green.jpg");
    objects[0].add_Child(objects[96].position_Object());
    objects[97] = new ColorTile(new Vector3d(0.705f, 0.05f, -0.698f), 0.075f, 0.05f, "./src/resources/green.jpg");
    objects[0].add_Child(objects[97].position_Object());
    objects[98] = new ColorTile(new Vector3d(0.705f, 0.05f, -0.482f), 0.075f, 0.05f, "./src/resources/green.jpg");
    objects[0].add_Child(objects[98].position_Object());
    objects[99] = new ColorTile(new Vector3d(0.47f, 0.05f, -0.482f), 0.075f, 0.05f, "./src/resources/green.jpg");
    objects[0].add_Child(objects[99].position_Object());
    objects[100] = new ColorTile(new Vector3d(0.92f, 0.05f, -0.59f), "./src/resources/green.jpg", 1, 0.56f);
    objects[0].add_Child(objects[100].position_Object());
    objects[101] = new ColorTile(new Vector3d(0.26f, 0.05f, -0.515f), "./src/resources/green.jpg", 1, 0.42f);
    objects[0].add_Child(objects[101].position_Object());
    objects[102] = new ColorTile(new Vector3d(0.59f, 0.05f, -0.26f), "./src/resources/green.jpg", 2, 0.75f);
    objects[0].add_Child(objects[102].position_Object());
    objects[103] = new ColorTile(new Vector3d(0.59f, 0.05f, -0.92f), "./src/resources/green.jpg", 2, 0.75f);
    objects[0].add_Child(objects[103].position_Object());
    
        // Middle tile (104)
        objects[104] = new RectangleBox(new Vector3d(0.0f, 0.05f, 0f));
        objects[0].add_Child(objects[104].position_Object());
        
        // Set up board transform
        board_Transform.setScale(4.0d);
        board_Transform.setTranslation(new Vector3d(0, 0, -0.1));
        board_Transform.setRotation(new AxisAngle4d(new Vector3d(0, 0, 1), Math.PI));
        Transform3D temp = new Transform3D();
        temp.rotX(Math.PI / 2);
        board_Transform.mul(temp);
        board.addChild(objects[0].position_Object());
        board.setTransform(board_Transform);
        
        return board;
        
  }
}






