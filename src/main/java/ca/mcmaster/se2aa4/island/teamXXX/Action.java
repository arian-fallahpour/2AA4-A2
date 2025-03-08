package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;

import ca.mcmaster.se2aa4.island.teamXXX.Enums.ActionType;

public class Action {
    private ActionType type;
    private JSONObject parameters = new JSONObject();

    public Action(ActionType type) {
        this.type = type;
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
    @Override
    public String toString() {
        JSONObject action = new JSONObject();
        action.put("action", this.type.toString());
        action.put("parameters", parameters);
        return action.toString();
    }
    
}
