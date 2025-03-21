package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;

import ca.mcmaster.se2aa4.island.teamXXX.Drone.Drone;
import ca.mcmaster.se2aa4.island.teamXXX.Response.BasicResponse;
import ca.mcmaster.se2aa4.island.teamXXX.Response.EchoResponse;
import ca.mcmaster.se2aa4.island.teamXXX.Response.Response;
import ca.mcmaster.se2aa4.island.teamXXX.Response.ScanResponse;
import ca.mcmaster.se2aa4.island.teamXXX.State.LandingState;
import ca.mcmaster.se2aa4.island.teamXXX.State.State;



 // This class makes decision based on the data in the drone
public class DecisionMaker {
    private Drone drone;
    private State state;
    private Action action;

    private final Integer minBatteryBeforeAbort = 30;

    public DecisionMaker(Drone drone, State initialState) {
        this.drone = drone;
        this.state = initialState;
    }

    public Action decide() {

        if (this.drone.getCharge() < this.minBatteryBeforeAbort) {
            this.state = new LandingState(this.drone);
        }
        
        if (this.state != null) {
            this.action = this.state.request();
        } else {
            this.action = new Action(Action.Type.STOP);
        }
        
        return this.action;
    }
    
    public void acknowledge(JSONObject jsonResponse) {
        if (this.state == null) return;
        
        Response response = this.getResponse(jsonResponse);
        if (response.getStatus() != Response.Status.OK) return;        
        
        this.state = this.state.respond(response);
    }

    private Response getResponse(JSONObject jsonResponse) {
        switch (this.action.getType()) {
            case SCAN: return new ScanResponse(jsonResponse);
            case ECHO: return new EchoResponse(jsonResponse);
            default: return new BasicResponse(jsonResponse);
        }
    }
}