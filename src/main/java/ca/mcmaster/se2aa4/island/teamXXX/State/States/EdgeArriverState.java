package ca.mcmaster.se2aa4.island.teamXXX.State.States;

import ca.mcmaster.se2aa4.island.teamXXX.Action;
import ca.mcmaster.se2aa4.island.teamXXX.Drone.Drone;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Orientation;
import ca.mcmaster.se2aa4.island.teamXXX.Response.EchoResponse;
import ca.mcmaster.se2aa4.island.teamXXX.Response.Response;
import ca.mcmaster.se2aa4.island.teamXXX.State.State;

public class EdgeArriverState implements State {
    public enum Stage { FLY, ECHO }

    private Drone drone;
    private Stage stage;

    public EdgeArriverState(Drone drone) {
        this(drone, Stage.ECHO);
    }
    
    private EdgeArriverState(Drone drone, Stage stage) {
        this.drone = drone;
        this.stage = stage;
    }

    @Override
    public Action request() {
        switch (this.stage) {
            case FLY: return new Action(Action.Type.FLY);
            case ECHO: return new Action(Action.Type.ECHO).setParam("direction", this.drone.getHeading());
            default: throw new IllegalStateException("Unexpected stage: " + this.stage.toString());
        }
    }

    @Override 
    public State respond(Response response) {
        switch (this.stage) {
            case FLY:
                this.drone.fly(response.getCost());
                return new EdgeArriverState(this.drone, Stage.ECHO);
            
            case ECHO:
                this.drone.echo(response.getCost(), Orientation.FORWARD);

                EchoResponse echoResponse = (EchoResponse)response;
                Integer distance = echoResponse.getRange();

                if (distance > 0) {
                    return new EdgeArriverState(this.drone, Stage.FLY);
                } else {
                    return new StepScannerState(this.drone);
                }

            default: throw new IllegalStateException("Unexpected stage: " + this.stage.toString());
        }
    }

    @Override 
    public String getStatus() {
        return "State: " + this.getClass().getName() + ", Stage: " + this.stage.toString();
    }
}
