package ca.mcmaster.se2aa4.island.teamXXX.State;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ca.mcmaster.se2aa4.island.teamXXX.Action;
import ca.mcmaster.se2aa4.island.teamXXX.Drone.Drone;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Heading;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Orientation;
import ca.mcmaster.se2aa4.island.teamXXX.Response.EchoResponse;
import ca.mcmaster.se2aa4.island.teamXXX.Response.Response;

public class BorderArriverState implements State{
    private final Logger logger = LogManager.getLogger();

    public enum Stage { ECHO, FLY };

    private Drone drone;
    private Stage stage;

    private final Integer minDistanceBeforeReversing = 3;

    public BorderArriverState(Drone drone) {
        this(drone, Stage.ECHO);
    }

    private BorderArriverState(Drone drone, Stage stage) {
        this.drone = drone;
        this.stage = stage;
    }

    @Override
    public Action request() {
        switch (this.stage) {
            case ECHO: return new Action(Action.Type.ECHO).setParam("direction", this.drone.getHeading());
            case FLY: return new Action(Action.Type.FLY);
            default: throw new IllegalStateException("Unexpected stage: " + this.stage.toString());
        }
    }

    @Override 
    public State respond(Response response) {
        switch (this.stage) {
            case ECHO: return this.respondEcho(response);
            case FLY: return this.respondFly(response);
            default: throw new IllegalStateException("Unexpected stage: " + this.stage.toString());
        }
    }

    private State respondEcho(Response response) {
        this.drone.echo(response.getCost(), Orientation.FORWARD);

        EchoResponse echoResponse = (EchoResponse)response;
        Integer distance = echoResponse.getRange();

        if (distance > minDistanceBeforeReversing) {
            return new BorderArriverState(this.drone, Stage.FLY);
        } else {
            return new ReverseTurnState(this.drone, this.getReverseTurnOrientation());
        }
    }

    private State respondFly(Response response) {
        this.drone.fly(response.getCost());
        return new BorderArriverState(this.drone, Stage.ECHO);
    }

    private Orientation getReverseTurnOrientation() {
        return this.drone.getHeading() == Heading.S ? Orientation.LEFT : Orientation.RIGHT;
    }

    @Override 
    public String getStatus() {
        return "State: " + this.getClass().getName() + ", Stage: " + this.stage.toString();
    }
}
