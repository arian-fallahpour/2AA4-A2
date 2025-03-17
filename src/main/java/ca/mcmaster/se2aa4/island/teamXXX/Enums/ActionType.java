package ca.mcmaster.se2aa4.island.teamXXX.Enums;

public enum ActionType {
    FLY, ARRIVE, HEADING, SCAN, ECHO, STOP;

    public ActionType next(){
        if (this == FLY) {
            return ARRIVE;
        } else if (this == ARRIVE) {
            return SCAN;
        } else {
            return SCAN;
        }
    }
}
