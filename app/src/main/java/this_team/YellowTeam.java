package this_team;


import java.io.FileNotFoundException;

public class YellowTeam extends PieceLogic {
    public YellowTeam(float gridSize, float cellSize) throws FileNotFoundException {
        super(gridSize, cellSize, "Yellow", TeamConfig.Yellow.COLOR);
    }

    @Override
    protected int[] getStartPosition() {
        return TeamConfig.Yellow.START_POSITION;
    }

    @Override
    protected float[][] getHomePositions() {
        return TeamConfig.Yellow.HOME_POSITIONS;
    }

    @Override
    public int[][] getPath() {
        return TeamConfig.Yellow.PATH;
    }
}