package ca.mcmaster.se2aa4.island.teamXXX.Drone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import ca.mcmaster.se2aa4.island.teamXXX.Action;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.ActionType;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Heading;
import ca.mcmaster.se2aa4.island.teamXXX.Vector;

/**
 * GridSearch implements a systematic search pattern for exploring the island
 * after it's been located. Each scan covers a 3x3 grid around the drone.
 * This implementation is based on a state pattern that efficiently navigates
 * columns and handles edge detection.
 */
public class GridSearch {
    // State pattern for grid search states
    private interface SearchState {
        Action handle(GridSearch gridSearch, Vector position, Heading currentHeading, JSONObject lastResponse);
    }
    
    // Initial scanning state
    private class ScanningState implements SearchState {
        @Override
        public Action handle(GridSearch gridSearch, Vector position, Heading currentHeading, JSONObject lastResponse) {
            // If we have a last response, process any discoveries
            if (lastResponse != null && lastResponse.has("extras")) {
                processDiscoveries(lastResponse, position);
            }
            
            // After scanning, move to flying state
            currentState = new FlyingState();
            return new Action(ActionType.SCAN);
        }
    }
    
    // Flying along a column state
    private class FlyingState implements SearchState {
        @Override
        public Action handle(GridSearch gridSearch, Vector position, Heading currentHeading, JSONObject lastResponse) {
            // If we've flown enough in this direction, scan again
            if (++moveCounter >= MOVE_STEP) {
                moveCounter = 0;
                currentState = new ScanningState();
                return new Action(ActionType.SCAN);
            }
            
            // Check if we need to change direction first
            if (currentHeading != currentDirection) {
                Action headingAction = new Action(ActionType.HEADING);
                headingAction.setParameter("direction", currentDirection);
                return headingAction;
            }
            
            // Continue flying
            return new Action(ActionType.FLY);
        }
    }
    
    // Checking if we've reached edge state (using echo)
    private class EdgeCheckingState implements SearchState {
        @Override
        public Action handle(GridSearch gridSearch, Vector position, Heading currentHeading, JSONObject lastResponse) {
            // If we got a response with echo information
            if (lastResponse != null && lastResponse.has("extras") && lastResponse.getJSONObject("extras").has("found")) {
                JSONObject extras = lastResponse.getJSONObject("extras");
                if (extras.getString("found").equals("OUT_OF_RANGE")) {
                    // We're at the edge, time to turn
                    edgeRange = extras.getInt("range");
                    currentState = new TurningState();
                    return new Action(ActionType.HEADING);
                } else {
                    // There's still ground ahead, keep flying
                    groundRange = extras.getInt("range");
                    currentState = new FlyingState();
                    return new Action(ActionType.FLY);
                }
            }
            
            // Send an echo in the current direction
            Action echoAction = new Action(ActionType.ECHO);
            echoAction.setParameter("direction", currentDirection);
            return echoAction;
        }
    }
    
    // State for executing the turn to next column
    private class TurningState implements SearchState {
        @Override
        public Action handle(GridSearch gridSearch, Vector position, Heading currentHeading, JSONObject lastResponse) {
            // Turn sequence: first turn perpendicular
            if (turnStep == 0) {
                // First turn is to the right of current direction
                Heading turnDirection = currentDirection.right();
                // Update for next turn step
                turnStep++;
                Action turnAction = new Action(ActionType.HEADING);
                turnAction.setParameter("direction", turnDirection);
                return turnAction;
            }
            // Turn sequence: scan after first turn
            else if (turnStep == 1) {
                // Scan after turning to catch any creeks at the edge
                turnStep++;
                currentState = new EdgeScanState();
                return new Action(ActionType.SCAN);
            }
            // After turn is complete, go back to flying
            else {
                currentState = new FlyingState();
                return new Action(ActionType.FLY);
            }
        }
    }
    
    // Special scan state during edge turning
    private class EdgeScanState implements SearchState {
        @Override
        public Action handle(GridSearch gridSearch, Vector position, Heading currentHeading, JSONObject lastResponse) {
            // Process any discoveries from the scan
            if (lastResponse != null && lastResponse.has("extras")) {
                processDiscoveries(lastResponse, position);
            }
            
            // Check if we've completed all columns
            columnCount++;
            if (columnCount >= maxColumns) {
                // We've searched the entire grid
                currentState = new CompletedState();
                return new Action(ActionType.STOP);
            }
            
            // Change direction for next column
            currentDirection = currentDirection.opposite();
            
            // Reset turn step for next turn
            turnStep = 0;
            
            // Move to flying state in the new direction
            currentState = new FlyingState();
            return new Action(ActionType.FLY);
        }
    }
    
    // Final completed state
    private class CompletedState implements SearchState {
        @Override
        public Action handle(GridSearch gridSearch, Vector position, Heading currentHeading, JSONObject lastResponse) {
            return new Action(ActionType.STOP);
        }
    }

    // Current state of the grid search
    private SearchState currentState;
    
    // Grid parameters
    private final int maxColumns;
    private int columnCount = 0;
    
    // Movement tracking
    private final int MOVE_STEP = 2;  // How many cells to move before scanning
    private int moveCounter = 0;
    private int turnStep = 0;
    
    // Direction tracking
    private Heading currentDirection;
    
    // Range values
    private int edgeRange = 0;
    private int groundRange = 0;
    
    // Discovered items tracking
    private Map<Vector, List<String>> biomes = new HashMap<>();
    private List<String> creekIds = new ArrayList<>();
    private List<String> siteIds = new ArrayList<>();
    
    // Track creek and site positions for closest calculation
    private Map<String, Vector> creekPositions = new HashMap<>();
    private Map<String, Vector> sitePositions = new HashMap<>();
    
    /**
     * Initialize the grid search with boundaries
     */
    public GridSearch(int maxColumns, int maxRows) {
        this.maxColumns = maxColumns;
        this.currentState = new ScanningState();
        this.currentDirection = Heading.E;  // Start by going east
    }
    
    /**
     * Get the next action in the search pattern
     */
    public Action getNextAction(Vector position, Heading currentHeading, JSONObject lastResponse) {
        return currentState.handle(this, position, currentHeading, lastResponse);
    }
    
    /**
     * Process discoveries from a scan response
     */
    private void processDiscoveries(JSONObject response, Vector position) {
        try {
            JSONObject extras = response.getJSONObject("extras");
            
            // Process creeks
            if (extras.has("creeks")) {
                JSONArray creeksArray = extras.getJSONArray("creeks");
                for (int i = 0; i < creeksArray.length(); i++) {
                    String creekId = creeksArray.getString(i);
                    if (!creekIds.contains(creekId)) {
                        creekIds.add(creekId);
                        creekPositions.put(creekId, position);
                    }
                }
            }
            
            // Process sites
            if (extras.has("sites")) {
                JSONArray sitesArray = extras.getJSONArray("sites");
                for (int i = 0; i < sitesArray.length(); i++) {
                    String siteId = sitesArray.getString(i);
                    if (!siteIds.contains(siteId)) {
                        siteIds.add(siteId);
                        sitePositions.put(siteId, position);
                    }
                }
            }
            
            // Process biomes
            if (extras.has("biomes")) {
                JSONArray biomesArray = extras.getJSONArray("biomes");
                List<String> biomeList = new ArrayList<>();
                for (int i = 0; i < biomesArray.length(); i++) {
                    biomeList.add(biomesArray.getString(i));
                }
                biomes.put(position, biomeList);
                
                // Check if we've hit ocean - we might need to check for edge
                boolean allOcean = true;
                for (String biome : biomeList) {
                    if (!biome.equals("OCEAN")) {
                        allOcean = false;
                        break;
                    }
                }
                
                if (allOcean) {
                    // All ocean means we need to echo to check for edge
                    currentState = new EdgeCheckingState();
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing discoveries: " + e.getMessage());
        }
    }
    
    /**
     * Get the closest creek to any emergency site
     */
    public String getClosestCreek() {
        // If no creeks or sites, return nothing
        if (creekIds.isEmpty() || siteIds.isEmpty()) {
            return creekIds.isEmpty() ? null : creekIds.get(0);
        }
        
        // Find the closest creek to any site
        String closestCreek = null;
        double minDistance = Double.MAX_VALUE;
        
        for (String siteId : siteIds) {
            Vector sitePos = sitePositions.get(siteId);
            
            for (String creekId : creekIds) {
                Vector creekPos = creekPositions.get(creekId);
                
                // Calculate Manhattan distance
                double distance = Math.abs(sitePos.x - creekPos.x) + Math.abs(sitePos.y - creekPos.y);
                
                if (distance < minDistance) {
                    minDistance = distance;
                    closestCreek = creekId;
                }
            }
        }
        
        return closestCreek != null ? closestCreek : creekIds.get(0);
    }
    
    /**
     * Get all discovered creek IDs
     */
    public List<String> getCreekIds() {
        return new ArrayList<>(creekIds);
    }
    
    /**
     * Get all discovered site IDs
     */
    public List<String> getSiteIds() {
        return new ArrayList<>(siteIds);
    }
    
    /**
     * Check if the grid search is complete
     */
    public boolean isCompleted() {
        return currentState instanceof CompletedState;
    }
}
