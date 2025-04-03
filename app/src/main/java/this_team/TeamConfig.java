package this_team;

import org.jogamp.vecmath.Color3f;

public class TeamConfig {

  public static final class Red {
    // Home positions [row, col, xOffset, yOffset] in cell units
    public static final float[][] HOME_POSITIONS = {
      {1, 11, -0.425f, 0.7f},  // Piece 0
      {1, 13, -0.625f, 0.7f},  // Piece 1
      {3, 11, -0.425f, 0.4f},  // Piece 2
      {3, 13, -0.525f, 0.4f}   // Piece 3
    };

    // Starting position [row, col] without offsets
    public static final int[] START_POSITION = {6, 13};

    // Visual properties
    public static final Color3f COLOR = MaterialManager.Red;
    public static final String NAME = "Red";

    public static final int[][] PATH = {
      {6, 13}, {6, 12}, {6, 11}, {6, 10}, {6, 9},
      {5, 8}, {4, 8}, {3, 8}, {2, 8}, {1, 8},
      {0, 8}, {0, 7}, {0, 6},
      {1, 6}, {2, 6}, {3, 6}, {4, 6}, {5, 6},
      {6, 5}, {6, 4}, {6, 3}, {6, 2}, {6, 1},
      {6, 0}, {7, 0}, {8, 0},
      {8, 1}, {8, 2}, {8, 3}, {8, 4}, {8, 5},
      {9, 6}, {10, 6}, {11, 6}, {12, 6}, {13, 6},
      {14, 6}, {14, 7}, {14, 8},
      {13, 8}, {12, 8}, {11, 8}, {10, 8}, {9, 8},
      {8, 9}, {8, 10}, {8, 11}, {8, 12}, {8, 13},
      {8, 14}, {7, 14}, {7, 13}, {7, 12}, {7, 11}, {7, 10}, {7, 9}, {7, 8}
    };

    public static final int[] END_POSITION = {7, 8};
  }

  public static final class Yellow {
    // Home positions [row, col, xOffset, yOffset] in cell units
    public static final float[][] HOME_POSITIONS = {
      {12, 1, 0.625f, 0.35f},  // Piece 0
      {10, 1, 0.625f, 0.725f}, // Piece 1
      {12, 3, 0.425f, 0.35f},  // Piece 2
      {10, 3, 0.45f, 0.65f}    // Piece 3
    };

    // Starting position [row, col] without offsets
    public static final int[] START_POSITION = {8, 1};

    // Visual properties
    public static final Color3f COLOR = MaterialManager.Yellow;
    public static final String NAME = "Yellow";

    // First 5 path squares for testing
    public static final int[][] PATH = {
      {8, 1}, {8, 2}, {8, 3}, {8, 4}, {8, 5},
      {9, 6}, {10, 6}, {11, 6}, {12, 6}, {13, 6},
      {14, 6}, {14, 7}, {14, 8},
      {13, 8}, {12, 8}, {11, 8}, {10, 8}, {9, 8},
      {8, 9}, {8, 10}, {8, 11}, {8, 12}, {8, 13},
      {8, 14}, {7, 14}, {6, 14},
      {6, 13}, {6, 12}, {6, 11}, {6, 10}, {6, 9},
      {5, 8}, {4, 8}, {3, 8}, {2, 8}, {1, 8},
      {0, 8}, {0, 7}, {0, 6},
      {1, 6}, {2, 6}, {3, 6}, {4, 6}, {5, 6},
      {6, 5}, {6, 4}, {6, 3}, {6, 2}, {6, 1},
      {6, 0}, {7, 0}, {7, 1}, {7, 2}, {7, 3}, {7, 4}, {7, 5}, {7, 6}
    };

    public static final int[] END_POSITION = {7, 6};
  }

  public static final class Blue {
    // Home positions [row, col, xOffset, yOffset] in cell units
    public static final float[][] HOME_POSITIONS = {
      {11, 11, -0.5f, -0.5f}, // Piece 0 (was 12)
      {13, 11, -0.5f, -0.7f}, // Piece 1 (was 12)
      {11, 13, -0.7f, -0.5f}, // Piece 2 (was 14)
      {13, 13, -0.7f, -0.7f}  // Piece 3 (was 14)
    };

    // Starting position [row, col] without offsets
    public static final int[] START_POSITION = {13, 8};

    // Visual properties
    public static final Color3f COLOR = MaterialManager.Blue;
    public static final String NAME = "Blue";

    // Movement path
    public static final int[][] PATH = {
      {13, 8}, {12, 8}, {11, 8}, {10, 8}, {9, 8},
      {8, 9}, {8, 10}, {8, 11}, {8, 12}, {8, 13},
      {8, 14}, {7, 14}, {6, 14},
      {6, 13}, {6, 12}, {6, 11}, {6, 10}, {6, 9},
      {5, 8}, {4, 8}, {3, 8}, {2, 8}, {1, 8},
      {0, 8}, {0, 7}, {0, 6},
      {1, 6}, {2, 6}, {3, 6}, {4, 6}, {5, 6},
      {6, 5}, {6, 4}, {6, 3}, {6, 2}, {6, 1},
      {6, 0}, {7, 0}, {8, 0},
      {8, 1}, {8, 2}, {8, 3}, {8, 4}, {8, 5},
      {9, 6}, {10, 6}, {11, 6}, {12, 6}, {13, 6},
      {14, 6}, {14, 7}, {14, 8},
      {13, 8}, {12, 8}, {11, 8}, {10, 8}, {9, 8},
      {8, 7}, {8, 6}, {7, 6}, {7, 7}, {7, 8}
    };

    public static final int[] END_POSITION = {7, 8};
  }

  public static final class Green {
    // Home positions [row, col, xOffset, yOffset] in cell units
    public static final float[][] HOME_POSITIONS = {
      {1, 1, 0.5f, 0.5f},   // Piece 0
      {1, 3, 0.3f, 0.5f},   // Piece 1
      {3, 1, 0.5f, 0.3f},   // Piece 2
      {3, 3, 0.3f, 0.3f}    // Piece 3
    };

    // Starting position [row, col] without offsets
    public static final int[] START_POSITION = {1, 6};

    // Visual properties
    public static final Color3f COLOR = MaterialManager.Green;
    public static final String NAME = "Green";

    // Movement path
    public static final int[][] PATH = {
      {1, 6}, {2, 6}, {3, 6}, {4, 6}, {5, 6},
      {6, 5}, {6, 4}, {6, 3}, {6, 2}, {6, 1},
      {6, 0}, {7, 0}, {8, 0},
      {8, 1}, {8, 2}, {8, 3}, {8, 4}, {8, 5},
      {9, 6}, {10, 6}, {11, 6}, {12, 6}, {13, 6},
      {14, 6}, {14, 7}, {14, 8},
      {13, 8}, {12, 8}, {11, 8}, {10, 8}, {9, 8},
      {8, 9}, {8, 10}, {8, 11}, {8, 12}, {8, 13},
      {8, 14}, {7, 14}, {6, 14},
      {6, 13}, {6, 12}, {6, 11}, {6, 10}, {6, 9},
      {5, 8}, {4, 8}, {3, 8}, {2, 8}, {1, 8},
      {0, 8}, {0, 7}, {0, 6},
      {1, 6}, {2, 6}, {3, 6}, {4, 6}, {5, 6},
      {6, 7}, {6, 8}, {7, 8}, {7, 7}, {7, 6}
    };

    public static final int[] END_POSITION = {7, 6};
  }
}