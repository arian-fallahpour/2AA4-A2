package ca.mcmaster.se2aa4.island.teamXXX.State.States;

import ca.mcmaster.se2aa4.island.teamXXX.Action;
import ca.mcmaster.se2aa4.island.teamXXX.Drone.Drone;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Heading;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Orientation;
import ca.mcmaster.se2aa4.island.teamXXX.Response.Response;
import ca.mcmaster.se2aa4.island.teamXXX.State.State;

public class BorderArriverState implements State{
    public enum Stage { FLY, REVERSE };

    private Drone drone;
    private Integer distance;
    private Stage stage;

    private final Integer minDistanceBeforeReversing = 3;

    public BorderArriverState(Drone drone, Integer distance) {
        this(drone, distance, Stage.FLY);
    }

    private BorderArriverState(Drone drone, Integer distance, Stage stage) {
        this.drone = drone;
        this.distance = distance;
        this.stage = stage;
    }

    @Override
    public Action request() {
        switch (this.stage) {
            case FLY: return new Action(Action.Type.FLY);
            case REVERSE: return new Action(Action.Type.ECHO).setParam("direction", this.drone.getHeading());
            default: throw new IllegalStateException("Unexpected stage: " + this.stage.toString());
        }
    }

    @Override 
    public State respond(Response response) {
        switch (this.stage) {
            case FLY:
                this.drone.fly(response.getCost());

                if (this.distance > minDistanceBeforeReversing) {
                    return new BorderArriverState(this.drone, this.distance - 1, Stage.FLY);
                } else {
                    return new BorderArriverState(this.drone, this.distance, Stage.REVERSE);
                }

            case REVERSE: 
                this.drone.echo(response.getCost(), Orientation.FORWARD);
                return new ReverseTurnState(this.drone, this.getReverseTurnOrientation());

            default: throw new IllegalStateException("Unexpected stage: " + this.stage.toString());
        }
    }

    private Orientation getReverseTurnOrientation() {
        return this.drone.getHeading() == Heading.S ? Orientation.LEFT : Orientation.RIGHT;
    }
}
