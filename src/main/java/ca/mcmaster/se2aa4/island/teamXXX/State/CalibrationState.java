package ca.mcmaster.se2aa4.island.teamXXX.State;

import ca.mcmaster.se2aa4.island.teamXXX.Action;
import ca.mcmaster.se2aa4.island.teamXXX.Drone.Drone;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Orientation;
import ca.mcmaster.se2aa4.island.teamXXX.Response.Response;

public class CalibrationState implements State {
    public enum Stage { GET_ROWS, GET_COLS };
    private Drone drone;
    private Stage stage;

    private Integer maxRows;
    private Integer maxCols;

    private final Orientation rowsOrientation = Orientation.RIGHT;
    private final Orientation colsOrientation = Orientation.FORWARD;

    public CalibrationState(Drone drone) {
        this(new CalibrationState.Builder(drone).withStage(Stage.GET_ROWS));
    }

    private CalibrationState(Builder builder) {
        this.drone = builder.drone;
        this.stage = builder.stage;
        this.maxRows = builder.maxRows;
        this.maxCols = builder.maxCols;
    }

    private static class Builder {
        private Drone drone;
        private Stage stage;
    
        private Integer maxRows;
        private Integer maxCols;

        public Builder(Drone drone) {
            this.drone = drone;
        }

        public Builder withStage(Stage stage) {
            this.stage = stage;
            return this;
        }

        public Builder withMaxRows(Integer maxRows) {
            this.maxRows = maxRows;
            return this;
        }

        public Builder withMaxCols(Integer maxCols) {
            this.maxCols = maxCols;
            return this;
        }

        public CalibrationState build() {
            return new CalibrationState(this);
        }
    }

    @Override
    public Action request() {
        switch (this.stage) {
            case GET_ROWS: return new Action(Action.Type.ECHO).setParam("direction", rowsOrientation.orient(this.drone.getHeading()));
            case GET_COLS: return new Action(Action.Type.ECHO).setParam("direction", colsOrientation.orient(this.drone.getHeading()));
            default: throw new IllegalStateException("Unexpected stage: " + this.stage.toString());
        }
    }

    /*
     * Updates map dimensions, and calibrates drone map if in final (GET_COLS) stage
     */
    @Override
    public State respond(Response response) {
        switch (this.stage) {
            case GET_ROWS: return this.respondGetRows(response);
            case GET_COLS: return this.respondGetCols(response);
            default: throw new IllegalStateException("Unexpected stage: " + this.stage.toString());
        }
    }

    private State respondGetRows(Response response) {
        this.drone.echo(response.getCost(), colsOrientation);
        return new CalibrationState.Builder(this.drone)
            .withStage(Stage.GET_COLS)
            .withMaxRows(response.getExtras().getInt("range"))
            .build();
    }

    private State respondGetCols(Response response) {
        this.maxCols = response.getExtras().getInt("range");

        this.drone.echo(response.getCost(), colsOrientation);
        this.drone.calibrate(this.maxRows, this.maxCols);
        
        return new EdgeFinderState(this.drone);
    }

    @Override 
    public String getStatus() {
        return "State: " + this.getClass().getName() + ", Stage: " + this.stage.toString();
    }
}
