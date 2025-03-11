package ca.mcmaster.se2aa4.island.teamXXX.Response;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import ca.mcmaster.se2aa4.island.teamXXX.Enums.Biome;

public class ScanResponse extends Response {
    private List<Biome> biomes = new ArrayList<Biome>();
    private List<String> creeks = new ArrayList<String>();
    private List<String> sites = new ArrayList<String>();

    public ScanResponse(JSONObject responseObject) {
        super(responseObject);

        JSONObject extrasObject = this.getExtrasObject();
        this.setBiomes(extrasObject.getJSONArray("biomes"));
        this.setCreeks(extrasObject.getJSONArray("creeks"));
        this.setSites(extrasObject.getJSONArray("sites"));
    }

    /*
     * Sets biomes with proper enum type
     */
    private void setBiomes(JSONArray biomes) {
        for (int i = 0; i < biomes.length(); i++) {
            this.biomes.add(Biome.valueOf(biomes.getString(i)));
        }
    }

    /*
     * Sets creeks
     */
    private void setCreeks(JSONArray creeks) {
        for (int i = 0; i < creeks.length(); i++) {
            this.creeks.add(creeks.getString(i));
        }
    }

    /*
     * Sets sites
     */
    private void setSites(JSONArray sites) {
        for (int i = 0; i < sites.length(); i++) {
            this.sites.add(sites.getString(i));
        }
    }

    /*
     * returns biomes 
     */
    public ArrayList<Biome> getBiomes() {
        return this.biomes;
    }

    /*
     * Returns creeks
     */
    public ArrayList<String> getCreeks() {
        return this.creeks;
    }

    /*
     * Returns sites
     */
    public ArrayList<String> getSites() {
        return this.sites;
    }
}
