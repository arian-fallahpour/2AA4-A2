package ca.mcmaster.se2aa4.island.teamXXX;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONArray;
import org.json.JSONObject;

import ca.mcmaster.se2aa4.island.teamXXX.Enums.Heading;

// Just used to test methods

public class Test {

    public static void main(String[] args) {
        try {
            Heading test = Heading.N;
            System.out.println(test.reverse());

            String content = new String(Files.readAllBytes(Paths.get("./maps/map03.json")));
            
            JSONObject jsonObject = new JSONObject(content);
            JSONArray jsonEdges = new JSONArray(jsonObject.get("edge_props").toString());
            
            System.out.println(jsonEdges.get(0).toString()); // Pretty print with indentation
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
