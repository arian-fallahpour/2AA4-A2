package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;
import org.json.JSONTokener;

import ca.mcmaster.se2aa4.island.teamXXX.Response.ScanResponse;

// Just used to test methods

public class Test {

    public static void main(String[] args) {
        // try {
            JSONObject jsonObject = new JSONObject(new JSONTokener("{\"cost\": 2, \"extras\": { \"biomes\": [\"BEACH\"], \"creeks\": [\"id\"], \"sites\": []}, \"status\": \"OK\"}"));
            ScanResponse scanResponse = new ScanResponse(jsonObject);

            System.out.println(scanResponse.getCost());
            System.out.println(scanResponse.getStatus());
            System.out.println(scanResponse.getBiomes().toString());
            System.out.println(scanResponse.getCreeks().toString());
            System.out.println(scanResponse.getSites().toString());


            // String content = new String(Files.readAllBytes(Paths.get("./maps/map03.json")));
            
            // JSONObject jsonObject = new JSONObject(content);
            // JSONArray jsonEdges = new JSONArray(jsonObject.get("edge_props").toString());
            
            // System.out.println(jsonEdges.get(0).toString()); // Pretty print with indentation
        // } catch (IOException e) {
        //     e.printStackTrace();
        // }
    }
}
