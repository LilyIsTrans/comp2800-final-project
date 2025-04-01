package this_team;

import javax.swing.JOptionPane;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class GameSetup {
    
    public static GameLogic initializeGame() throws FileNotFoundException {
        // 1. Get number of players (1 or 2)
        int numPlayers = getPlayerCount();
        
        // 2. Assign colors to players
        List<Team> teams = assignColors(numPlayers);
        
        // 3. Initialize and return GameLogic
        return new GameLogic(teams.toArray(new Team[0]));
    }
    
    private static int getPlayerCount() {
        Object[] options = {"1 Player", "2 Players"};
        return JOptionPane.showOptionDialog(
            null,
            "Select number of players:",
            "Game Setup",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[1] // Default to 2 players
        ) + 1; // Convert 0/1 to 1/2
    }
    
    private static List<Team> assignColors(int numPlayers) throws FileNotFoundException {
        List<Team> teams = new ArrayList<>();
        float gridSize = 8.0f; // 
        float cellSize = 1.0f;
        String[] colors = {"Red", "Yellow"};
        
        for (int i = 0; i < numPlayers; i++) {
            // Let players choose colors (default: Player 1=Red, Player 2=Yellow)
            int colorChoice = JOptionPane.showOptionDialog(
                null,
                "Player " + (i+1) + ", choose your color:",
                "Color Selection",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                colors,
                colors[i] // Default to Red/Yellow in order
            );
            
            Team team = (colorChoice == 0) 
                ? new RedTeam(gridSize, cellSize)
                : new YellowTeam(gridSize, cellSize);
            teams.add(team);
        }
        return teams;
    }
}