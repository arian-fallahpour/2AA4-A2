package ca.mcmaster.se2aa4.island.teamXXX.Drone;

import ca.mcmaster.se2aa4.island.teamXXX.Enums.Heading;


import org.json.JSONObject;

//drone class that handles all drone movement and sensing operations
public class Drone {
    private Heading direction;
    private final Heading initialDirection;
    private Integer batteryLevel;
    private Double x = 0.0;
    private Double y = 0.0;
    private JSONObject decision;
    private JSONObject parameters;
    private Battery battery;
    private Radar radar;

    public Drone(Integer battery, Heading direction) {
        this.batteryLevel = battery;
        this.battery = new Battery(battery);
        this.direction = direction;
        this.initialDirection = direction;
        this.decision = new JSONObject();
        this.parameters = new JSONObject();
        this.radar = new Radar();
    }
    
    //creates a fly action to move the drone forward
    //updates position tracking
    public JSONObject fly() {
        // Update coordinates based on direction
        updateCoords();
        
        // Create the fly action
        decision = new JSONObject();
        decision.put("action", "fly");
        
        return decision;
    }
    
    //turn the drone right (90 degrees clockwise)
    public JSONObject turnRight() {
        // Determine new heading
        Heading newHeading = getRightHeading(direction);
        
        //update direction
        this.direction = newHeading;
        
        //create the heading action
        decision = new JSONObject();
        parameters = new JSONObject();
        parameters.put("direction", direction.toString());
        decision.put("parameters", parameters);
        decision.put("action", "heading");
        
        return decision;
    }
    
    //turn the drone left (90 degrees counter-clockwise)
    public JSONObject turnLeft() {
        // Determine new heading
        Heading newHeading = getLeftHeading(direction);
        
        // Update direction
        this.direction = newHeading;
        
        // Create the heading action
        decision = new JSONObject();
        parameters = new JSONObject();
        parameters.put("direction", direction.toString());
        decision.put("parameters", parameters);
        decision.put("action", "heading");
        
        return decision;
    }
    
    //creates a scan action to scan the current location
    public JSONObject scan() {
        decision = new JSONObject();
        decision.put("action", "scan");
        return decision;
    }
    
    //creates a stop action to end the mission
    public JSONObject stop() {
        decision = new JSONObject();
        decision.put("action", "stop");
        return decision;
    }
    
    //echo forward (in the direction the drone is facing)
    public JSONObject echoFwd() {
        return radar.echoFwd(direction);
    }
    
    //echo right (90 degrees clockwise from current heading)
    public JSONObject echoRight() {
        return radar.echoRight(direction);
    }
    
    //echo left (90 degrees counter-clockwise from current heading)
    public JSONObject echoLeft() {
        return radar.echoLeft(direction);
    }
    
    //update coordinates based on the current direction when moving
    private void updateCoords() {
        switch (direction) {
            case N:
                this.y += 1.0;
                break;
            case E:
                this.x += 1.0;
                break;
            case S:
                this.y -= 1.0;
                break;
            case W:
                this.x -= 1.0;
                break;
        }
    }
    
    //get the heading to the left of the current heading
    private Heading getLeftHeading(Heading current) {
        switch (current) {
            case N: return Heading.W;
            case E: return Heading.N;
            case S: return Heading.E;
            case W: return Heading.S;
            default: return current;
        }
    }
    
    //get the heading to the right of the current heading
    private Heading getRightHeading(Heading current) {
        switch (current) {
            case N: return Heading.E;
            case E: return Heading.S;
            case S: return Heading.W;
            case W: return Heading.N;
            default: return current;
        }
    }
    
    //get the drone's current coordinates
    public Double[] coords() {
        Double[] arr = {this.x, this.y};
        return arr;
    }
    
    //get the drone's current direction
    public Heading getDirection() {
        return direction;
    }
    
    //get the drone's battery
    public Battery getBattery() {
        return battery;
    }
    
    //update the battery level
    public void updateBattery(int cost) {
        this.batteryLevel -= cost;
        this.battery.drain(cost);
    }
    
    //get battery level
    public Integer getBatteryLevel() {
        return batteryLevel;
    }
    
    //get current x position
    public Double getX() {
        return x;
    }
    
    //get current y position
    public Double getY() {
        return y;
    }
    
    //perform a safe turn sequence to prevent 180-degree turns
    public JSONObject safeTurn(Heading targetHeading) {
        // Check if it would be a 180-degree turn
        if (isOppositeHeading(direction, targetHeading)) {
            // Use intermediate turn
            Heading intermediate = getIntermediateHeading(direction, targetHeading);
            direction = intermediate;
            
            // Create the heading action
            decision = new JSONObject();
            parameters = new JSONObject();
            parameters.put("direction", direction.toString());
            decision.put("parameters", parameters);
            decision.put("action", "heading");
            
            return decision;
        } else {
            // Direct turn
            direction = targetHeading;
            
            // Create the heading action
            decision = new JSONObject();
            parameters = new JSONObject();
            parameters.put("direction", direction.toString());
            decision.put("parameters", parameters);
            decision.put("action", "heading");
            
            return decision;
        }
    }
    
    //check if two headings are opposite (would cause a 180-degree turn)
    private boolean isOppositeHeading(Heading h1, Heading h2) {
        return (h1 == Heading.N && h2 == Heading.S) ||
               (h1 == Heading.S && h2 == Heading.N) ||
               (h1 == Heading.E && h2 == Heading.W) ||
               (h1 == Heading.W && h2 == Heading.E);
    }
    
    //get an intermediate heading to avoid 180-degree turns
    private Heading getIntermediateHeading(Heading current, Heading target) {
        if (current == Heading.N || current == Heading.S) {
            return Heading.E;
        } else {
            return Heading.N;
        }
    }
    
    //get a string representation of the drone's current state
    public String getStatus() {
        return "Position: (" + x + "," + y + "), " +
               "Heading: " + direction + ", " +
               "Battery: " + batteryLevel;
    }
}
