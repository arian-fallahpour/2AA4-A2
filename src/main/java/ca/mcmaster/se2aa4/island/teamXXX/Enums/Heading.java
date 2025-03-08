package ca.mcmaster.se2aa4.island.teamXXX.Enums;

public enum Heading {
    N, E, S, W; // North, East, South, West

    public Heading turn(Orientation orientation) {
        switch (orientation) {
            case Orientation.LEFT: return this.left();
            case Orientation.RIGHT: return this.left();
            default:  return this;
        }
    }

    public Heading right() {
        switch (this) {
            case N: return E; 
            case E: return S; 
            case S: return W; 
            case W: return N; 
            default: throw new IllegalArgumentException(String.format("Unexpected heading value: %s", this));
        }
    }

    public Heading left() {
        switch (this) {
            case N: return W; 
            case E: return N; 
            case S: return E; 
            case W: return S; 
            default:  throw new IllegalArgumentException(String.format("Unexpected heading value: %s", this));
        }
    }

    public Heading reverse() {
        return this.right().right();
    }
}
