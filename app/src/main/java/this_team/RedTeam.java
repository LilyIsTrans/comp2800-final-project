
package this_team;

import java.io.FileNotFoundException;

public class RedTeam extends PieceLogic {

    public RedTeam(float gridSize, float cellSize) throws FileNotFoundException {
        super(gridSize, cellSize, "Red", TeamConfig.Red.COLOR);
    }

    @Override
    protected int[] getStartPosition() {
        return TeamConfig.Red.START_POSITION;
    }

    @Override
    protected float[][] getHomePositions() {
        return TeamConfig.Red.HOME_POSITIONS;
    }

    @Override
    public int[][] getPath() {
        return TeamConfig.Red.PATH;
    }

    protected int[] getEndPosition() {
        return TeamConfig.Red.END_POSITION;
    }

    @Override
    public void reset() {
        for (int i = 0; i < 4; i++) {
            placeAtHomePosition(i); // Reuse existing method
        }
    }
}
