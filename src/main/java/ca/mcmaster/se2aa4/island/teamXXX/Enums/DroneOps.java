package ca.mcmaster.se2aa4.island.teamXXX.Enums;

import ca.mcmaster.se2aa4.island.teamXXX.Drone.Drone;

public enum DroneOps {
     LOOKING, REACHING, SEARCHING;

    public DroneOps nextOp(Drone drone) {
        if (this == LOOKING) {
            return REACHING;
        } else if (this == REACHING) {
            return SEARCHING;
        } else {
            return SEARCHING;
        }
    }
}
