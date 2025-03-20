package ca.mcmaster.se2aa4.island.teamXXX.State.States;

import ca.mcmaster.se2aa4.island.teamXXX.Action;
import ca.mcmaster.se2aa4.island.teamXXX.Drone.Drone;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Orientation;
import ca.mcmaster.se2aa4.island.teamXXX.Response.EchoResponse;
import ca.mcmaster.se2aa4.island.teamXXX.Response.Response;
import ca.mcmaster.se2aa4.island.teamXXX.State.State;

public class ReverseTurnState implements State {
    public enum Stage { TURN1, FLY, TURN2 };

    private Drone drone;
    private Stage stage;
    private Orientation orientation;

    public ReverseTurnState(Drone drone, Orientation orientation) {
        this(drone, orientation, Stage.TURN1);
    }

    private ReverseTurnState(Drone drone, Orientation orientation, Stage stage) {
        this.drone = drone;
        this.orientation = orientation;
        this.stage = stage;
    }

    @Override
    public Action request() {
        switch (this.stage) {
            case TURN1: return new Action(Action.Type.ECHO).setParam("direction", this.drone.getHeading());
            case FLY: return new Action(Action.Type.FLY);
            case TURN2: return new Action(Action.Type.ECHO).setParam("direction", this.drone.getHeading());
            default: throw new IllegalStateException("Unexpected stage: " + this.stage.toString());
        }
    }

    @Override
    public State respond(Response response) {
        switch (this.stage) {
            case TURN1: 
                this.drone.echo(response.getCost(), Orientation.FORWARD);

                // Check if drone is safe to turn
                if (!this.drone.isSafeWithin(1)) {
                    return null;
                }
                
                State exitState1 = new ReverseTurnState(this.drone, this.orientation, Stage.FLY);
                return new SharpTurnState(this.drone, this.orientation, exitState1);
                
            case FLY: 
                this.drone.fly();

                if (this.drone.getPosition().x > this.drone.getMap()[0].length - 1) {
                    return null;
                }

                State exitState2 = new ReverseTurnState(this.drone, this.orientation, Stage.TURN2);
                return new SharpTurnState(this.drone, this.orientation, exitState2);
                
            case TURN2: 
                this.drone.echo(response.getCost(), Orientation.FORWARD);
                
                EchoResponse echoResponse = (EchoResponse)response;
                EchoResponse.Found found = echoResponse.getFound();
                
                // Check if drone is safe to turn
                if (!this.drone.isSafeWithin(1)) {
                    return null;
                }
                
                if (found == EchoResponse.Found.OUT_OF_RANGE) {
                    return null; // Stop plane
                } else {
                    return new EdgeArriverState(this.drone);
                }

            default: throw new IllegalStateException("Unexpected stage: " + this.stage.toString());
        }
    }

    @Override 
    public String getStatus() {
        return "State: " + this.getClass().getName() + ", Stage: " + this.stage.toString();
    }
}
