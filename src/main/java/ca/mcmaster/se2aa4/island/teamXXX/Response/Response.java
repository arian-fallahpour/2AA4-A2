package ca.mcmaster.se2aa4.island.teamXXX.Response;

import org.json.JSONObject;

import ca.mcmaster.se2aa4.island.teamXXX.Enums.ResponseStatus;

/*
 * Interface for the different responses that the drone returns as JSON
 * 
 * Note: Fly and Heading actions don't need their own abstract classes
 */
public class Response {
    protected JSONObject responseObject;
    protected Integer cost;
    protected ResponseStatus status;

    public Response(JSONObject responseObject) {
        this.responseObject = responseObject;
        this.cost = responseObject.getInt("cost");
        this.status = responseObject.getString("status").equals("OK") ? ResponseStatus.OK : ResponseStatus.FAIL;
    }

    /*
     * Returns response JSON object (no leaky abstraction)
     */
    public JSONObject getResponseObject() {
        return new JSONObject(this.responseObject.toString());
    }

    /*
     * Returns extras JSON object (no leaky abstraction)
     */
    public JSONObject getExtrasObject() {
        JSONObject extrasObject = this.responseObject.getJSONObject("extras");
        return new JSONObject(extrasObject.toString());
    }

    /*
     * Returns response's cost (no leaky abstraction)
     */
    public Integer getCost() {
        return Integer.valueOf(this.cost);
    }

    /*
     * Returns response's status (no leaky abstraction)
     */
    public ResponseStatus getStatus() {
        return ResponseStatus.valueOf(this.status.toString());
    }
}
