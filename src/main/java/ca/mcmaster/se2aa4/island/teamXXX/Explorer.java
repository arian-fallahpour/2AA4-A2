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

// Main class that coordinates the drone's island exploration mission
public class Explorer implements IExplorerRaid {
    private static final Logger logger = LogManager.getLogger(Explorer.class);
    private Drone drone;
    private DecisionMaker decisionMaker;
    
    @Override
    public void initialize(String s) {
        JSONObject info = new JSONObject(new JSONTokener(new StringReader(s)));
        logger.info("Initializing Explorer");
        Integer charge = info.getInt("budget");
        Heading heading = Heading.valueOf(info.getString("heading"));
        Vector position = new Vector(1, 1);
        this.drone = new Drone(charge, position, heading);
        State initialState = new CalibrationState(this.drone);
        this.decisionMaker = new DecisionMaker(drone, initialState);
    }
    
    @Override
    public String takeDecision() {
        // Ask decision maker for next action based on current state
        Action action = this.decisionMaker.decide();
        logger.info("REQUEST: " + action.toString());
        return action.toString();
    }

    @Override
    public void acknowledgeResults(String s) {
        // Update the decision maker with the JSON response
        JSONObject jsonResponse = new JSONObject(new JSONTokener(new StringReader(s)));
        logger.info("RESPONSE: " + jsonResponse);
        this.decisionMaker.acknowledge(jsonResponse);
        logger.info(this.drone.getStatus());
    }

    @Override
    public String deliverFinalReport() {
        // Generate final report of all discovered resources
        logger.info("Delivering final report");
        ResourceTracker tracker = ResourceTracker.getInstance(this.drone);
        DeliverFinalReport reportGenerator = new DeliverFinalReport(tracker);
        return reportGenerator.getFinalReport();
    }
}
