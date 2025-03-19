package ca.mcmaster.se2aa4.island.teamXXX.State.States;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ca.mcmaster.se2aa4.island.teamXXX.Action;
import ca.mcmaster.se2aa4.island.teamXXX.Drone.Drone;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Heading;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Orientation;
import ca.mcmaster.se2aa4.island.teamXXX.Response.Response;
import ca.mcmaster.se2aa4.island.teamXXX.State.State;

public class SharpTurnState implements State {
    private final Logger logger = LogManager.getLogger();

    public enum Stage { STEP, TURN, EXIT };

    private Drone drone;
    private Orientation orientation;
    private State exitState;
    private Stage stage;
    private Integer turns;

    private final Integer maxTurnsBeforeExit = 2;

    public SharpTurnState(Drone drone, Orientation orientation, State exitState) {
        this(
            new SharpTurnState.Builder(drone)
                .withOrientation(orientation)
                .withExitState(exitState)
                .withStage(Stage.STEP)
                .withTurns(0)
        );
    }

    private SharpTurnState(Builder builder) {
        this.drone = builder.drone;
        this.orientation = builder.orientation;
        this.exitState = builder.exitState;
        this.turns = builder.turns;
        this.stage = builder.stage;
    }

    public static class Builder {
        private Drone drone;
        private SharpTurnState.Stage stage;
        private State exitState;
        private Integer turns;
        private Orientation orientation;

        public Builder(Drone drone) {
            this.drone = drone;
        }

        public Builder withOrientation(Orientation orientation) {
            this.orientation = orientation;
            return this;
        }

        public Builder withExitState(State exitState) {
            this.exitState = exitState;
            return this;
        }

        public Builder withTurns(Integer turns) {
            this.turns = turns;
            return this;
        }

        public Builder withStage(Stage stage) {
            this.stage = stage;
            return this;
        }

        public SharpTurnState build() {
            return new SharpTurnState(this);
        }
    }

    @Override
    public Action request() {
        switch (this.stage) {
            case STEP:
            case EXIT: return new Action(Action.Type.FLY);
            case TURN: return new Action(Action.Type.HEADING).setParam("direction", this.getTurnHeading());
            default: throw new IllegalStateException("Unexpected stage: " + this.stage.toString());
        }
    }

    @Override 
    public State respond(Response response) {
        switch (this.stage) {
            case STEP:
                this.drone.fly(response.getCost());
                return new SharpTurnState.Builder(drone)
                    .withOrientation(this.orientation)
                    .withExitState(this.exitState)
                    .withStage(Stage.TURN)
                    .withTurns(this.turns)
                    .build();
            
            case TURN:
                this.drone.turn(response.getCost(), this.getTurnOrientation());
                return new SharpTurnState.Builder(drone)
                    .withOrientation(this.orientation)
                    .withExitState(this.exitState)
                    .withStage(this.turns < this.maxTurnsBeforeExit ? Stage.TURN : Stage.EXIT)
                    .withTurns(this.turns + 1)
                    .build();
            
            case EXIT:
                this.drone.fly(response.getCost());
                return this.exitState;

            default: throw new IllegalStateException("Unexpected stage: " + this.stage.toString());
        }
    }

    /* Find opposite turn heading */
    private Heading getTurnHeading() {
        Heading heading = this.drone.getHeading();
        if (this.orientation == Orientation.RIGHT) {
            return heading.left();
        }
        
        return heading.right();
    }

    /* Find opposite turn orientation */
    private Orientation getTurnOrientation() {
        if (this.orientation == Orientation.RIGHT) {
            return Orientation.LEFT;
        }
        
        return Orientation.RIGHT;
    }
}
