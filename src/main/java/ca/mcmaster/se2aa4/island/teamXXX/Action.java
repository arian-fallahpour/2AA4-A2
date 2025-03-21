package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;

// Represents an action that the drone can perform
public class Action {
    public enum Type { FLY, ARRIVE, HEADING, SCAN, ECHO, STOP };
    private Type type;
    private JSONObject parameters = new JSONObject();

    public Action(Type type) {
        this.type = type;
    }

    public Type getType() {
        return Type.valueOf(this.type.toString());
    }

    public Action setParam(String key, Object value) {
        this.parameters.put(key, value);
        return this;
    }

    public String toString() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("action", this.type.toString().toLowerCase());
        jsonObject.put("parameters", parameters);
        return jsonObject.toString();
    }
    
}
