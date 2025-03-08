package ca.mcmaster.se2aa4.island.teamXXX.Response;

import org.json.JSONObject;

import ca.mcmaster.se2aa4.island.teamXXX.Enums.RadarType;

public class EchoResponse extends Response {
    private Integer range;
    private RadarType reading;

    public EchoResponse(JSONObject responseObject) {
        super(responseObject);

        JSONObject extrasObject = this.getExtrasObject();
        this.range = extrasObject.getInt("range");
        this.reading = RadarType.valueOf(extrasObject.getString("found"));
    }

    /*
     * Returns echo range (no leaky abstraction)
     */
    public Integer getRange() {
        return Integer.valueOf(this.range);
    }

    /*
     * Returns echo reading (no leaky abstraction)
     */
    public RadarType getReading() {
        return RadarType.valueOf(this.reading.toString());
    }
}
