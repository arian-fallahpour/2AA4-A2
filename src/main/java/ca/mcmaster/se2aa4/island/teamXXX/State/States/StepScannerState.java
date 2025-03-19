package ca.mcmaster.se2aa4.island.teamXXX.State.States;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ca.mcmaster.se2aa4.island.teamXXX.Action;
import ca.mcmaster.se2aa4.island.teamXXX.Drone.Drone;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Biome;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Orientation;
import ca.mcmaster.se2aa4.island.teamXXX.Response.EchoResponse;
import ca.mcmaster.se2aa4.island.teamXXX.Response.Response;
import ca.mcmaster.se2aa4.island.teamXXX.Response.ScanResponse;
import ca.mcmaster.se2aa4.island.teamXXX.State.State;

public class StepScannerState implements State {
    private final Logger logger = LogManager.getLogger();

    public enum Stage { SCAN, STEP, CHECK };

    private Drone drone;
    private Stage stage;
    
    public StepScannerState(Drone drone) {
        this(drone, Stage.SCAN);
    }

    private StepScannerState(Drone drone, Stage stage) {
        this.drone = drone;
        this.stage = stage;
    }

    @Override
    public Action request() {
        switch (this.stage) {
            case SCAN: return new Action(Action.Type.SCAN);
            case STEP: return new Action(Action.Type.FLY);
            case CHECK: return new Action(Action.Type.ECHO).setParam("direction", this.drone.getHeading());
            default: throw new IllegalStateException("Unexpected stage: " + this.stage.toString());
        }
    }

    @Override 
    public State respond(Response response) {
        switch (this.stage) {
            case SCAN:
                this.drone.scan(response.getCost());
                
                ScanResponse scanResponse = (ScanResponse)response;
                ArrayList<Biome> biomes = scanResponse.getBiomes();

                Boolean overOcean = biomes.size() == 1 && biomes.get(0) == Biome.OCEAN;
                if (overOcean) {
                    return new StepScannerState(this.drone, Stage.CHECK);
                } else {
                    return new StepScannerState(this.drone, Stage.STEP);
                }

            case STEP:
                this.drone.fly(response.getCost());
                return new StepScannerState(this.drone, Stage.SCAN);

            case CHECK:
                this.drone.echo(response.getCost(), Orientation.FORWARD);

                EchoResponse echoResponse = (EchoResponse)response;
                
                Boolean foundGround = echoResponse.getFound() == EchoResponse.Found.GROUND;
                logger.info("FOUND GROUND: " + foundGround);
                if (foundGround) {
                    return new EdgeArriverState(this.drone, echoResponse.getRange());
                } else {
                    // return null;
                    return new BorderArriverState(this.drone, echoResponse.getRange());
                }

            default: throw new IllegalStateException("Unexpected stage: " + this.stage.toString());
        }
    }
}
