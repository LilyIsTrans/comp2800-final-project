package this_team;


import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Transform3D;

import java.io.FileNotFoundException;

public class YellowTeam extends PieceLogic {
    public YellowTeam(float gridSize, float cellSize, Transform3D rootTransform, BranchGroup sceneBG) throws FileNotFoundException {
        super(gridSize, cellSize, "Yellow", TeamConfig.Yellow.COLOR, rootTransform, sceneBG);
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

    protected int[] getEndPosition() {
        return TeamConfig.Yellow.END_POSITION;
    }

    @Override
    public void reset() {
      for (int i = 0; i < 4; i++) {
        placeAtHomePosition(i); // Reuse existing method
      }
    }
}