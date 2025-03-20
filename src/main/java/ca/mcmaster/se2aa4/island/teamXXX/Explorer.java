package ca.mcmaster.se2aa4.island.teamXXX;

import java.io.StringReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONTokener;

import ca.mcmaster.se2aa4.island.teamXXX.Drone.Drone;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Heading;
import ca.mcmaster.se2aa4.island.teamXXX.State.CalibrationState;
import ca.mcmaster.se2aa4.island.teamXXX.State.State;
import eu.ace_design.island.bot.IExplorerRaid;

public class Explorer implements IExplorerRaid {
    private final Logger logger = LogManager.getLogger();

    private Drone drone;
    private DecisionMaker decisionMaker;

    @Override
    public void initialize(String s) {
        JSONObject info = new JSONObject(new JSONTokener(new StringReader(s)));
        logger.info(info.toString());
        
        // Create drone
        Integer charge = info.getInt("budget");
        Heading heading = Heading.valueOf(info.getString("heading"));
        Vector position = new Vector(1, 1);
        this.drone = new Drone(charge, position, heading);
        
        // Create decision maker
        State initialState = new CalibrationState(this.drone);
        this.decisionMaker = new DecisionMaker(drone, initialState);
    }

    @Override
    public String takeDecision() {
        Action action = this.decisionMaker.decide();
        logger.info("REQUEST: " + action.toString());

        return action.toString();
    }

    @Override
    public void acknowledgeResults(String s) {
        JSONObject jsonResponse = new JSONObject(new JSONTokener(new StringReader(s)));
        
        logger.info("RESPONSE: " + jsonResponse);
        this.decisionMaker.acknowledge(jsonResponse);
        
        logger.info(this.drone.getStatus());
    }

    @Override
    public String deliverFinalReport() {
        return "No creeks found";
    }
}
