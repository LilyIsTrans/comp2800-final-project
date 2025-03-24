package this_team;

import org.jogamp.java3d.*;
import org.jogamp.vecmath.*;

public class Board extends ObjectManager {
    private final double width;
    private final double height;
    private final int rows;
    private final int cols;
    private final Coordinates[][] grid; // Use your Coordinates class

    // Constructor to initialize board dimensions and grid resolution
    public Board(double width, double height, int rows, int cols) {
        this.width = width;
        this.height = height;
        this.rows = rows;
        this.cols = cols;
        this.grid = new Coordinates[rows][cols]; // Initialize the grid with Coordinates

        initializeGrid();
        createBoardGeometry(); // Create the 3D geometry for the board
    }

    // Map board dimensions into a grid with each cell's center coordinate
    private void initializeGrid() {
        double cellWidth = width / cols;
        double cellHeight = height / rows;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // Calculate center coordinate of the cell
                double x = (j * cellWidth) + (cellWidth / 2);
                double y = (i * cellHeight) + (cellHeight / 2);
                grid[i][j] = new Coordinates(x, y); // Store the center coordinate
            }
        }
    }

    // Create the 3D geometry for the board
    private void createBoardGeometry() {
        // Create a Shape3D object to represent the board
        Shape3D boardShape = new Shape3D();

        // Create a geometry for the board (e.g., a grid of lines)
        Geometry geometry = createGridGeometry();

        // Create an appearance for the board
        Appearance appearance = new Appearance();
        appearance.setMaterial(new Material());

        // Set the geometry and appearance for the Shape3D
        boardShape.setGeometry(geometry);
        boardShape.setAppearance(appearance);

        // Position the grid at the same height as the box and texture
        Transform3D transform = new Transform3D();
        transform.setTranslation(new Vector3d(-0.75, -0.5, 1.0)); // Align with the box
        TransformGroup gridTG = new TransformGroup(transform);
        gridTG.addChild(boardShape);

        // Add the grid to the TransformGroup (inherited from ObjectManager)
        objTG.addChild(gridTG);
    }

    // Create a grid of lines to represent the board
    private Geometry createGridGeometry() {
        int numLines = (rows + cols) * 2;
        LineArray lines = new LineArray(numLines * 2, LineArray.COORDINATES);

        double cellWidth = width / cols;
        double cellHeight = height / rows;

        int index = 0;

        // Draw horizontal lines
        for (int i = 0; i <= rows; i++) {
            double y = i * cellHeight;
            lines.setCoordinate(index++, new Point3d(0, y, 0));
            lines.setCoordinate(index++, new Point3d(width, y, 0));
        }

        // Draw vertical lines
        for (int j = 0; j <= cols; j++) {
            double x = j * cellWidth;
            lines.setCoordinate(index++, new Point3d(x, 0, 0));
            lines.setCoordinate(index++, new Point3d(x, height, 0));
        }

        return lines;
    }

    // Get the center coordinate of a specific grid cell
    public Coordinates getCoordinateAt(int row, int col) {
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            return grid[row][col];
        }
        throw new IndexOutOfBoundsException("Invalid grid position: row " + row + ", col " + col);
    }

    // Add a game piece to the board
    public void addGamePiece(GamePiece piece) {
        objTG.addChild(piece.position_Object());
    }

    // Optional getters for board properties
    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }
}