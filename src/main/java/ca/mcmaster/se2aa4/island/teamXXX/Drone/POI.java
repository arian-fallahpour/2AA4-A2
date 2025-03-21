package ca.mcmaster.se2aa4.island.teamXXX.Drone;

import ca.mcmaster.se2aa4.island.teamXXX.Vector;

// Point of Interest class that represents discovered locations like creeks and emergency sites
public class POI {
    public enum Type { CREEK, SITE };

    private Type type;
    private String id;
    private Vector position;

    public POI(Type type, String id, Vector position) {
        this.type = type;
        this.id = id;
        this.position = position;
    }

    public Type getType() { return this.type; }
    public String getId() { return this.id; }
    public Vector getPosition() { return this.position; }
}
