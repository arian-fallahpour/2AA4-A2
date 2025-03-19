package ca.mcmaster.se2aa4.island.teamXXX.Response;

import org.json.JSONObject;

public class BasicResponse implements Response {
    private Integer cost;
    private Response.Status status;
    private JSONObject extras;

    public BasicResponse(JSONObject jsonResponse) {
        this.cost = jsonResponse.getInt("cost");
        this.status = Response.Status.valueOf(jsonResponse.getString("status"));
        this.extras = jsonResponse.getJSONObject("extras");
    }

    public Integer getCost() { return this.cost; }
    public Response.Status getStatus() { return this.status; }
    public JSONObject getExtras() { return this.extras; }
}
