package ca.mcmaster.se2aa4.island.teamXXX.Drone;

import ca.mcmaster.se2aa4.island.teamXXX.Enums.Heading;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Orientation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ca.mcmaster.se2aa4.island.teamXXX.Vector;

//drone class that handles all drone movement and sensing operations
public class Drone {
    private final Logger logger = LogManager.getLogger();

    private Heading heading; // Direction as a Heading
    private Vector position; // Position as a Vector
    private Battery battery;
    private Integer[][] map;

    private Integer step = 1;

    public Drone(Integer charge, Heading heading) {
        this.heading = heading;
        this.position = new Vector(0, 0);
        this.battery = new Battery(charge);
    }

    /*
     * Calibrates drone's map
     */
    public void calibrate(Integer rows, Integer cols) {
        logger.info("Drone map calibrated with " + rows + " rows and " + cols + " columns");
        this.map = new Integer[rows][cols];
    }

    /*
     * Moves drones in direction of its heading
     */
    public void fly(Integer cost) {
        this.battery.drain(cost);
        this.position = this.position.add(this.heading.toVector().multiply(this.step));
    }

    public void fly() {
        this.fly(0);
    }

    /*
     * Turns in a non in-place manner, as defined in game engine documentation
     */
    public void turn(Integer cost, Orientation orientiation) {
        if (orientiation == Orientation.FORWARD) {
            throw new IllegalArgumentException("You cannot turn forwards");
        }

        this.battery.drain(cost);
        this.fly();
        this.heading = orientiation.orient(this.heading);
        this.fly();
    }

    public void echo(Integer cost, Orientation orientiation) {
        this.battery.drain(cost);
    }

    public void scan(Integer cost) {
        this.battery.drain(cost);
    }
    
    /*
     * Returns true if the plane will be on the first/last tile of the map when moving forward
     */
    public Boolean willHitBorderAfterFlying(Integer step) {
        switch (this.heading) {
            case N: return this.position.y - step < 1;
            case E: return this.position.x + step >= map.length;
            case S: return this.position.y + step >= map[0].length + 1;
            case W: return this.position.x - step < 0;
            default: return false;
        }
    }

    public Boolean willHitBorderAfterFlying() {
        return this.willHitBorderAfterFlying(this.step);
    }
    
    // Get the drone's current coordinates
    public Vector getPosition() {
        return this.position.copy();
    }
    
    // Gets the drone's current direction as heading
    public Heading getHeading() {
        return Heading.valueOf(this.heading.toString());
    }
    
    // Gets the drone's current direction as vector
    public Vector getDirection() {
        return this.heading.toVector();
    }

    // Gets the drone's battery
    public Battery getBattery() {
        return new Battery(this.battery.getCharge());
    }
    
    // Gets the drone's map
    public Integer[][] getMap() {
        return this.map;
    }
    
    // Set the drone's position (to be called from Explorer when the engine updates)
    public void setPosition(Vector newPosition) {
        this.position = newPosition;
    }
    
    // Set the drone's direction (to be called from Explorer when the engine updates)
    public void setHeading(Heading newHeading) {
        this.heading = newHeading;
    }
    
    //get a string representation of the drone's current state
    public String getStatus() {
        return "Position: (" + this.position.x + "," + this.position.y + "), " +
               "Heading: " + this.heading + ", " +
               "Battery: " + this.battery.getCharge();
    }
}