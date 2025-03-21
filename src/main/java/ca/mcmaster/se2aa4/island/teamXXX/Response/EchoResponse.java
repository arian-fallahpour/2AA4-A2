package ca.mcmaster.se2aa4.island.teamXXX.Response;

import org.json.JSONObject;

// Handles echo responses that provide information about distances to island features
public class EchoResponse implements Response {
    public enum Found { OUT_OF_RANGE, GROUND };

    private Integer cost;
    private Response.Status status;
    private JSONObject extras;

    private Found found;
    private Integer range;

    public EchoResponse(JSONObject jsonResponse) {
        this.cost = jsonResponse.getInt("cost");
        this.status = Response.Status.valueOf(jsonResponse.getString("status"));
        this.extras = jsonResponse.getJSONObject("extras");
        
        this.found = Found.valueOf(this.extras.getString("found"));
        this.range = this.extras.getInt("range");
    }

    @Override
    public Integer getCost() { return this.cost; }
    
    @Override
    public Response.Status getStatus() { return this.status; }
    
    @Override
    public JSONObject getExtras() { return this.extras; }

    public Found getFound() { return this.found; }
    public Integer getRange() { return this.range; }
}
