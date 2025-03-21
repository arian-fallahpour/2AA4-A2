package ca.mcmaster.se2aa4.island.teamXXX.Response;

import org.json.JSONObject;

// Common interface for all response types from the island game engine
public interface Response {
    public enum Status { OK, FAIL, KO };

    public Integer getCost();
    public Status getStatus();
    public JSONObject getExtras();
}
