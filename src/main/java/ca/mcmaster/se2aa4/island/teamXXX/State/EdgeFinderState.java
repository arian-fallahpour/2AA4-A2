package ca.mcmaster.se2aa4.island.teamXXX.State;

import org.json.JSONObject;

import ca.mcmaster.se2aa4.island.teamXXX.Action;
import ca.mcmaster.se2aa4.island.teamXXX.Drone.Drone;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Orientation;
import ca.mcmaster.se2aa4.island.teamXXX.Response.EchoResponse;
import ca.mcmaster.se2aa4.island.teamXXX.Response.Response;

public class EdgeFinderState implements State {
    public enum Stage { RIGHT_TURN, LEFT_TURN, ECHO, FLY };

    private Drone drone;
    private Stage stage;

    private final Orientation echoOrientation = Orientation.RIGHT;

    public EdgeFinderState(Drone drone) {
        this(drone, Stage.RIGHT_TURN);
    }

    private EdgeFinderState(Drone drone, Stage stage) {
        this.drone = drone;
        this.stage = stage;
    }

    @Override
    public Action request() {
        switch (this.stage) {
            case RIGHT_TURN: return new Action(Action.Type.HEADING).setParam("direction", this.drone.getHeading().right());
            case LEFT_TURN: return new Action(Action.Type.HEADING).setParam("direction", this.drone.getHeading().left());
            case ECHO: return new Action(Action.Type.ECHO).setParam("direction", this.echoOrientation.orient(this.drone.getHeading()));
            case FLY: return new Action(Action.Type.FLY);
            default: throw new IllegalStateException("Unexpected stage: " + this.stage.toString());
        }
    }

    @Override
    public State respond(Response response) {
        switch (this.stage) {
            case RIGHT_TURN: return this.respondRightTurn(response);
            case LEFT_TURN: return this.respondLeftTurn(response);
            case ECHO: return this.respondEcho(response);
            case FLY: return this.respondFly(response);
            default: throw new IllegalStateException("Unexpected stage: " + this.stage.toString());
        }
    }

    private State respondRightTurn(Response response) {
        this.drone.turn(response.getCost(), Orientation.RIGHT);
        return new EdgeFinderState(this.drone, Stage.LEFT_TURN);
    }

    private State respondLeftTurn(Response response) {
        this.drone.turn(response.getCost(), Orientation.LEFT);
        return new EdgeFinderState(this.drone, Stage.ECHO);
    }

    private State respondEcho(Response response) {
        this.drone.echo(response.getCost());

        EchoResponse echoResponse = (EchoResponse)response;
        EchoResponse.Found found = echoResponse.getFound();

        if (found == EchoResponse.Found.OUT_OF_RANGE) {
            return new EdgeFinderState(this.drone, Stage.FLY);
        } else {
            State exitState = new EdgeArriverState(this.drone);
            return new SharpTurnState(this.drone, this.echoOrientation, exitState);
        }
    }
    private State respondFly(Response response) {
        this.drone.fly(response.getCost());
        return new EdgeFinderState(this.drone, Stage.ECHO);
    }
}
