package this_team;

import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Transform3D;

import java.io.FileNotFoundException;

public class GreenTeam extends PieceLogic {
    public GreenTeam(float gridSize, float cellSize, Transform3D rootTransform, BranchGroup sceneBG) throws FileNotFoundException {
        super(gridSize, cellSize, "Green", TeamConfig.Green.COLOR, rootTransform, sceneBG);
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