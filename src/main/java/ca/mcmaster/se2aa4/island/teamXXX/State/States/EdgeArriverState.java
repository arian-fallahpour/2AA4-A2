package ca.mcmaster.se2aa4.island.teamXXX.State.States;

import ca.mcmaster.se2aa4.island.teamXXX.Action;
import ca.mcmaster.se2aa4.island.teamXXX.Drone.Drone;
import ca.mcmaster.se2aa4.island.teamXXX.Response.Response;
import ca.mcmaster.se2aa4.island.teamXXX.State.State;

public class EdgeArriverState implements State {
    public enum Stage { FLY }

    private Drone drone;
    private Stage stage;
    private Integer distance;

    public EdgeArriverState(Drone drone, Integer distance) {
        this(drone, distance, Stage.FLY);
    }
    
    private EdgeArriverState(Drone drone, Integer distance, Stage stage) {
        this.drone = drone;
        this.stage = stage;
        this.distance = distance;
    }

    @Override
    public Action request() {
        switch (this.stage) {
            case FLY: return new Action(Action.Type.FLY);
            default: throw new IllegalStateException("Unexpected stage: " + this.stage.toString());
        }
    }

    @Override 
    public State respond(Response response) {
        switch (this.stage) {
            case FLY:
                if (this.distance > 0) {
                    this.drone.fly(response.getCost());
                    return new EdgeArriverState(this.drone, this.distance - 1);
                } else {
                    return new StepScannerState(this.drone);
                }

            default: throw new IllegalStateException("Unexpected stage: " + this.stage.toString());
        }
    }

}
