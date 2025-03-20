package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;


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

    /*
     * Adds a parameter value for a key in the action's parameters
     */
    public Action setParam(String key, Object value) {
        this.parameters.put(key, value);
        return this;
    }

    /*
     * Converts action to a string so that it can be used as a request
     */
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("action", this.type.toString().toLowerCase());
        jsonObject.put("parameters", parameters);
        return jsonObject.toString();
    }
    
}
