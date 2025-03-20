package ca.mcmaster.se2aa4.island.teamXXX;

import java.io.StringReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONTokener;

import ca.mcmaster.se2aa4.island.teamXXX.Drone.Drone;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Heading;
import ca.mcmaster.se2aa4.island.teamXXX.State.State;
import ca.mcmaster.se2aa4.island.teamXXX.State.States.CalibrationState;
import eu.ace_design.island.bot.IExplorerRaid;

public class Explorer implements IExplorerRaid {
    private final Logger logger = LogManager.getLogger();

    private Drone drone;
    private DecisionMaker decisionMaker;

    @Override
    public void initialize(String s) {
        logger.info("** Initializing the Exploration Command Center");
        JSONObject info = new JSONObject(new JSONTokener(new StringReader(s)));
        
        // Create drone
        Heading direction = Heading.valueOf(info.getString("heading"));
        Integer charge = info.getInt("budget");
        this.drone = new Drone(charge, direction);

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
