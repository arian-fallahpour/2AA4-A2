package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;

import ca.mcmaster.se2aa4.island.teamXXX.Enums.ActionType;

/**
 * Represents an action that the drone can perform, including parameters.
 * This class handles the conversion to the JSON format required by the game engine.
 */
public class Action {
    private ActionType type;
    private JSONObject parameters;
    
    /**
     * Create a new action with the specified type
     */
    public Action(ActionType type) {
        this.type = type;
        this.parameters = new JSONObject();
    }
    
    /**
     * Set a parameter for this action
     */
    public void setParameter(String name, Object value) {
        parameters.put(name, value);
    }
    
    /**
     * Get the type of this action
     */
    public ActionType getType() {
        return type;
    }
    
    /**
     * Get the parameters for this action
     */
    public JSONObject getParameters() {
        return parameters;
    }
    
    /**
     * Convert this action to a JSON string for the game engine
     */
    @Override
    public String toString() {
        JSONObject actionJson = new JSONObject();
        actionJson.put("action", type.toString().toLowerCase());
        
        // Only add parameters if there are any
        if (parameters.length() > 0) {
            actionJson.put("parameters", parameters);
        }
        
        return actionJson.toString();
    }
}
