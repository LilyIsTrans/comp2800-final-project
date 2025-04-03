package this_team;

import org.jogamp.java3d.TransformGroup;

public interface Team {
  // Essential methods for GameLogic
  String getTeamName();
  int getCurrentRow(int index);
  int getCurrentCol(int index);
  TransformGroup getTransformGroup();
  
  // Movement-related methods
  boolean isAtHome(int pieceIndex);
  void moveToStart(int pieceIndex);
  void highlightPiece(int pieceIndex);
  void unhighlightPiece(int pieceIndex);
  void movePiece(int index, int row, int col, float zOffset);
  void placeAtHomePosition(int index);
  int[][] getPath();

  // win related methods
  boolean isFinished(int pieceIndex);  // New method
  void setFinished(int pieceIndex);    // New method
  void reset();
  
}