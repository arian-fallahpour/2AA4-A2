package ca.mcmaster.se2aa4.island.teamXXX.Drone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import ca.mcmaster.se2aa4.island.teamXXX.Action;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.ActionType;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Heading;

/**
 * IslandFinder is responsible for finding the island by using a combination of
 * echo and fly actions. It will echo in different directions (left, right, forward)
 * before flying to ensure comprehensive coverage.
 */
public class IslandFinder {
    private final Logger logger = LogManager.getLogger();
    
    // The state of the island finder
    private enum FinderState {
        INIT,               // Initial state
        ECHO_LEFT,          // Echoing to the left
        ECHO_RIGHT,         // Echoing to the right  
        ECHO_FORWARD,       // Echoing forward
        FLYING_FORWARD,     // Flying forward
        FLYING_TO_ISLAND,   // Flying toward detected island
        SCANNING,           // Scanning after reaching the island
        FOUND               // Island has been found
    }
    
    // Current state
    private FinderState currentState = FinderState.INIT;
    
    // Tracking variables
    private boolean islandDetected = false;   // Whether the island has been detected
    private int distanceToIsland = 0;         // Distance to the detected island
    private int scanCount = 0;                // Number of scans performed after landing
    private int echoCount = 0;                // Number of echoes performed without moving
    private final int MAX_ECHOS_WITHOUT_MOVING = 3;  // Maximum echoes before forcing a move
    private final int MIN_SCANS_TO_COMPLETE = 5;     // Minimum scans to perform after finding island
    
    // Direction tracking for echoing in a rotation
    private enum EchoDirection {
        LEFT, RIGHT, FORWARD
    }
    private EchoDirection currentEchoDirection = EchoDirection.FORWARD;
    
    // Current heading of the drone
    private Heading currentHeading;
    
    /**
     * Constructor to initialize the IslandFinder with the drone's starting heading
     * @param initialHeading The initial heading of the drone
     */
    public IslandFinder(Heading initialHeading) {
        this.currentHeading = initialHeading;
        logger.info("Initialized IslandFinder with heading: " + initialHeading);
    }
    
    /**
     * Default constructor - initializes with North heading
     * This is provided for backward compatibility
     */
    public IslandFinder() {
        this(Heading.N);
        logger.info("Using default constructor with North heading - this should not happen with proper initialization");
    }
    
    /**
     * Process the response from the game engine
     * @param jsonResponse The JSON response from the game engine
     * @return The next action to perform
     */
    public Action processResponse(JSONObject jsonResponse) {
        // Handle null response
        if (jsonResponse == null) {
            return handleNullResponse();
        }
        
        // Get a reference to the extras
        JSONObject extras = null;
        if (jsonResponse.has("extras")) {
            extras = jsonResponse.getJSONObject("extras");
            
            // Update the current heading if available
            if (extras.has("heading")) {
                String headingStr = extras.getString("heading");
                currentHeading = Heading.valueOf(headingStr);
                logger.info("Updated heading to: " + currentHeading);
            }
        }
        
        // Handle each state
        switch (currentState) {
            case INIT:
                logger.info("Starting island search");
                return handleEchoing();
                
            case ECHO_LEFT:
            case ECHO_RIGHT:
            case ECHO_FORWARD:
                // Process echo response
                if (extras != null && extras.has("found")) {
                    String found = extras.getString("found");
                    
                    if (found.equals("GROUND")) {
                        // Island detected!
                        islandDetected = true;
                        distanceToIsland = extras.getInt("range");
                        
                        // If distance is 1, we've reached the island
                        if (distanceToIsland == 1) {
                            logger.info("Island reached!");
                            currentState = FinderState.SCANNING;
                            return new Action(ActionType.SCAN);
                        }
                        
                        // Otherwise, fly toward the island
                        logger.info("Island detected at distance " + distanceToIsland);
                        currentState = FinderState.FLYING_TO_ISLAND;
                        return new Action(ActionType.FLY);
                    }
                }
                
                // Increment echo count since we didn't find ground
                echoCount++;
                
                // If we've echoed in all directions without finding the island,
                // force a fly action to move
                if (echoCount >= MAX_ECHOS_WITHOUT_MOVING) {
                    logger.info("Maximum echoes reached without finding island, flying forward");
                    echoCount = 0;
                    currentState = FinderState.FLYING_FORWARD;
                    return new Action(ActionType.FLY);
                }
                
                // Continue with the next echo direction
                return handleEchoing();
                
            case FLYING_FORWARD:
                logger.info("Flying forward in search of island");
                // After flying, try echoing again
                echoCount = 0;
                return handleEchoing();
                
            case FLYING_TO_ISLAND:
                // If we're flying toward the island, decrement distance
                distanceToIsland--;
                
                if (distanceToIsland <= 1) {
                    logger.info("Arrived at island, starting scan");
                    currentState = FinderState.SCANNING;
                    return new Action(ActionType.SCAN);
                }
                
                // Continue flying to island
                logger.info("Flying to island, remaining distance: " + distanceToIsland);
                return new Action(ActionType.FLY);
                
            case SCANNING:
                // Process scan response
                scanCount++;
                
                // Check if we've found land to confirm we're on the island
                if (extras != null && extras.has("biomes")) {
                    boolean foundLand = false;
                    for (int i = 0; i < extras.getJSONArray("biomes").length(); i++) {
                        String biome = extras.getJSONArray("biomes").getString(i);
                        if (!biome.equals("OCEAN")) {
                            foundLand = true;
                            break;
                        }
                    }
                    
                    if (foundLand) {
                        // We are on land, let's scan some more to find stuff
                        if (scanCount >= MIN_SCANS_TO_COMPLETE) {
                            logger.info("Island found and scanned!");
                            currentState = FinderState.FOUND;
                            return null; // Island finder is complete
                        }
                        
                        // Alternate scan with fly to cover more area
                        if (scanCount % 2 == 0) {
                            return new Action(ActionType.FLY);
                        } else {
                            return new Action(ActionType.SCAN);
                        }
                    } else {
                        // We're not on land, reset to echoing
                        logger.info("Scan did not confirm land, continuing search");
                        return handleEchoing();
                    }
                }
                
                // No extras in scan, keep scanning
                return new Action(ActionType.SCAN);
                
            case FOUND:
                // We've found the island!
                return null;
                
            default:
                return handleEchoing();
        }
    }
    
    /**
     * Handle a null response by returning a safe action
     */
    private Action handleNullResponse() {
        // If island was detected but we're not on it, keep flying
        if (islandDetected && currentState == FinderState.FLYING_TO_ISLAND) {
            distanceToIsland--;
            if (distanceToIsland <= 1) {
                currentState = FinderState.SCANNING;
                return new Action(ActionType.SCAN);
            }
            return new Action(ActionType.FLY);
        }
        
        // Otherwise, start echoing
        return handleEchoing();
    }
    
    /**
     * Handle echoing in different directions in rotation
     */
    private Action handleEchoing() {
        Action echoAction = new Action(ActionType.ECHO);
        
        // Calculate the relative directions based on current heading
        Heading leftDirection = getLeftDirection(currentHeading);
        Heading rightDirection = getRightDirection(currentHeading);
        
        // Rotate through echo directions
        switch (currentEchoDirection) {
            case LEFT:
                currentState = FinderState.ECHO_LEFT;
                echoAction.setParameter("direction", leftDirection);
                currentEchoDirection = EchoDirection.RIGHT;
                logger.info("Echoing LEFT (direction: " + leftDirection + ")");
                break;
                
            case RIGHT:
                currentState = FinderState.ECHO_RIGHT;
                echoAction.setParameter("direction", rightDirection);
                currentEchoDirection = EchoDirection.FORWARD;
                logger.info("Echoing RIGHT (direction: " + rightDirection + ")");
                break;
                
            case FORWARD:
            default:
                currentState = FinderState.ECHO_FORWARD;
                echoAction.setParameter("direction", currentHeading);
                currentEchoDirection = EchoDirection.LEFT;
                logger.info("Echoing FORWARD (direction: " + currentHeading + ")");
                break;
        }
        
        return echoAction;
    }
    
    /**
     * Get the direction to the left of the given heading
     */
    private Heading getLeftDirection(Heading heading) {
        switch (heading) {
            case N: return Heading.W;
            case E: return Heading.N;
            case S: return Heading.E;
            case W: return Heading.S;
            default: return Heading.N; // Should never happen
        }
    }
    
    /**
     * Get the direction to the right of the given heading
     */
    private Heading getRightDirection(Heading heading) {
        switch (heading) {
            case N: return Heading.E;
            case E: return Heading.S;
            case S: return Heading.W;
            case W: return Heading.N;
            default: return Heading.N; // Should never happen
        }
    }
    
    /**
     * Check if the island has been found
     */
    public boolean isIslandFound() {
        return currentState == FinderState.FOUND;
    }
}
