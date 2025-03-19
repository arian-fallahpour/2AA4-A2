package ca.mcmaster.se2aa4.island.teamXXX.State.States;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ca.mcmaster.se2aa4.island.teamXXX.Action;
import ca.mcmaster.se2aa4.island.teamXXX.Drone.Drone;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Orientation;
import ca.mcmaster.se2aa4.island.teamXXX.Response.EchoResponse;
import ca.mcmaster.se2aa4.island.teamXXX.Response.Response;
import ca.mcmaster.se2aa4.island.teamXXX.State.State;

public class ReverseTurnState implements State {
    private final Logger logger = LogManager.getLogger();

    public enum Stage { TURN1, TURN2, EXIT };

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
            case TURN2: return new Action(Action.Type.FLY);
            case EXIT: return new Action(Action.Type.ECHO).setParam("direction", this.drone.getHeading());
            default: throw new IllegalStateException("Unexpected stage: " + this.stage.toString());
        }
    }

    @Override
    public State respond(Response response) {
        switch (this.stage) {
            case TURN1: 
                this.drone.echo(response.getCost(), Orientation.FORWARD);
                
                State exitState1 = new ReverseTurnState(this.drone, this.orientation, Stage.TURN2);
                return new SharpTurnState(this.drone, this.orientation, exitState1);
                
            case TURN2: 
                this.drone.fly(response.getCost());
                
                State exitState2 = new ReverseTurnState(this.drone, this.orientation, Stage.EXIT);
                return new SharpTurnState(this.drone, this.orientation, exitState2);
                
            case EXIT:
                this.drone.echo(response.getCost(), Orientation.FORWARD);

                EchoResponse echoResponse = (EchoResponse)response;
                Integer distance = echoResponse.getRange();
                EchoResponse.Found found = echoResponse.getFound();

                if (found == EchoResponse.Found.OUT_OF_RANGE) {
                    return null;
                } else {
                    return new EdgeArriverState(this.drone, distance);
                }

            default: throw new IllegalStateException("Unexpected stage: " + this.stage.toString());
        }
    }
}
