package ca.mcmaster.se2aa4.island.teamXXX.State;

import ca.mcmaster.se2aa4.island.teamXXX.Action;
import ca.mcmaster.se2aa4.island.teamXXX.Drone.Drone;
import ca.mcmaster.se2aa4.island.teamXXX.Response.Response;

// Final state that safely ends the mission by stopping the drone
public class LandingState implements State {
    private Drone drone;

    public LandingState(Drone drone) {
        this.drone = drone;
    }

    @Override
    public Action request() {
        return new Action(Action.Type.STOP);
    }

    @Override
    public State respond(Response response) {
        this.drone.stop(response.getCost());
        return null;
    }
}
