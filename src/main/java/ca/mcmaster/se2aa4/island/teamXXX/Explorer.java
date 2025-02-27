package ca.mcmaster.se2aa4.island.teamXXX;

import java.io.StringReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.ace_design.island.bot.IExplorerRaid;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Explorer implements IExplorerRaid {

    Boolean scanned = false;

    private final Logger logger = LogManager.getLogger();

    @Override
    public void initialize(String s) {
        logger.info("** Initializing the Exploration Command Center");
        JSONObject info = new JSONObject(new JSONTokener(new StringReader(s)));

        logger.info("** Initialization info:\n {}",info.toString(2));
        String direction = info.getString("heading");
        Integer batteryLevel = info.getInt("budget");
        
        logger.info("The drone is facing {}", direction);
        logger.info("Battery level is {}", batteryLevel);
    }

    @Override
    public String takeDecision() {
        Double rand = Math.random();
        System.out.println(rand);
        JSONObject decision = new JSONObject();
        

        if (this.scanned) {
            decision.put("action", "fly"); // we stop the exploration immediately
            this.scanned = false;
        } else {
            decision.put("action", "echo"); // we stop the exploration immediately
            JSONObject parameters = new JSONObject();
            parameters.put("direction", "E");
            decision.put("parameters", parameters);

            this.scanned = true;
        }
        // if (rand < .33) {
        // } else if (rand > .67) {
        //     decision.put("action", "heading"); // we stop the exploration immediately
        //     JSONObject parameters = new JSONObject();
        //     parameters.put("direction", "S");
        //     decision.put("parameters", parameters);
        // } else {
        //     decision.put("action", "heading"); // we stop the exploration immediately
        //     JSONObject parameters = new JSONObject();
        //     parameters.put("direction", "E");
        //     decision.put("parameters", parameters);

        // }
        logger.info("** Decision: {}",decision.toString());
        return decision.toString();
    }

    @Override
    public void acknowledgeResults(String s) {
        JSONObject response = new JSONObject(new JSONTokener(new StringReader(s)));
        logger.info("** Response received:\n"+response.toString(2));

        Integer cost = response.getInt("cost");
        logger.info("The cost of the action was {}", cost);

        String status = response.getString("status");
        logger.info("The status of the drone is {}", status);

        JSONObject extraInfo = response.getJSONObject("extras");
        logger.info("Additional information received: {}", extraInfo);
    }

    @Override
    public String deliverFinalReport() {
        return "no creek found";
    }

}
