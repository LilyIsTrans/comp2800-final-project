package this_team;

import org.jogamp.java3d.TransformGroup;
import org.jogamp.vecmath.Color3f;

import java.io.FileNotFoundException;

public abstract class PieceLogic implements Team {
    protected final GamePiece[] pieces = new GamePiece[4];
    protected final int[] currentRows = new int[4];
    protected final int[] currentCols = new int[4];
    protected final float gridSize;
    protected final float cellSize;
    protected final String teamName;
    protected final Color3f teamColor;
    
    // ===== NEW: Win condition tracking =====
    protected final boolean[] finishedPieces = new boolean[4]; // Tracks which pieces reached the end

    

    public PieceLogic(float gridSize, float cellSize, String teamName, Color3f teamColor) throws FileNotFoundException {
        this.gridSize = gridSize;
        this.cellSize = cellSize;
        this.teamName = teamName;
        this.teamColor = teamColor;
        initializePieces();
    }

    // === Core Team Interface Implementation ===
    @Override
    public boolean isAtHome(int pieceIndex) {
        float[] home = getHomePositions()[pieceIndex];
        return currentRows[pieceIndex] == (int)home[0] && 
               currentCols[pieceIndex] == (int)home[1];
    }

    @Override
    public void moveToStart(int pieceIndex) {
        int[] start = getStartPosition();
        float x = calculateX(start[1]);
        float y = calculateY(start[0]);

        pieces[pieceIndex].moveTo(x, y, 0.1f);
        updatePosition(pieceIndex, start[0], start[1]);
    }

    @Override
    public void highlightPiece(int pieceIndex) {
        pieces[pieceIndex].highlight();
    }

    @Override
    public void unhighlightPiece(int pieceIndex) {
        pieces[pieceIndex].unhighlight();
    }

    @Override
    public void placeAtHomePosition(int index) {
        float[] home = getHomePositions()[index];
        float x = calculateX((int)home[1]) + (home[2] * cellSize);
        float y = calculateY((int)home[0]) + (home[3] * cellSize);
        pieces[index].moveTo(x, y, 0.1f);
        updatePosition(index, (int)home[0], (int)home[1]);
        
        // NEW: Reset finished status when sent home
        finishedPieces[index] = false;
    }

    // === Shared Movement Logic ===
    @Override
    public void movePiece(int index, int newRow, int newCol, float zOffset) {
        if (index >= 0 && index < pieces.length) {
            float x = calculateX(newCol);
            float y = calculateY(newRow);
            pieces[index].moveTo(x, y, zOffset);
            updatePosition(index, newRow, newCol);
        }
    }

    // ===== NEW: Win condition methods (Team interface implementation) =====
    @Override
    public boolean isFinished(int pieceIndex) {
        return pieceIndex >= 0 && pieceIndex < 4 && finishedPieces[pieceIndex];
    }

    @Override
    public void setFinished(int pieceIndex) {
        if (pieceIndex >= 0 && pieceIndex < 4) {
            finishedPieces[pieceIndex] = true;
            System.out.println(teamName + " piece " + (pieceIndex + 1) + " reached home!");
        }
    }

    // === Team Configuration (Abstract) ===
    protected abstract int[] getStartPosition();
    protected abstract float[][] getHomePositions();
    public abstract int[][] getPath();

    // === Common Utilities ===
    private float calculateX(int col) {
        return (col * cellSize) - (gridSize / 2) + (cellSize / 2);
    }

    private float calculateY(int row) {
        return (row * cellSize) - (gridSize / 2) + (cellSize / 2);
    }

    private void updatePosition(int index, int row, int col) {
        currentRows[index] = row;
        currentCols[index] = col;
    }

    // === Required Getters ===
    @Override
    public String getTeamName() { return teamName; }

    @Override
    public int getCurrentRow(int index) { return currentRows[index]; }

    @Override
    public int getCurrentCol(int index) { return currentCols[index]; }

    @Override
    public TransformGroup getTransformGroup() {
        TransformGroup group = new TransformGroup();
        group.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
        group.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
        for (GamePiece piece : pieces) {
            group.addChild(piece.getTransformGroup());
        }
        return group;
    }

    // === Initialization ===
    private void initializePieces() throws FileNotFoundException {
        for (int i = 0; i < 4; i++) {
            pieces[i] = new GamePiece(cellSize, teamColor);
            placeAtHomePosition(i);
            finishedPieces[i] = false; // NEW: Initialize all pieces as not finished
        }
    }

}