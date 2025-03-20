package ca.mcmaster.se2aa4.island.teamXXX.State;

import java.util.ArrayList;

import ca.mcmaster.se2aa4.island.teamXXX.Action;
import ca.mcmaster.se2aa4.island.teamXXX.Drone.Drone;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Biome;
import ca.mcmaster.se2aa4.island.teamXXX.Response.EchoResponse;
import ca.mcmaster.se2aa4.island.teamXXX.Response.Response;
import ca.mcmaster.se2aa4.island.teamXXX.Response.ScanResponse;

public class StepScannerState implements State {
    public enum Stage { SCAN, FLY, CHECK };

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
            case FLY: return new Action(Action.Type.FLY);
            case CHECK: return new Action(Action.Type.ECHO).setParam("direction", this.drone.getHeading());
            default: throw new IllegalStateException("Unexpected stage: " + this.stage.toString());
        }
    }

    @Override 
    public State respond(Response response) {
        switch (this.stage) {
            case SCAN: return this.respondScan(response);
            case FLY: return this.respondFly(response);
            case CHECK: return this.respondCheck(response);
            default: throw new IllegalStateException("Unexpected stage: " + this.stage.toString());
        }
    }

    private State respondScan(Response response) {
        this.drone.scan(response.getCost());
                
        ScanResponse scanResponse = (ScanResponse)response;
        ArrayList<String> creeks = scanResponse.getCreeks();
        ArrayList<String> sites = scanResponse.getSites();

        this.drone.saveCreeks(creeks);
        if (sites.size() == 1) { this.drone.saveSite(sites.get(0)); }
        
        ArrayList<Biome> biomes = scanResponse.getBiomes();
        Boolean overOcean = biomes.size() == 1 && biomes.get(0) == Biome.OCEAN;
        if (overOcean) {
            return new StepScannerState(this.drone, Stage.CHECK);
        } else {
            return new StepScannerState(this.drone, Stage.FLY);
        }
    }

    private State respondFly(Response response) {
        this.drone.fly(response.getCost());
        return new StepScannerState(this.drone, Stage.SCAN);
    }

    private State respondCheck(Response response) {
        this.drone.echo(response.getCost());

        EchoResponse echoResponse = (EchoResponse)response;
        EchoResponse.Found found = echoResponse.getFound();
        Integer distance = echoResponse.getRange();
        
        if (distance == 0) {
            return new StepScannerState(this.drone, Stage.FLY);
        } else if (found == EchoResponse.Found.GROUND) {
            return new EdgeArriverState(this.drone);
        } else {
            return new BorderArriverState(this.drone);
        }
    }
}
