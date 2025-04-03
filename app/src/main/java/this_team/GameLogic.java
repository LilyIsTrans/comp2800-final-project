package this_team;

import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameLogic {
  // Constants and Fields
  private final Team[] teams;
  private int currentTeamIndex = 0;
  private final Random random = new Random();
  private int currentDiceValue = 0;
  private boolean hasRolledSix = false;
  private TurnState turnState = TurnState.NEEDS_ROLL;

  private enum TurnState {
    NEEDS_ROLL,
    NEEDS_MOVE,
    NO_MOVES,
    TURN_COMPLETE,
    GAME_OVER
  }

  // ==================== INITIALIZATION ====================
  public GameLogic(Team... teams) {
    if (teams == null || teams.length == 0) {
      throw new IllegalArgumentException("At least one team required");
    }
    this.teams = teams;
  }

  // ==================== CORE GAME FLOW ====================
  public void handleTurn() {
    switch (turnState) {
      case NEEDS_ROLL:
        rollDice();

        if (!canMakeAnyMove()) {
            if (winCondition()){
                turnState = TurnState.GAME_OVER;
            }
          turnState = TurnState.NO_MOVES;
          System.out.println("No possible moves - press ENTER to continue");
        } else {
          turnState = TurnState.NEEDS_MOVE;
        }
        break;
      case NO_MOVES:
        break;
      case NEEDS_MOVE:
        break;
      case TURN_COMPLETE:
        endTurn();
        break;
      case GAME_OVER:
        break;
    }
  }

  public boolean winCondition(){
    Team currentTeam = getCurrentTeam();
    for (int i = 0; i < 4; i++) {
      if (!currentTeam.isFinished(i)) {
        return false;
      }
    }
    return true;
  }

  public int rollDice() {
    if (turnState != TurnState.NEEDS_ROLL) {
      System.out.println("Cannot re-roll! Finish your move first.");
      return -1;
    }

    // Simulate a dice roll, slightly biased towards 6
    if (random.nextDouble() < 0.20) {  // 20% chance to roll a 6
      currentDiceValue = 6;
    } else {
      currentDiceValue = random.nextInt(5) + 1;  // Rolls 1-5
    }
    hasRolledSix = (currentDiceValue == 6);
    System.out.println(getCurrentTeam().getTeamName() + " rolled: " + currentDiceValue);
    return currentDiceValue;
  }

  private boolean canMakeAnyMove() {
    Team currentTeam = getCurrentTeam();

    // Check each piece
    for (int i = 0; i < 4; i++) {
      if (currentTeam.isAtHome(i)) {
        // A home piece can only move if a 6 is rolled.
        if (hasRolledSix) {
          return true;
        }
      }
      // For pieces not at home, check if advancing won't exceed the path using legalMove.
      if (legalMove(currentTeam, i)) {
        return true;
      }
    }
    return false;
  }

  public void moveSelectedPiece(int pieceIndex) {
    if (turnState != TurnState.NEEDS_MOVE) {
      System.out.println("Not time to move!");
      return;
    }

    Team currentTeam = getCurrentTeam();
    int attempts = 0;
    int currentPiece = pieceIndex;

    while (attempts < 4) {
      if (hasRolledSix && isAtHome(currentTeam, currentPiece)) {
        currentTeam.moveToStart(currentPiece);
        System.out.println("Moved from home to start");
        turnState = TurnState.TURN_COMPLETE;
        handleTurn();
        return;
      } 
      
      else if (currentTeam.isAtHome(currentPiece)) {
        System.out.println("Piece " + (currentPiece + 1) + " is still at home");
      } 
      
      else if (currentTeam.isFinished(currentPiece)) {
        System.out.println("Piece " + (currentPiece + 1) + " has already finished");
      } 
      
      else if (isOnPath(currentTeam, currentPiece) && legalMove(currentTeam, currentPiece)) {
        moveOnPath(currentTeam, currentPiece);
        turnState = TurnState.TURN_COMPLETE;
        handleTurn();
        return;
      }

      // Move to next piece (wrapping around with %4)
      currentPiece = (currentPiece + 1) % 4;
      attempts++;
    }

    System.out.println("No valid moves found after checking all pieces");
    turnState = TurnState.TURN_COMPLETE;
    handleTurn();
  }

  private void endTurn() {
    if (winCondition()) {
        turnState = TurnState.GAME_OVER;
        System.out.println(getCurrentTeam().getTeamName() + " wins!");
    } 
    
    else {
        currentTeamIndex = (currentTeamIndex + 1) % teams.length;
        currentDiceValue = 0;
        hasRolledSix = false;
        turnState = TurnState.NEEDS_ROLL;
    }
}

  // ==================== POSITION HELPERS ====================
  private List<Integer> findPiecesAtPosition(Team team, int row, int col) {
    List<Integer> pieces = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
      if (!team.isAtHome(i) &&
          team.getCurrentRow(i) == row &&
          team.getCurrentCol(i) == col) {
        pieces.add(i);
      }
    }
    return pieces;
  }

  private boolean isAtHome(Team team, int pieceIndex) {
    float[] home = getHomePositions(team)[pieceIndex];
    return team.getCurrentRow(pieceIndex) == (int) home[0] &&
           team.getCurrentCol(pieceIndex) == (int) home[1];
  }

  private boolean isOnPath(Team team, int pieceIndex) {
    int row = team.getCurrentRow(pieceIndex);
    int col = team.getCurrentCol(pieceIndex);
    for (int[] pos : team.getPath()) {
      if (pos[0] == row && pos[1] == col) {
        return true;
      }
    }
    return false;
  }

  public boolean legalMove(Team team, int pieceIndex) {
    int row = team.getCurrentRow(pieceIndex);
    int col = team.getCurrentCol(pieceIndex);
    int[][] path = team.getPath();

    for (int i = 0; i < path.length; i++) {
      if (path[i][0] == row && path[i][1] == col) {
        // Check if current path index plus dice roll is within bounds
        return (i + currentDiceValue) < path.length;
      }
    }
    return false; // Piece is not on the path
  }

  private void moveOnPath(Team movingTeam, int pieceIndex) throws ArrayIndexOutOfBoundsException {
    // 1. Find current position index on path
    int currentPathIndex = findCurrentPathIndex(movingTeam, pieceIndex);
    int newIndex = currentPathIndex + currentDiceValue;
    int[] newPos = movingTeam.getPath()[newIndex];

    // 3. Check if piece completes its path
    if (newIndex == movingTeam.getPath().length - 1) {
      movingTeam.setFinished(pieceIndex);
      System.out.println(movingTeam.getTeamName() + "'s piece " + (pieceIndex + 1) + " completed the path!");
      movingTeam.movePiece(pieceIndex, newPos[0], newPos[1], 0.1f);
      return; // Skip captures for finishing moves
    }

    // 4. Capture logic (only on non-safe squares)
    if (!isSafePosition(newPos[0], newPos[1])) {
      for (Team otherTeam : teams) {
        if (otherTeam != movingTeam) {
          List<Integer> enemyPieces = findPiecesAtPosition(otherTeam, newPos[0], newPos[1]);
          for (int enemyIndex : enemyPieces) {
            otherTeam.placeAtHomePosition(enemyIndex);
            System.out.println(otherTeam.getTeamName() + "'s piece " + (enemyIndex + 1) + " was captured!");
          }
        }
      }
    }

    // 5. Execute the move
    movingTeam.movePiece(pieceIndex, newPos[0], newPos[1], 0.1f);
  }

  private int findCurrentPathIndex(Team team, int pieceIndex) {
    int currentRow = team.getCurrentRow(pieceIndex);
    int currentCol = team.getCurrentCol(pieceIndex);
    int[][] path = team.getPath();
    for (int i = 0; i < path.length; i++) {
      if (path[i][0] == currentRow && path[i][1] == currentCol) {
        return i; // Return the found index
      }
    }
    return -1; // Not found on path
  }

  private boolean isSafePosition(int row, int col) {
    // Star squares
    return (row == 6 && col == 2) ||  // Example safe positions
           (row == 8 && col == 12) ||
           (row == 2 && col == 8) ||
           (row == 12 && col == 6) ||
           // Home squares
           (row == 8 && col == 1) ||
           (row == 6 && col == 13) ||
           (row == 13 && col == 8) ||   // blue
           (row == 1 && col == 6);       // green
  }

  // ==================== CONFIGURATION HELPERS ====================
  public Team[] getTeams() {
    return Arrays.copyOf(teams, teams.length);
  }

  private float[][] getHomePositions(Team team) {
    if (team instanceof RedTeam) {
      return TeamConfig.Red.HOME_POSITIONS;
    } else if (team instanceof YellowTeam) {
      return TeamConfig.Yellow.HOME_POSITIONS;
    }
    throw new IllegalArgumentException("Unknown team type");
  }

  // ==================== STATE QUERIES ====================
  public Team getCurrentTeam() {
    return teams[currentTeamIndex];
  }

  public int getCurrentDiceValue() {
    return currentDiceValue;
  }

  public boolean hasRolledSix() {
    return hasRolledSix;
  }

  public boolean isWaitingForRoll() {
    return turnState == TurnState.NEEDS_ROLL;
  }

  public boolean isWaitingForMove() {
    return turnState == TurnState.NEEDS_MOVE;
  }

  public boolean isNoMovesState() {
    return turnState == TurnState.NO_MOVES;
  }
    
  public void forceTurnEnd() {
    turnState = TurnState.TURN_COMPLETE;
    handleTurn();
  }

  public Team getWinningTeam() {
    if (turnState == TurnState.GAME_OVER) {
        return getCurrentTeam();
    }
    return null;
}

  public void resetGame() {

    for (Team team : teams) {
      for (int j = 0; j < 4; j++) {
        team.placeAtHomePosition(j);
      }
    }
    currentTeamIndex = 0;
    currentDiceValue = 0;
    hasRolledSix = false;
    turnState = TurnState.NEEDS_ROLL;
  }
}