package ca.mcmaster.se2aa4.island.teamXXX.Enums;

public enum ActionType {
    FLY, HEADING, SCAN, ECHO;

    @Override
    public String toString() {
        return this.toString().toLowerCase();
    }
}
