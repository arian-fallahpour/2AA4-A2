package ca.mcmaster.se2aa4.island.teamXXX.Response;

import org.json.JSONObject;

public interface Response {
    public enum Status { OK, FAIL, KO };

    public Integer getCost();
    public Status getStatus();
    public JSONObject getExtras();
}
