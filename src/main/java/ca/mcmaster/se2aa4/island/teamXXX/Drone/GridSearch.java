package ca.mcmaster.se2aa4.island.teamXXX.Drone;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import ca.mcmaster.se2aa4.island.teamXXX.Action;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.ActionType;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Heading;

/**
 * GridSearch implements a systematic zigzag search pattern for exploring the island.
 * When reaching an edge, it performs a sequence of turns to move to the next "row"
 * and continue in the opposite direction, ensuring complete coverage of the island.
 */
public class GridSearch {
    // Logger for recording information
    private final Logger logger = LogManager.getLogger();
    
    // Search states for the state pattern
    private enum SearchState {
        SCANNING,           // Performing a scan
        FLYING,             // Flying in current direction
        ECHO_FORWARD,       // Checking for edge
        TURNING_FIRST,      // First turn in the zigzag pattern (to progress direction)
        SCANNING_IN_TURN,   // Scan during turning to catch edge creeks
        TURNING_SECOND      // Second turn in zigzag (changing main direction)
    }
    
    // Current search state
    private SearchState currentState = SearchState.SCANNING;
    
    // Direction tracking
    private Heading mainDirection;       // Main direction (E/W)
    private Heading progressDirection;   // Direction to progress (S)
    private Heading initialDirection;    // Initial direction drone was facing
    private Heading directionBeforeTurn; // Save direction before turn for proper sequence
    
    // Island tracking
    private boolean foundOcean = false;  // Found ocean ahead
    private int groundRange = 0;         // Distance to ground ahead
    private int oceanRange = 0;          // Distance to ocean ahead
    
    // Pattern tracking
    private int scanCount = 0;           // Count of scans performed
    private int flyCount = 0;            // Count of fly actions performed
    private int columnCount = 0;         // Current column number
    private int turnStep = 0;            // Step in the turning sequence
    
    // Discoveries
    private Set<String> creekIds = new HashSet<>();
    private Set<String> siteIds = new HashSet<>();
    private Map<String, int[]> creekPositions = new HashMap<>();
    private Map<String, int[]> sitePositions = new HashMap<>();
    private String closestCreek = null;
    
    /**
     * Get the heading to the left of the current heading
     */
    private Heading getLeftHeading(Heading heading) {
        switch (heading) {
            case N: return Heading.W;
            case E: return Heading.N;
            case S: return Heading.E;
            case W: return Heading.S;
            default: return heading;
        }
    }
    
    /**
     * Get the heading to the right of the current heading
     */
    private Heading getRightHeading(Heading heading) {
        switch (heading) {
            case N: return Heading.E;
            case E: return Heading.S;
            case S: return Heading.W;
            case W: return Heading.N;
            default: return heading;
        }
    }
    
    /**
     * Helper method to get relative headings safely
     */
    private Heading getRelativeHeading(Heading currentHeading, Heading targetHeading) {
        // If we're just returning the same heading, that's safe
        if (targetHeading == currentHeading) {
            return targetHeading;
        }
        
        // Get the relative direction (left, right, opposite)
        if (targetHeading == getLeftHeading(currentHeading)) {
            return targetHeading; // Left is safe
        } else if (targetHeading == getRightHeading(currentHeading)) {
            return targetHeading; // Right is safe
        } else {
            // We want to go in the opposite direction, need two turns
            // First turn left (safest option)
            return getLeftHeading(currentHeading);
        }
    }
    
    /**
     * Initialize the grid search with the initial heading
     */
    public GridSearch(int maxColumns, int maxRows, Heading initialHeading) {
        this.initialDirection = initialHeading;
        
        // Start by going east or in the initial direction
        if (initialHeading == Heading.N || initialHeading == Heading.S) {
            mainDirection = Heading.E;
        } else {
            mainDirection = initialHeading;
        }
        
        // Progress direction is south
        progressDirection = Heading.S;
        directionBeforeTurn = mainDirection;
        
        // Start with a scan
        currentState = SearchState.SCANNING;
    }
    
    /**
     * Get the next action in the search pattern
     * @param heading Current heading of the drone
     * @param lastResponse Last response from the game engine
     * @return Next action to perform
     */
    public Action getNextAction(Heading heading, JSONObject lastResponse) {
        // Process response to extract discoveries
        processResponse(lastResponse, heading);
        
        // Handle each state in the state pattern
        switch (currentState) {
            case SCANNING:
                scanCount++;
                
                // After scanning, check if we need to echo or fly
                if (groundRange > 2) {
                    // If we know ground is ahead, keep flying
                    currentState = SearchState.FLYING;
                    groundRange--;
                    return new Action(ActionType.FLY);
                }
                
                // Process scan results to check if we're on land or ocean
                if (lastResponse != null && lastResponse.has("extras") && 
                    lastResponse.getJSONObject("extras").has("biomes")) {
                    
                    JSONArray biomes = lastResponse.getJSONObject("extras").getJSONArray("biomes");
                    boolean onlyOcean = true;
                    
                    // Check if we're only seeing ocean
                    for (int i = 0; i < biomes.length(); i++) {
                        if (!biomes.getString(i).equals("OCEAN")) {
                            onlyOcean = false;
                            break;
                        }
                    }
                    
                    if (onlyOcean) {
                        // If we only see ocean, echo forward to check for edge
                        currentState = SearchState.ECHO_FORWARD;
                        Action echoAction = new Action(ActionType.ECHO);
                        
                        // Always echo in the current heading direction
                        echoAction.setParameter("direction", heading);
                        logger.info("Ocean detected, echoing forward in direction: " + heading);
                        
                        // Remember our current heading for the turn sequence
                        directionBeforeTurn = heading;
                        
                        return echoAction;
                    }
                }
                
                // Default behavior: fly after scanning if we're on land
                currentState = SearchState.FLYING;
                
                // If not facing the correct direction, turn first
                if (heading != mainDirection) {
                    Action headingAction = new Action(ActionType.HEADING);
                    headingAction.setParameter("direction", mainDirection);
                    return headingAction;
                }
                
                return new Action(ActionType.FLY);
                
            case FLYING:
                flyCount++;
                currentState = SearchState.SCANNING; // Scan after every fly
                return new Action(ActionType.SCAN);
                
            case ECHO_FORWARD:
                // Process echo results to check for island edge
                if (lastResponse != null && lastResponse.has("extras") && 
                    lastResponse.getJSONObject("extras").has("found")) {
                    
                    JSONObject extras = lastResponse.getJSONObject("extras");
                    String found = extras.getString("found");
                    int range = extras.getInt("range");
                    
                    // Only make turning decisions based on echo in our current heading direction
                    // Verify that this response is from our forward echo by checking direction
                    boolean isForwardEcho = true;
                    if (extras.has("direction")) {
                        Heading echoDirection = Heading.valueOf(extras.getString("direction"));
                        isForwardEcho = (echoDirection == heading);
                        logger.info("Echo direction: " + echoDirection + ", current heading: " + heading + ", isForward: " + isForwardEcho);
                    }
                    
                    if (found.equals("OUT_OF_RANGE") && isForwardEcho) {
                        // Found ocean ahead in our current heading direction
                        oceanRange = range;
                        logger.info("Edge detected ahead with range: " + range + " while heading: " + heading);
                        
                        if (range <= 1) {
                            // We're at the edge of the map, start turning sequence
                            logger.info("Edge is very close ahead (range <= 1), starting turn sequence");
                            
                            // Determine which way to turn based on current heading
                            if (heading == Heading.E) {
                                // When heading east, make a right U-turn (turn south first)
                                logger.info("Heading east - making a right U-turn (south first)");
                                currentState = SearchState.TURNING_FIRST;
                                Action turnAction = new Action(ActionType.HEADING);
                                turnAction.setParameter("direction", Heading.S);
                                return turnAction;
                            } else if (heading == Heading.W) {
                                // When heading west, make a left U-turn (turn south first)
                                logger.info("Heading west - making a left U-turn (south first)");
                                currentState = SearchState.TURNING_FIRST;
                                Action turnAction = new Action(ActionType.HEADING);
                                turnAction.setParameter("direction", Heading.S);
                                return turnAction;
                            } else {
                                // For other directions, turn south as well
                                logger.info("Heading " + heading + " - turning south");
                                currentState = SearchState.TURNING_FIRST;
                                Action turnAction = new Action(ActionType.HEADING);
                                turnAction.setParameter("direction", Heading.S);
                                return turnAction;
                            }
                        } else if (range <= 3) {
                            // Edge is within 3 units, fly closer
                            logger.info("Edge is close ahead (range <= 3), flying closer");
                            currentState = SearchState.FLYING;
                            return new Action(ActionType.FLY);
                        } else {
                            // Edge is far away, continue flying
                            currentState = SearchState.FLYING;
                            return new Action(ActionType.FLY);
                        }
                    } else if (found.equals("GROUND")) {
                        // Still on land, remember ground range
                        if (range > 1) {
                            groundRange = range;
                        }
                        // Continue flying
                        currentState = SearchState.FLYING;
                        return new Action(ActionType.FLY);
                    } else {
                        // Either not a forward echo or not out of range, continue flying
                        currentState = SearchState.FLYING;
                        return new Action(ActionType.FLY);
                    }
                }
                
                // Default: continue flying if no clear echo results
                currentState = SearchState.FLYING;
                return new Action(ActionType.FLY);
                
            case TURNING_FIRST:
                // First turn completed (turned south), scan to check for creeks at the edge
                currentState = SearchState.SCANNING_IN_TURN;
                logger.info("First turn completed (now facing south), scanning edge");
                return new Action(ActionType.SCAN);
                
            case SCANNING_IN_TURN:
                // After scanning during turn, move to the second turn
                logger.info("Edge scan complete, continuing turn sequence");
                currentState = SearchState.TURNING_SECOND;
                
                // For the second turn, decide based on what direction we were heading before
                Action turnAction = new Action(ActionType.HEADING);
                if (directionBeforeTurn == Heading.E) {
                    // If we were heading east, now turn west (right U-turn)
                    turnAction.setParameter("direction", Heading.W);
                    logger.info("Second turn: east→south→west (right U-turn)");
                } else {
                    // If we were heading west or other, now turn east (left U-turn)
                    turnAction.setParameter("direction", Heading.E);
                    logger.info("Second turn: west→south→east (left U-turn)");
                }
                return turnAction;
                
            case TURNING_SECOND:
                // Second turn complete, now we're going in the opposite direction
                // Update our main direction
                mainDirection = (directionBeforeTurn == Heading.E) ? Heading.W : Heading.E;
                directionBeforeTurn = mainDirection;
                
                // Increment column count since we've moved to a new row
                columnCount++;
                logger.info("U-turn complete, now heading " + mainDirection + " (column " + columnCount + ")");
                
                // Start with a scan in the new direction
                currentState = SearchState.SCANNING;
                return new Action(ActionType.SCAN);
                
            default:
                // Should never reach here
                currentState = SearchState.SCANNING;
                return new Action(ActionType.SCAN);
        }
    }
    
    /**
     * Process the response to extract discoveries and update search state
     */
    private void processResponse(JSONObject response, Heading currentHeading) {
        if (response != null && response.has("extras")) {
            JSONObject extras = response.getJSONObject("extras");
            
            // Process creeks
            if (extras.has("creeks")) {
                JSONArray creeks = extras.getJSONArray("creeks");
                for (int i = 0; i < creeks.length(); i++) {
                    String creekId = creeks.getString(i);
                    creekIds.add(creekId);
                    
                    // Store notional position
                    creekPositions.put(creekId, new int[]{columnCount, flyCount});
                    logger.info("Discovered creek: " + creekId + " at column " + columnCount);
                }
            }
            
            // Process emergency sites
            if (extras.has("sites")) {
                JSONArray sites = extras.getJSONArray("sites");
                for (int i = 0; i < sites.length(); i++) {
                    String siteId = sites.getString(i);
                    siteIds.add(siteId);
                    
                    // Store notional position
                    sitePositions.put(siteId, new int[]{columnCount, flyCount});
                    logger.info("Discovered emergency site: " + siteId + " at column " + columnCount);
                }
            }
            
            // Calculate closest creek whenever we have both creeks and sites
            if (!creekIds.isEmpty() && !siteIds.isEmpty()) {
                calculateClosestCreek();
            }
        }
    }
    
    /**
     * Calculate the closest creek to any emergency site
     */
    private void calculateClosestCreek() {
        double closestDistance = Double.MAX_VALUE;
        String closestId = null;
        
        // For each site and creek pair, calculate distance
        for (String siteId : siteIds) {
            int[] sitePos = sitePositions.get(siteId);
            
            for (String creekId : creekIds) {
                int[] creekPos = creekPositions.get(creekId);
                
                // Calculate Manhattan distance
                double distance = Math.abs(sitePos[0] - creekPos[0]) + Math.abs(sitePos[1] - creekPos[1]);
                
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestId = creekId;
                }
            }
        }
        
        closestCreek = closestId;
        logger.info("Updated closest creek: " + closestId);
    }
    
    /**
     * Check if the grid search has visited enough cells
     */
    public boolean isCompleted() {
        // Complete if we've done enough scans
        if (scanCount > 60) {
            logger.info("Exploration complete: maximum scan count reached (" + scanCount + ")");
            return true;
        }
        
        // Complete if we've found both creeks and emergency sites
        if (!creekIds.isEmpty() && !siteIds.isEmpty() && scanCount > 20) {
            logger.info("Exploration complete: found " + creekIds.size() + " creeks and " + 
                    siteIds.size() + " emergency sites after " + scanCount + " scans");
            return true;
        }
        
        // Continue exploring
        return false;
    }
    
    /**
     * Get the closest creek ID for the final report
     */
    public String getClosestCreek() {
        // Calculate one final time
        if (!creekIds.isEmpty() && !siteIds.isEmpty()) {
            calculateClosestCreek();
        }
        
        // If there are no creeks or sites, return default message
        if (creekIds.isEmpty()) {
            return "NONE";
        }
        
        // If there are creeks but no sites, return the first creek
        if (siteIds.isEmpty()) {
            return creekIds.iterator().next();
        }
        
        return closestCreek;
    }
    
    /**
     * Get statistics about the search
     */
    public String getStatistics() {
        return "Scans: " + scanCount + 
               ", Moves: " + flyCount + 
               ", Discoveries: " + creekIds.size() + " creeks, " + 
               siteIds.size() + " sites";
    }
    
    /**
     * Get all discovered creek IDs
     */
    public Set<String> getCreekIds() {
        return creekIds;
    }
    
    /**
     * Get all discovered emergency site IDs
     */
    public Set<String> getSiteIds() {
        return siteIds;
    }
}
