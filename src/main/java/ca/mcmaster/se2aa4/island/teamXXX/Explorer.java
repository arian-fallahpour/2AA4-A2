package ca.mcmaster.se2aa4.island.teamXXX;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import ca.mcmaster.se2aa4.island.teamXXX.Drone.Drone;
import ca.mcmaster.se2aa4.island.teamXXX.Drone.GridSearch;
import ca.mcmaster.se2aa4.island.teamXXX.Drone.IslandFinder;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.ActionType;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Heading;
import ca.mcmaster.se2aa4.island.teamXXX.Response.ScanResponse;
import eu.ace_design.island.bot.IExplorerRaid;

public class Explorer implements IExplorerRaid {
    private Boolean scanned = false;
    private Integer turns = 0;
    private Boolean shouldTurnLeft = false;
    private Drone drone;
    private Integer count = 0;
    private JSONObject lastResponse = null;
    
    // Add IslandFinder instance
    private IslandFinder islandFinder;
    // Add GridSearch instance
    private GridSearch gridSearch;
    // Flag to control exploration phases
    private boolean islandFound = false;
    private boolean islandArrived = false;
    private boolean exploreComplete = false;
    
    // Track all discovered creeks and emergency sites as their original IDs
    private ArrayList<String> creekIds = new ArrayList<>();
    private ArrayList<String> siteIds = new ArrayList<>();
    
    // Store all scan results for final report
    private ArrayList<ScanResponse> scanResponses = new ArrayList<>();
    
    // Track creek and site positions for closest calculation
    private Map<String, int[]> creekPositions = new HashMap<>();
    private Map<String, int[]> sitePositions = new HashMap<>();
    
    // Track the closest creek to emergency site
    private String closestCreekId = null;
    private double closestCreekDistance = Double.MAX_VALUE;

    private ActionType previousActionType;
    // Track moves to ensure drone is making progress
    private int moveCount = 0;
    // Track scan count
    private int scanCount = 0;

    // Track positions we've already visited for systematic coverage
    private java.util.Set<Vector> visitedPositions = new java.util.HashSet<>();
    
    // Track scan statistics
    private int consecutiveNoDiscoveries = 0;
    private final int MAX_NO_DISCOVERIES = 10; // If 10 scans yield nothing new, we may be done
    private int totalScanCount = 0;
    
    // Exploration boundaries
    private int minX = 0, maxX = 0, minY = 0, maxY = 0;

    private final Logger logger = LogManager.getLogger();

    // Exploration phases
    private enum ExplorationPhase {
        FINDING,    // Finding the island
        EXPLORING,  // Exploring the island systematically
        COMPLETED   // Mission completed
    }

    // Current phase of exploration
    private ExplorationPhase currentPhase = ExplorationPhase.FINDING;
    
    // State tracking
    private Heading currentHeading;
    private Action previousAction = null;

    @Override
    public void initialize(String s) {
        logger.info("** Initializing the Exploration Command Center");
        JSONObject info = new JSONObject(new JSONTokener(new StringReader(s)));

        logger.info("** Initialization info:\n {}", info.toString(2));
        Integer battery = info.getInt("budget");
        String directionStr = info.getString("heading");
        
        // Parse heading from string
        currentHeading = Heading.valueOf(directionStr);
        
        logger.info("The drone is facing: {}", currentHeading);
        logger.info("The drone's battery level is: {}", battery);
        
        // Initialize our exploration components
        islandFinder = new IslandFinder(currentHeading);
        gridSearch = new GridSearch(15, 15, currentHeading);
        
        // Create a default initial response object
        createDefaultResponse(directionStr, battery);
        
        logger.info("*** EXPLORATION STRATEGY: Find island with echo, then zigzag scan pattern");
    }

    /**
     * Creates a default initial response object to avoid null on first processing
     */
    private void createDefaultResponse(String heading, Integer battery) {
        // Create the main response object
        JSONObject response = new JSONObject();
        response.put("status", "OK");
        response.put("cost", 0);
        
        // Create extras object with minimal required fields
        JSONObject extras = new JSONObject();
        extras.put("heading", heading);
        extras.put("budget", battery);
        
        // Add empty discovery arrays
        extras.put("creeks", new JSONArray());
        extras.put("sites", new JSONArray());
        extras.put("biomes", new JSONArray());
        
        // Add extras to response
        response.put("extras", extras);
        
        // Set as initial response
        this.lastResponse = response;
        logger.info("Default initial response created");
    }

    @Override
    public String takeDecision() {
        Action action = null;
        
        try {
            // Process based on current exploration phase
            switch (currentPhase) {
                case FINDING:
                    logger.info("Phase 1: Finding island - Heading: " + currentHeading);
                    
                    // Get next action from island finder
                    action = islandFinder.processResponse(lastResponse);
                    
                    // Check if island is found
                    if (islandFinder.isIslandFound() && !islandFound) {
                        islandFound = true;
                        logger.info("*** ISLAND FOUND! Now moving toward it...");
                    }
                    
                    // Check if island finder is complete
                    if (islandFinder.isIslandFound()) {
                        currentPhase = ExplorationPhase.EXPLORING;
                        islandArrived = true;
                        logger.info("*** ARRIVED AT ISLAND! Starting systematic exploration...");
                    }
                    
                    break;
                    
                case EXPLORING:
                    // Calculate simple statistics for logging
                    int creeksFound = gridSearch.getCreekIds().size();
                    int sitesFound = gridSearch.getSiteIds().size();
                    
                    logger.info("Phase 3: Exploring island - Heading: " + currentHeading);
                    logger.info("  Scans: " + scanCount + ", Moves: " + moveCount + 
                             ", Discoveries: " + creeksFound + " creeks, " + sitesFound + " sites");
                    
                    // Get search pattern statistics
                    String searchStats = gridSearch.getStatistics();
                    logger.info("  Search Progress: " + searchStats);
                    
                    // Get next action from grid search
                    action = gridSearch.getNextAction(currentHeading, lastResponse);
                    
                    // Check if grid search is complete
                    if (gridSearch.isCompleted()) {
                        currentPhase = ExplorationPhase.COMPLETED;
                        logger.info("*** EXPLORATION COMPLETE!");
                        logger.info("  Found " + creeksFound + " creeks and " + sitesFound + " emergency sites");
                        
                        String closestCreek = gridSearch.getClosestCreek();
                        if (closestCreek != null && !closestCreek.equals("NONE")) {
                            logger.info("  Closest creek to emergency site: " + closestCreek);
                        } else {
                            logger.info("  No closest creek found");
                        }
                    }
                    
                    break;
                    
                case COMPLETED:
                    logger.info("Phase 4: Mission complete - Heading: " + currentHeading);
                    action = new Action(ActionType.STOP);
                    logger.info("Drone stopped. Mission complete.");
                    break;
            }
        } catch (Exception e) {
            logger.error("Error during takeDecision: " + e.getMessage());
            e.printStackTrace();
            
            // If we encounter an error, scan to make sure we don't miss anything
            action = new Action(ActionType.SCAN);
            logger.info("Encountered an error, defaulting to SCAN action");
        }

        // If somehow we got a null action, perform a scan
        if (action == null) {
            action = new Action(ActionType.SCAN);
            logger.info("Action was null, forcing SCAN action");
        }
        
        // Track action statistics
        previousAction = action;
        if (action.getType() == ActionType.SCAN) {
            scanCount++;
        } else if (action.getType() == ActionType.FLY) {
            moveCount++;
        }
        
        // Log the action we're going to take
        logger.info("Executing action: " + action.getType());
        
        return action.toString();
    }

    @Override
    public void acknowledgeResults(String s) {
        try {
            // Parse the response from the game engine
            JSONObject response = new JSONObject(new JSONTokener(new StringReader(s)));
            lastResponse = response;
            
            // Check if we've reached the end of the exploration
            if (response.has("exception")) {
                // We might have run out of battery
                String message = response.getJSONArray("message").getString(0);
                logger.info("Received exception: " + message);
                
                if (message.contains("out of radio range")) {
                    // We've run out of battery, the exploration is complete
                    logger.info("Drone is out of radio range (battery depleted)");
                    currentPhase = ExplorationPhase.COMPLETED;
                }
                return;
            }
            
            // Update heading if available
            if (response.has("extras") && response.getJSONObject("extras").has("heading")) {
                String headingStr = response.getJSONObject("extras").getString("heading");
                currentHeading = Heading.valueOf(headingStr);
            }
            
            // Log response information
            logger.info("** Response received");
            Integer cost = response.getInt("cost");
            logger.info("  Cost: {}", cost);
            String status = response.getString("status");
            logger.info("  Status: {}", status);
            
            // Log discoveries from scan
            if (previousAction != null && previousAction.getType() == ActionType.SCAN &&
                response.has("extras")) {
                
                JSONObject extras = response.getJSONObject("extras");
                
                if (extras.has("creeks") && extras.getJSONArray("creeks").length() > 0) {
                    logger.info("*** CREEKS FOUND: " + extras.getJSONArray("creeks").toString());
                }
                
                if (extras.has("sites") && extras.getJSONArray("sites").length() > 0) {
                    logger.info("*** EMERGENCY SITES FOUND: " + extras.getJSONArray("sites").toString());
                }
            }
            
        } catch (Exception e) {
            logger.error("Error processing response: " + e.getMessage());
        }
    }

    @Override
    public String deliverFinalReport() {
        // Return the closest creek ID
        String closestCreek = gridSearch.getClosestCreek();
        logger.info("Final report delivered: closest creek is " + closestCreek);
        return closestCreek;
    }
}
