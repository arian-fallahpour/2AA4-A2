package ca.mcmaster.se2aa4.island.teamXXX.Drone;

import java.util.ArrayList;

import ca.mcmaster.se2aa4.island.teamXXX.Enums.Heading;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Orientation;
import ca.mcmaster.se2aa4.island.teamXXX.Vector;


public class Drone {
    private Heading heading; // Direction as a Heading
    private Vector position; // Position as a Vector
    private Battery battery;
    private Storage storage;
    private Map map;

    private final Integer step = 1;

    public Drone(Integer charge, Vector position, Heading heading) {
        this.heading = heading;
        this.position = position;
        this.battery = new Battery(charge);
        this.storage = new Storage();
    }

    public void calibrate(Integer rows, Integer cols) {
        this.map = new Map(rows, cols);
    }

    public void fly(Integer cost) {
        this.battery.drain(cost);
        this.position = this.position.add(this.heading.toVector().multiply(this.step));
    }
    private void fly() { this.fly(0); }

    public void turn(Integer cost, Orientation orientiation) {
        if (orientiation == Orientation.FORWARD) {
            throw new IllegalArgumentException("You cannot turn forwards");
        }

        this.fly();
        this.heading = orientiation.orient(this.heading);
        this.fly();

        this.battery.drain(cost);
    }

    public void echo(Integer cost) {
        this.battery.drain(cost);
    }

    public void scan(Integer cost) {
        this.battery.drain(cost);
    }

    public void stop(Integer cost) {
        this.battery.drain(cost);
    }

    // Get the drone's current coordinates
    public Vector getPosition() {
        return this.position.clone();
    }
    
    // Gets the drone's current direction as heading
    public Heading getHeading() {
        return Heading.valueOf(this.heading.toString());
    }
    
    // Get a string representation of the drone's current state
    public String getStatus() {
        return "Position: " + this.position.toString() + ", " + "Heading: " + this.heading + ", " + "Battery: " + this.battery.getCharge();
    }

    public Integer getCharge() {
        return this.battery.getCharge();
    }

    // Returns true if border is not within the specified range from the drone
    public Boolean isSafeWithin(Integer range) {
        Integer rows = this.map.getRows();
        Integer cols = this.map.getCols();

        if (this.position.x < range) return false;
        if (this.position.x > rows - range) return false;
        if (this.position.y < range) return false;
        if (this.position.y > cols - range) return false;

        return true;
    }

    public void saveCreeks(ArrayList<String> creekIds) {
        for (int i = 0; i < creekIds.size(); i++) { 
            this.saveCreek(creekIds.get(i));
        }
    }

    public void saveCreek(String creekId) {
        POI poi = new POI(POI.Type.CREEK, creekId, this.position.clone());
        this.storage.saveCreek(poi);
    }

    public void saveSite(String siteId) {
        POI poi = new POI(POI.Type.SITE, siteId, this.position.clone());
        this.storage.saveSite(poi);
    }
}