package ca.mcmaster.se2aa4.island.teamXXX.Enums;

public enum Orientation {
    LEFT, RIGHT, FORWARD;

    public Heading oriente(Heading heading) {
        switch (this) {
            case LEFT: return heading.left();
            case RIGHT: return heading.right();
            case FORWARD: return heading;
            default: throw new IllegalArgumentException(String.format("Incorrect orientation %s", this));
        }
    }
}
