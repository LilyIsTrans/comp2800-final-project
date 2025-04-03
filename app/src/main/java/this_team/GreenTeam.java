package this_team;

import java.io.FileNotFoundException;

public class GreenTeam extends PieceLogic {
    public GreenTeam(float gridSize, float cellSize) throws FileNotFoundException {
        super(gridSize, cellSize, "Green", TeamConfig.Green.COLOR);
    }

    @Override
    protected int[] getStartPosition() {
        return TeamConfig.Green.START_POSITION;
    }

    @Override
    protected float[][] getHomePositions() {
        return TeamConfig.Green.HOME_POSITIONS;
    }

    @Override
    public int[][] getPath() {
        return TeamConfig.Green.PATH;
    }

    protected int[] getEndPosition() {
        return TeamConfig.Green.END_POSITION;
    }

        public void reset() {
        for (int i = 0; i < 4; i++) {
            placeAtHomePosition(i); // Reuse existing method
        }
    }
    
}