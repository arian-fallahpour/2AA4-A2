package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;

import ca.mcmaster.se2aa4.island.teamXXX.Enums.ActionType;

public class Action {
    private ActionType type;
    private JSONObject parameters = new JSONObject();

    public Action(ActionType type) {
        this.type = type;
    }

    public ActionType getType() {
        return ActionType.valueOf(this.type.toString());
    }

    /*
     * Adds a parameter value for a key in the action's parameters
     */
    public void setParameter(String key, Object value) {
        this.parameters.put(key, value);
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
