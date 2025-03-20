package ca.mcmaster.se2aa4.island.teamXXX.Response;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import ca.mcmaster.se2aa4.island.teamXXX.Enums.Biome;

public class ScanResponse implements Response {
    private Integer cost;
    private Response.Status status;
    private JSONObject extras;

    private ArrayList<Biome> biomes;
    private ArrayList<String> creeks;
    private ArrayList<String> sites;

    public ScanResponse(JSONObject jsonResponse) {
        this.cost = jsonResponse.getInt("cost");
        this.status = Response.Status.valueOf(jsonResponse.getString("status"));
        this.extras = jsonResponse.getJSONObject("extras");

        this.setBiomes();
        this.setCreeks();
        this.setSites();
    }

    @Override
    public Integer getCost() { return this.cost; }
    
    @Override
    public Response.Status getStatus() { return this.status; }
    
    @Override
    public JSONObject getExtras() { return this.extras; }

    public ArrayList<Biome> getBiomes() { return this.biomes; }
    public ArrayList<String> getCreeks() { return this.creeks; }
    public ArrayList<String> getSites() { return this.sites; }

    private void setBiomes() {
        JSONArray biomesArray = this.extras.getJSONArray("biomes");
        this.biomes = new ArrayList<Biome>();
        for (int i = 0; i < biomesArray.length(); i++) {
            this.biomes.add(Biome.valueOf(biomesArray.getString(i)));
        }
    }

    private void setCreeks() {
        JSONArray creeksArray = this.extras.getJSONArray("creeks");
        this.creeks = new ArrayList<String>();
        for (int i = 0; i < creeksArray.length(); i++) {
            this.creeks.add(creeksArray.getString(i));
        }
    }
    
    private void setSites() {
        JSONArray sitesArray = this.extras.getJSONArray("sites");
        this.sites = new ArrayList<String>();
        for (int i = 0; i < sitesArray.length(); i++) {
            this.sites.add(sitesArray.getString(i));
        }
    }
}
