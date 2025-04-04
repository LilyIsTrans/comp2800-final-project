package this_team;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GameLogic {
  private final Team[] teams;
  private int currentTeamIndex = 0;
  private int currentDiceValue = 0;
  private DiceRollWrapper diceRollWrapper;
  private boolean hasRolledSix = false;
  private TurnState turnState = TurnState.NEEDS_ROLL;

  private enum TurnState {
    NEEDS_ROLL,
    NEEDS_MOVE,
    NO_MOVES,
    TURN_COMPLETE,
    GAME_OVER
  }

  public GameLogic(DiceRollWrapper diceRollWrapper, Team... teams) {
    if (teams == null || teams.length == 0) {
      throw new IllegalArgumentException("At least one team required");
    }
    this.teams = teams;
    this.diceRollWrapper = diceRollWrapper;
  }

  public void handleTurn(Consumer<Void> onTurnHandled) {
    switch (turnState) {
      case NEEDS_ROLL:
        rollDice(result -> {
          if (!canMakeAnyMove()) {
            if (winCondition()) {
              turnState = TurnState.GAME_OVER;
            } else {
              turnState = TurnState.NO_MOVES;
            }
          } else {
            turnState = TurnState.NEEDS_MOVE;
          }
          onTurnHandled.accept(null);
        });
        break;
      case NO_MOVES:
      case NEEDS_MOVE:
      case TURN_COMPLETE:
      case GAME_OVER:
        onTurnHandled.accept(null);
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

  public void rollDice(Consumer<Integer> callback) {
    if (turnState != TurnState.NEEDS_ROLL) {
      callback.accept(-1);
      return;
    }

    diceRollWrapper.rollDiceAsync(result -> {
      currentDiceValue = result;
      hasRolledSix = (currentDiceValue == 6);
      callback.accept(currentDiceValue);
    });
  }

  public boolean canMakeAnyMove() {
    Team currentTeam = getCurrentTeam();
    for (int i = 0; i < 4; i++) {
      if (currentTeam.isAtHome(i) && hasRolledSix) {
        return true;
      }
      if (legalMove(currentTeam, i)) {
        return true;
      }
    }
    return false;
  }

  private boolean isAtHome(Team team, int pieceIndex) {
    float[] home = getHomePositions(team)[pieceIndex];
    return team.getCurrentRow(pieceIndex) == (int) home[0] &&
           team.getCurrentCol(pieceIndex) == (int) home[1];
  }
  private float[][] getHomePositions(Team team) {
    if (team instanceof RedTeam) {
      return TeamConfig.Red.HOME_POSITIONS;
    } else if (team instanceof YellowTeam) {
      return TeamConfig.Yellow.HOME_POSITIONS;
    } else if (team instanceof BlueTeam) {
      return TeamConfig.Blue.HOME_POSITIONS;
    } else if (team instanceof GreenTeam) {
      return TeamConfig.Green.HOME_POSITIONS;
    }
  
    throw new IllegalArgumentException("Unknown team type");
  }
  

  public void moveSelectedPiece(int pieceIndex, Consumer<Boolean> callback) {
    if (turnState != TurnState.NEEDS_MOVE) {
        System.out.println("Not time to move!");
        callback.accept(false);
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
            callback.accept(true);
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
            callback.accept(true);
            return;
        }

        currentPiece = (currentPiece + 1) % 4;
        attempts++;
    }

    System.out.println("No valid moves found after checking all pieces");
    turnState = TurnState.TURN_COMPLETE;
    callback.accept(false);
}


  private void endTurn() {
    if (winCondition()) {
      turnState = TurnState.GAME_OVER;
    } else {
      currentTeamIndex = (currentTeamIndex + 1) % teams.length;
      currentDiceValue = 0;
      hasRolledSix = false;
      turnState = TurnState.NEEDS_ROLL;
    }
  }

  public boolean legalMove(Team team, int pieceIndex) {
    int currentIndex = findCurrentPathIndex(team, pieceIndex);
    return currentIndex != -1 && (currentIndex + currentDiceValue) < team.getPath().length;
  }

  private void moveOnPath(Team team, int pieceIndex) {
    int currentIndex = findCurrentPathIndex(team, pieceIndex);
    int newIndex = currentIndex + currentDiceValue;
    int[] newPos = team.getPath()[newIndex];

    if (newIndex == team.getPath().length - 1) {
      team.setFinished(pieceIndex);
      team.movePiece(pieceIndex, newPos[0], newPos[1], 0.1f);
      return;
    }

    if (!isSafePosition(newPos[0], newPos[1])) {
      for (Team otherTeam : teams) {
        if (otherTeam != team) {
          List<Integer> enemyPieces = findPiecesAtPosition(otherTeam, newPos[0], newPos[1]);
          for (int enemyIndex : enemyPieces) {
            otherTeam.placeAtHomePosition(enemyIndex);
          }
        }
      }
    }
    team.movePiece(pieceIndex, newPos[0], newPos[1], 0.1f);
  }

  private int findCurrentPathIndex(Team team, int pieceIndex) {
    int currentRow = team.getCurrentRow(pieceIndex);
    int currentCol = team.getCurrentCol(pieceIndex);
    int[][] path = team.getPath();
    for (int i = 0; i < path.length; i++) {
      if (path[i][0] == currentRow && path[i][1] == currentCol) {
        return i;
      }
    }
    return -1;
  }

  private boolean isSafePosition(int row, int col) {
    return (row == 6 && col == 2) || (row == 8 && col == 12) ||
           (row == 2 && col == 8) || (row == 12 && col == 6) ||
           (row == 8 && col == 1) || (row == 6 && col == 13) ||
           (row == 13 && col == 8) || (row == 1 && col == 6);
  }

  private List<Integer> findPiecesAtPosition(Team team, int row, int col) {
    List<Integer> pieces = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
      if (!team.isAtHome(i) && team.getCurrentRow(i) == row && team.getCurrentCol(i) == col) {
        pieces.add(i);
      }
    }
    return pieces;
  }

  public Team getCurrentTeam() {
    return teams[currentTeamIndex];
  }

  public int getCurrentDiceValue() {
    return currentDiceValue;
  }

  public boolean isWaitingForRoll() {
    return turnState == TurnState.NEEDS_ROLL;
  }

  public boolean isNoMovesState() {
    return turnState == TurnState.NO_MOVES;
  }

  public boolean isWaitingForMove() {
    return turnState == TurnState.NEEDS_MOVE;
  }
  

  public Team getWinningTeam() {
    return turnState == TurnState.GAME_OVER ? getCurrentTeam() : null;
  }

  public void forceTurnEnd() {
    turnState = TurnState.TURN_COMPLETE;
    endTurn();
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

  public boolean isTurnComplete() {
    return turnState == TurnState.TURN_COMPLETE;
}

}




