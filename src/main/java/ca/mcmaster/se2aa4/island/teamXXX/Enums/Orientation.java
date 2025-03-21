package ca.mcmaster.se2aa4.island.teamXXX.Enums;

// Represents the possible turning directions for the drone
public enum Orientation {
    LEFT, RIGHT, FORWARD;

    public Heading orient(Heading heading) {
        switch (this) {
            case LEFT: return heading.left();
            case RIGHT: return heading.right();
            case FORWARD: return heading;
            default: throw new IllegalArgumentException(String.format("Incorrect orientation %s", this));
        }
    }
}
