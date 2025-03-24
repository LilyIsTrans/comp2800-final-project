package this_team;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.java3d.utils.geometry.Primitive;
import org.jogamp.vecmath.*;

public class GamePiece extends ObjectManager {
    private Coordinates position; // Current position of the piece
    private final Board board;    // Reference to the board

    // Constructor to initialize the game piece
    public GamePiece(Board board, double startX, double startY) {
        this.board = board;
        this.position = new Coordinates(startX, startY); // Set initial position
        createGamePieceGeometry(); // Create the 3D geometry for the game piece
    }

    // Create the 3D geometry for the game piece
    private void createGamePieceGeometry() {
        // Create a sphere to represent the game piece
        Sphere pieceSphere = new Sphere(0.03f, Primitive.GENERATE_NORMALS, 50, createAppearance());

        // Add the sphere to the TransformGroup (inherited from ObjectManager)
        objTG.addChild(pieceSphere);
    }

    // Create an appearance for the game piece
    private Appearance createAppearance() {
        Appearance appearance = new Appearance();
        Material material = new Material();
        material.setDiffuseColor(new Color3f(1.0f, 0.0f, 0.0f)); // Red color
        appearance.setMaterial(material);
        return appearance;
    }

    // Move the game piece to a new position
    public void moveTo(double newX, double newY) {
        // Update the position
        this.position = new Coordinates(newX, newY);

        // Update the position in the 3D scene
        Transform3D transform = new Transform3D();
        transform.setTranslation(new Vector3d(newX, newY, 0.0)); // Ensure Z = 0 to align with the board
        objTG.setTransform(transform);
    }

    // Get the current position of the game piece
    public Coordinates getPosition() {
        return position;
    }
}