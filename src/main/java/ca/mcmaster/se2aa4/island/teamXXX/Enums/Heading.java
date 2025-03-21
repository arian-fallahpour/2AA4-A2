package ca.mcmaster.se2aa4.island.teamXXX.Enums;

import ca.mcmaster.se2aa4.island.teamXXX.Vector;

// Represents the four cardinal directions for drone navigation
public enum Heading {
    N, E, S, W;

    // Turns the heading based on the provided orientation
    public Heading turn(Orientation orientation) {
        switch (orientation) {
            case Orientation.LEFT: return this.left();
            case Orientation.RIGHT: return this.right();
            default: return this;
        }
    }

    // Returns the heading after a 90-degree clockwise turn
    public Heading right() {
        switch (this) {
            case N: return E; 
            case E: return S; 
            case S: return W; 
            case W: return N; 
            default: throw new IllegalArgumentException(String.format("Unexpected heading value: %s", this));
        }
    }

    // Returns the heading after a 90-degree counterclockwise turn
    public Heading left() {
        switch (this) {
            case N: return W; 
            case E: return N; 
            case S: return E; 
            case W: return S; 
            default: throw new IllegalArgumentException(String.format("Unexpected heading value: %s", this));
        }
    }

    // Returns the opposite direction (180-degree turn)
    public Heading opposite() {
        switch (this) {
            case N: return S;
            case E: return W;
            case S: return N;
            case W: return E;
            default: throw new IllegalArgumentException(String.format("Unexpected heading value: %s", this));
        }
    }

    // Converts the heading to a unit vector for movement calculations
    public Vector toVector() {
        switch (this) {
            case N: return new Vector(0, -1);
            case E: return new Vector(1, 0);
            case S: return new Vector(0, 1);
            case W: return new Vector(-1, 0);
            default: throw new IllegalArgumentException(String.format("Unexpected heading value: %s", this));
        }
    }
}
