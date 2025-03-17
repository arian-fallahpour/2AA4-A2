package ca.mcmaster.se2aa4.island.teamXXX.Drone;

import org.json.JSONObject;

import ca.mcmaster.se2aa4.island.teamXXX.Action;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.ActionType;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Heading;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Orientation;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.RadarType;
import ca.mcmaster.se2aa4.island.teamXXX.Response.EchoResponse;
import ca.mcmaster.se2aa4.island.teamXXX.Response.Response;

/**
 * Island finder algorithm that uses alternating right/left echoes to find the island.
 * Once ground is detected, it performs a calculated maneuver to approach efficiently.
 */
public class IslandFinder {
    private enum State {
        INITIAL_ECHO,     // First echo to detect
        ECHO_ALTERNATE,   // Alternating between left and right echoes
        TURNING,          // Turning to the correct direction after finding the island
        APPROACHING,      // Flying toward the island
        ARRIVED,          // We've reached the island
        COMPLETED         // Finding process is done
    }
    
    private State currentState = State.INITIAL_ECHO;
    private Heading initialDirection;
    private Heading targetDirection;
    private boolean echoingRight = true;
    private int turningSteps = 0;
    private int approachDistance = 0;
    private int approachSteps = 0;
    private boolean groundDetected = false;
    
    // Constructor receives the initial direction the drone is facing
    public IslandFinder(Heading initialDirection) {
        this.initialDirection = initialDirection;
    }
    
    // Process the response from the last action and determine the next action
    public Action processResponse(JSONObject response) {
        Action nextActionObj = null;
        
        if (response != null) {
            try {
                // If we received a response, analyze it to update our state
                Response generalResponse = new Response(response);
                
                // Handle echo responses specifically
                if (response.has("extras") && response.getJSONObject("extras").has("found")) {
                    EchoResponse echoResponse = new EchoResponse(response);
                    
                    // Check if we got a ground reading - this means we found the island
                    if (echoResponse.getReading() == RadarType.GROUND) {
                        // We found the island!
                        groundDetected = true;
                        approachDistance = echoResponse.getRange();
                        
                        // Determine which direction we need to face to approach the island
                        if (echoingRight) {
                            targetDirection = initialDirection.right();
                        } else {
                            targetDirection = initialDirection.left();
                        }
                        
                        // Start turning phase
                        currentState = State.TURNING;
                        turningSteps = 0;
                    }
                }
                
                // Check for heading changes from the game engine
                if (response.has("extras") && response.getJSONObject("extras").has("heading")) {
                    // Update our current direction based on engine response
                    String headingStr = response.getJSONObject("extras").getString("heading");
                    Heading currentHeading = Heading.valueOf(headingStr);
                    initialDirection = currentHeading;
                }
            } catch (Exception e) {
                System.err.println("Error processing island finder response: " + e.getMessage());
            }
        }
        
        // Now determine the next action based on our current state
        switch (currentState) {
            case INITIAL_ECHO:
                // Initial echo is to the right
                nextActionObj = handleInitialEcho();
                break;
                
            case ECHO_ALTERNATE:
                // Alternate between echoing left and right until ground is found
                nextActionObj = handleEchoAlternate();
                break;
                
            case TURNING:
                // Turn to face the correct direction to approach the island
                nextActionObj = handleTurning();
                break;
                
            case APPROACHING:
                // Fly toward the island
                nextActionObj = handleApproaching();
                break;
                
            case ARRIVED:
                // We've reached the island, now we can scan it
                nextActionObj = new Action(ActionType.SCAN);
                currentState = State.COMPLETED;
                break;
                
            case COMPLETED:
                // Done with the finding process
                return null;
        }
        
        return nextActionObj;
    }
    
    // First echo to the right
    private Action handleInitialEcho() {
        // First action, send echo to the right
        Action echoAction = new Action(ActionType.ECHO);
        echoAction.setParameter("direction", initialDirection.right());
        currentState = State.ECHO_ALTERNATE;
        return echoAction;
    }
    
    // Handle alternating echoes
    private Action handleEchoAlternate() {
        // If we already found ground, move to the turning state
        if (groundDetected) {
            currentState = State.TURNING;
            return handleTurning();
        }
        
        // Toggle between echoing right and left
        echoingRight = !echoingRight;
        Orientation echoDir = echoingRight ? Orientation.RIGHT : Orientation.LEFT;
        Heading echoHeading = echoDir.oriente(initialDirection);
        
        Action nextAction = new Action(ActionType.ECHO);
        nextAction.setParameter("direction", echoHeading);
        
        return nextAction;
    }
    
    // Handle turning toward the island
    private Action handleTurning() {
        turningSteps++;
        
        if (turningSteps == 1) {
            // Turn to face the island
            Action turnAction = new Action(ActionType.HEADING);
            turnAction.setParameter("direction", targetDirection);
            return turnAction;
        } else {
            // We've already turned, now start approaching
            currentState = State.APPROACHING;
            approachSteps = 0;
            return handleApproaching();
        }
    }
    
    // Handle flying toward the island
    private Action handleApproaching() {
        approachSteps++;
        
        if (approachSteps >= approachDistance) {
            // We've reached the island
            currentState = State.ARRIVED;
            return new Action(ActionType.SCAN);
        }
        
        // Keep flying toward the island
        return new Action(ActionType.FLY);
    }
    
    // Check if the island finding process is complete
    public boolean isCompleted() {
        return currentState == State.COMPLETED;
    }
    
    // Check if the island has been found but we haven't arrived yet
    public boolean islandFound() {
        return groundDetected;
    }
}
