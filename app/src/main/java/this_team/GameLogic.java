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
        TURN_COMPLETE
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
        }
    }

    public int rollDice() {
        if (turnState != TurnState.NEEDS_ROLL) {
            System.out.println("Cannot re-roll! Finish your move first.");
            return -1;
        }
        
        currentDiceValue = random.nextInt(6) + 1;
        hasRolledSix = (currentDiceValue == 6);
        System.out.println(getCurrentTeam().getTeamName() + " rolled: " + currentDiceValue);
        return currentDiceValue;
    }

    public void moveSelectedPiece(int pieceIndex) {
        if (turnState != TurnState.NEEDS_MOVE) {
            System.out.println("Not time to move!");
            return;
        }

        Team currentTeam = getCurrentTeam();
        
        if (hasRolledSix && isAtHome(currentTeam, pieceIndex)) {
            currentTeam.moveToStart(pieceIndex);
            System.out.println("Moved from home to start");
        } 
        else if (isOnPath(currentTeam, pieceIndex)) {
            moveOnPath(currentTeam, pieceIndex);
        }
        else {
            System.out.println("Invalid move");
            return;
        }
        
        turnState = TurnState.TURN_COMPLETE;
        handleTurn();
    }

    private void endTurn() {
        currentTeamIndex = (currentTeamIndex + 1) % teams.length;
        currentDiceValue = 0;
        hasRolledSix = false;
        turnState = TurnState.NEEDS_ROLL;
    }

    // ==================== MOVE VALIDATION ====================
    private boolean canMakeAnyMove() {
        Team currentTeam = getCurrentTeam();
        int homePieces = 0;
        
        for (int i = 0; i < 4; i++) {
            if (currentTeam.isAtHome(i)) homePieces++;
        }
        
        if (homePieces == 4) return hasRolledSix;
        return true;
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
        return team.getCurrentRow(pieceIndex) == (int)home[0] && 
               team.getCurrentCol(pieceIndex) == (int)home[1];
    }

    private boolean isOnPath(Team team, int pieceIndex) {
        int row = team.getCurrentRow(pieceIndex);
        int col = team.getCurrentCol(pieceIndex);
        for (int[] pos : team.getPath()) {
            if (pos[0] == row && pos[1] == col) return true;
        }
        return false;
    }

    private void moveOnPath(Team movingTeam, int pieceIndex) {
        // 1. Find current position index on path
        int currentPathIndex = -1;
        int currentRow = movingTeam.getCurrentRow(pieceIndex);
        int currentCol = movingTeam.getCurrentCol(pieceIndex);
        
        // Locate current position in path array
        for (int i = 0; i < movingTeam.getPath().length; i++) {
            if (movingTeam.getPath()[i][0] == currentRow && 
                movingTeam.getPath()[i][1] == currentCol) {
                currentPathIndex = i;
                break;
            }
        }
        
        // 2. Calculate new position
        int newIndex = currentPathIndex + currentDiceValue;
        int[] newPos = movingTeam.getPath()[newIndex];
        
        // 3. Check if piece completes its path
        if (newIndex == movingTeam.getPath().length - 1) {
            movingTeam.setFinished(pieceIndex);
            System.out.println(movingTeam.getTeamName() + "'s piece " + 
                            (pieceIndex + 1) + " completed the path!");
            movingTeam.movePiece(pieceIndex, newPos[0], newPos[1], 0.1f); // Fixed: using movingTeam
            return; // Skip captures for finishing moves
        }
        
        // 4. Validate move bounds
        if (newIndex >= movingTeam.getPath().length) {
            System.out.println("Cannot move beyond path!");
            return;
        }
        
        // 5. Capture logic (only on non-safe squares)
        if (!isSafePosition(newPos[0], newPos[1])) {
            for (Team otherTeam : teams) {
                if (otherTeam != movingTeam) {
                    List<Integer> enemyPieces = findPiecesAtPosition(otherTeam, newPos[0], newPos[1]);
                    for (int enemyIndex : enemyPieces) {
                        otherTeam.placeAtHomePosition(enemyIndex);
                        System.out.println(otherTeam.getTeamName() + "'s piece " + 
                                        (enemyIndex + 1) + " was captured!");
                    }
                }
            }
        }
        
        // 6. Execute the move
        movingTeam.movePiece(pieceIndex, newPos[0], newPos[1], 0.1f);
    }

    
    private boolean isSafePosition(int row, int col) {
               // star squares 
        return (row == 6 && col == 2) ||  // Example safe positions
               (row == 8 && col == 12) ||
               (row == 2 && col == 8) ||
               (row == 12 && col == 6) ||

               /// home squares 
               (row == 8 && col == 1)||
               (row == 6 && col == 13)||
               (row == 13 && col == 8) ||   // blue
               (row == 1 && col == 6); // green
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
}












