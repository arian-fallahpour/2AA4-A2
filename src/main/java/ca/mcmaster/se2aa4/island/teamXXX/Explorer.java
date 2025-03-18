package ca.mcmaster.se2aa4.island.teamXXX;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONTokener;

import ca.mcmaster.se2aa4.island.teamXXX.Drone.Drone;
import ca.mcmaster.se2aa4.island.teamXXX.Drone.GridSearch;
import ca.mcmaster.se2aa4.island.teamXXX.Drone.IslandFinder;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.ActionType;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Heading;
import ca.mcmaster.se2aa4.island.teamXXX.Response.Response;
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
    
    // Track the closest creek to emergency site
    private String closestCreekId = null;
    private double closestCreekDistance = Double.MAX_VALUE;

    private ActionType previousActionType;

    private final Logger logger = LogManager.getLogger();

    @Override
    public void initialize(String s) {
        logger.info("** Initializing the Exploration Command Center");
        JSONObject info = new JSONObject(new JSONTokener(new StringReader(s)));

        logger.info("** Initialization info:\n {}",info.toString(2));
        Integer battery = info.getInt("budget");
        Heading direction = Heading.valueOf(info.getString("heading"));

        this.drone = new Drone(battery, direction);
        this.scanned = false;
        
        // Initialize our island finder with the drone's current heading
        this.islandFinder = new IslandFinder(direction);
        
        // Initialize the GridSearch with estimated grid size (adjust as needed)
        this.gridSearch = new GridSearch(20, 20);
        
        logger.info("The drone is facing: " + drone.getDirection().toString());
        logger.info("The drone's Battery level is: " + drone.getBattery().getCharge());
    }

    @Override
    public String takeDecision() {
        Action action;
        Vector position = this.drone.getPosition();
        Heading currentHeading = this.drone.getHeading();

        // Phase 1: Find the island
        if (!islandFound) {
            // Process the last response (null on first call)
            action = islandFinder.processResponse(lastResponse);
            
            // Check if the island finder process has found the island
            if (islandFinder.islandFound()) {
                islandFound = true;
                logger.info("Island found! Now moving toward it...");
            }
            
            if (islandFinder.isCompleted()) {
                islandArrived = true;
                logger.info("Arrived at the island! Starting exploration...");
            }
        } 
        // Phase 2: Move to the island
        else if (!islandArrived) {
            // We're still moving to the island
            action = islandFinder.processResponse(lastResponse);
            
            if (islandFinder.isCompleted()) {
                islandArrived = true;
                logger.info("Arrived at the island! Starting exploration...");
            }
        } 
        // Phase 3: Explore the island using grid search
        else if (!exploreComplete) {
            // Use our new grid search pattern to systematically explore the island
            // Pass the last response to process discoveries and determine state
            action = gridSearch.getNextAction(position, currentHeading, lastResponse);
            
            // Check if the grid search is complete
            if (gridSearch.isCompleted()) {
                exploreComplete = true;
                logger.info("Exploration complete!");
                
                // Get the closest creek to emergency site
                closestCreekId = gridSearch.getClosestCreek();
                
                // Log discoveries
                List<String> creeks = gridSearch.getCreekIds();
                List<String> sites = gridSearch.getSiteIds();
                
                logger.info("Found " + creeks.size() + " creeks and " + sites.size() + " emergency sites.");
                logger.info("Closest creek to emergency site: " + closestCreekId);
            }
        }
        // Phase 4: Exploration complete, stop the drone
        else {
            // We've completed our exploration, stop the drone
            action = this.drone.stop();
            logger.info("Mission complete, drone stopped.");
        }

        // Return value formatting
        this.previousActionType = action.getType();
        logger.info("Executing action: " + this.previousActionType + ", current position: " + position);
        return action.toString();
    }

    @Override
    public void acknowledgeResults(String s) {
        JSONObject jsonResponse = new JSONObject(new JSONTokener(new StringReader(s)));
        
        // Store the response for later processing
        lastResponse = jsonResponse;

        try {
            // Create a response object to easily access data
            Response response = new Response(jsonResponse);
            
            // Update drone position and direction from response
            updateDroneStatus(jsonResponse);
            Vector currentPosition = this.drone.getPosition();
            
            logger.info("The drone's coordinates are: " + currentPosition);
        } catch(Exception e) {
            System.err.println("Error processing response: " + e.getMessage());
        }
    }
    
    // Update drone position and heading based on game engine response
    private void updateDroneStatus(JSONObject jsonResponse) {
        try {
            // Get the extras object which contains the position data
            JSONObject extrasObj = jsonResponse.getJSONObject("extras");
            
            // Check if we have position data in the response
            if (extrasObj.has("position")) {
                JSONObject posObj = extrasObj.getJSONObject("position");
                int x = posObj.getInt("x");
                int y = posObj.getInt("y");
                Vector newPosition = new Vector(x, y);
                
                // Update drone position
                drone.setPosition(newPosition);
                logger.info("Updated drone position to: " + newPosition);
            }
            
            // Check if we have heading data and update if available
            if (extrasObj.has("heading")) {
                String headingStr = extrasObj.getString("heading");
                Heading newHeading = Heading.valueOf(headingStr);
                
                // Update drone heading
                drone.setDirection(newHeading);
                logger.info("Updated drone heading to: " + newHeading);
            }
        } catch (Exception e) {
            logger.warn("Could not update drone status: " + e.getMessage());
        }
    }

    @Override
    public String deliverFinalReport() {
        // If we found a closest creek, return it
        if (closestCreekId != null) {
            logger.info("Closest creek to emergency site is: " + closestCreekId);
            return closestCreekId;
        } 
        
        // If we found any creeks, return the first one
        List<String> creeks = gridSearch.getCreekIds();
        if (!creeks.isEmpty()) {
            String anyCreek = creeks.get(0);
            logger.info("Returning a creek, but couldn't find emergency site: " + anyCreek);
            return anyCreek;
        }
        
        // No creeks found
        logger.info("No creeks found during exploration!");
        return "No creeks found";
    }
}
