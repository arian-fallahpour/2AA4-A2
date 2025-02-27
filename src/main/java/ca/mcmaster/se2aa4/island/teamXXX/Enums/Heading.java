package ca.mcmaster.se2aa4.island.teamXXX.Enums;

public enum Heading {
    N, E, S, W; // North, East, South, West

    public Heading right() {
        Heading result;

        switch (this) {
            case N:
                result = E; 
                break;
            case E:
                result = S; 
                break;
            case S:
                result = W; 
                break;
            case W:
                result = N; 
                break;
            default: throw new IllegalArgumentException(String.format("Unexpected heading value: %s", this));
        }

        return result;
    }

    public Heading left() {
        Heading result;

        switch (this) {
            case N:
                result = W; 
                break;
            case E:
                result = N; 
                break;
            case S:
                result = E; 
                break;
            case W:
                result = S; 
                break;
            default: throw new IllegalArgumentException(String.format("Unexpected heading value: %s", this));
        }

        return result;
    }

    public Heading reverse() {
        return this.right().right();
    }
}
