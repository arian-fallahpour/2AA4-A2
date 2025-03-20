package ca.mcmaster.se2aa4.island.teamXXX.Drone;

import org.json.JSONArray;
import org.json.JSONObject;

import ca.mcmaster.se2aa4.island.teamXXX.Vector;

public class POI {
    public enum Type { CREEK, SITE };

    private Type type;
    private String id;
    private Vector position;

    public POI(Type type, String id, Vector position) {
        this.type = type;
        this.id = id;
        this.position = position;
    }

    public Type getType() { return this.type; }
    public String getId() { return this.id; }
    public Vector getPosition() { return this.position; }

    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", this.type.toString());
        jsonObject.put("id", this.id);

        JSONArray jsonPosition = new JSONArray().put(this.position.x).put(this.position.y);
        jsonObject.put("position", jsonPosition);
        
        return jsonObject;
    }
}
