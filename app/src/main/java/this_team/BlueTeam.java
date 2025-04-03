package this_team;

import java.io.FileNotFoundException;

public class BlueTeam extends PieceLogic {
    public BlueTeam(float gridSize, float cellSize) throws FileNotFoundException {
        super(gridSize, cellSize, "Blue", TeamConfig.Blue.COLOR);
    }

    @Override
    protected int[] getStartPosition() {
        return TeamConfig.Blue.START_POSITION;
    }

    @Override
    protected float[][] getHomePositions() {
        return TeamConfig.Blue.HOME_POSITIONS;
    }

    @Override
    public int[][] getPath() {
        return TeamConfig.Blue.PATH;
    }

    protected int[] getEndPosition() {
        return TeamConfig.Red.END_POSITION;
    }

    public void reset() {
        for (int i = 0; i < 4; i++) {
            placeAtHomePosition(i); // Reuse existing method
        }
    }
}