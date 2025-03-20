package ca.mcmaster.se2aa4.island.teamXXX.State;

import ca.mcmaster.se2aa4.island.teamXXX.Action;
import ca.mcmaster.se2aa4.island.teamXXX.Drone.Drone;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Heading;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Orientation;
import ca.mcmaster.se2aa4.island.teamXXX.Response.Response;

public class SharpTurnState implements State {
    public enum Stage { ENTER, TURN, EXIT };

    private Drone drone;
    private Orientation orientation;
    private State exitState;
    private Stage stage;
    private Integer turns;

    private final Integer maxTurnsBeforeExit = 2;
    private final Integer minSafeRange = 1;

    public SharpTurnState(Drone drone, Orientation orientation, State exitState) {
        this(
            new SharpTurnState.Builder(drone)
                .withOrientation(orientation)
                .withExitState(exitState)
                .withStage(Stage.ENTER)
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
            case ENTER: return new Action(Action.Type.FLY);
            case TURN: return new Action(Action.Type.HEADING).setParam("direction", this.getTurnHeading());
            case EXIT: return new Action(Action.Type.FLY);
            default: throw new IllegalStateException("Unexpected stage: " + this.stage.toString());
        }
    }

    @Override 
    public State respond(Response response) {
        switch (this.stage) {
            case ENTER: return this.respondEnter(response);
            case TURN: return this.respondTurn(response);
            case EXIT: return this.respondExit(response);
            default: throw new IllegalStateException("Unexpected stage: " + this.stage.toString());
        }
    }

    private State respondEnter(Response response) {
        this.drone.fly(response.getCost());

        return new SharpTurnState.Builder(drone)
            .withOrientation(this.orientation)
            .withExitState(this.exitState)
            .withStage(Stage.TURN)
            .withTurns(this.turns)
            .build();
    }
    
    private State respondTurn(Response response) {
        this.drone.turn(response.getCost(), this.getTurnOrientation());
        return new SharpTurnState.Builder(drone)
            .withOrientation(this.orientation)
            .withExitState(this.exitState)
            .withStage(this.turns < this.maxTurnsBeforeExit ? Stage.TURN : Stage.EXIT)
            .withTurns(this.turns + 1)
            .build();
    }

    private State respondExit(Response response) {
        this.drone.fly(response.getCost());
        return this.exitState;
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
