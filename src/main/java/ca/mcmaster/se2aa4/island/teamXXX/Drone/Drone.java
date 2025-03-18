package ca.mcmaster.se2aa4.island.teamXXX.Drone;

import ca.mcmaster.se2aa4.island.teamXXX.Action;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.ActionType;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Heading;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Orientation;
import ca.mcmaster.se2aa4.island.teamXXX.Vector;

//drone class that handles all drone movement and sensing operations
public class Drone {
    private Heading direction; // Direction as a Heading
    private Vector position; // Position as a Vector
    private Battery battery;
    private ActionType state = ActionType.FLY;

    public Drone(Integer battery, Heading direction) {
        this.direction = direction;
        this.position = new Vector(0, 0);
        this.battery = new Battery(battery);
    }

    // Creates a fly action to move the drone forward
    public Action fly() {
        // Don't update position here - the game engine will do this
        return new Action(ActionType.FLY);
    }

    // Creates a turn action to change direction
    public Action turn(Orientation orientation) {
        if (orientation.equals(Orientation.FORWARD)) {
            throw new IllegalArgumentException("Orientation can only be LEFT or RIGHT");
        }

        // Don't update position/direction here - the game engine will do this
        Action action = new Action(ActionType.HEADING);
        Heading newDirection = orientation.oriente(this.direction);
        action.setParameter("direction", newDirection);
        
        return action;
    }
    
    // Creates an echo action towards the orientation
    public Action echo(Orientation orientation) {
        Heading echoDirection = orientation.oriente(this.direction);

        Action action = new Action(ActionType.ECHO);
        action.setParameter("direction", echoDirection);
        
        return action;
    }
    
    // Creates a scan action to scan the current location
    public Action scan() {
        return new Action(ActionType.SCAN);
    }
    
    // Creates a stop action to end the mission
    public Action stop() {
        return new Action(ActionType.STOP);
    }
 
    
    // Get the drone's current coordinates
    public Vector getPosition() {
        return this.position.copy();
    }
    
    // Gets the drone's current direction as heading
    public Heading getHeading() {
        return Heading.valueOf(this.direction.toString());
    }
    
    
    // Gets the drone's current direction as vector
    public Vector getDirection() {
        return this.direction.toVector();
    }

    public Battery getBattery() {
        return new Battery(this.battery.getCharge());
    }
    
    // Set the drone's position (to be called from Explorer when the engine updates)
    public void setPosition(Vector newPosition) {
        this.position = newPosition;
    }
    
    // Set the drone's direction (to be called from Explorer when the engine updates)
    public void setDirection(Heading newDirection) {
        this.direction = newDirection;
    }
    
    //get a string representation of the drone's current state
    public String getStatus() {
        return "Position: (" + this.position.x + "," + this.position.y + "), " +
               "Heading: " + direction + ", " +
               "Battery: " + this.battery.getCharge();
    }
}