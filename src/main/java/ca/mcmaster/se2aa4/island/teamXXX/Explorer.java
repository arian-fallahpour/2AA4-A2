package ca.mcmaster.se2aa4.island.teamXXX;

import java.io.StringReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.ace_design.island.bot.IExplorerRaid;
import org.json.JSONObject;
import org.json.JSONTokener;

import ca.mcmaster.se2aa4.island.teamXXX.Drone.Drone;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.ActionType;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Heading;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Orientation;
import ca.mcmaster.se2aa4.island.teamXXX.Action;

public class Explorer implements IExplorerRaid {
    Boolean scanned;
    Integer turns = 0;
    Boolean shouldTurnLeft = false;
    Drone drone;
    Integer count = 0;

    private final Logger logger = LogManager.getLogger();

    @Override
    public void initialize(String s) {
        logger.info("** Initializing the Exploration Command Center");
        JSONObject info = new JSONObject(new JSONTokener(new StringReader(s)));

        logger.info("** Initialization info:\n {}",info.toString(2));
        Integer battery = info.getInt("budget");
        Heading direction = Heading.valueOf(info.getString("heading"));

        this.drone = new Drone(battery, direction);
        this.scanned = false;
        
        logger.info("The drone is facing: " + drone.getDirection().toString());
        logger.info("The drone's Battery level is: " + drone.getBattery().getCharge());
    }

    @Override
    public String takeDecision() {
        Action action;

        Integer rightBorderX = 50;
        Integer bottomBorderY = 53;

        Vector position = this.drone.getPosition();
        Heading heading = this.drone.getHeading();

        if (!this.scanned) {
            if (position.y < bottomBorderY) {
                if ((heading == Heading.E && position.x < rightBorderX) || (heading == Heading.W && position.x > 2)) {
                    action = this.drone.fly();
                } else {
                    if (this.turns < 2) {
                        if (!this.shouldTurnLeft) {
                            action = this.drone.turn(Orientation.RIGHT);
                        } else {
                            action = this.drone.turn(Orientation.LEFT);
                        }
                        this.turns++;
                    } else {
                        action = this.drone.fly();
                        this.turns = 0;
                        this.shouldTurnLeft = !this.shouldTurnLeft;
                    }
                }
            } else {
                action = this.drone.stop();
            }
        } else {
            action = this.drone.scan();
        }
        this.scanned = !this.scanned;

        // Return value formatting
        String decision = action.toString();
        logger.info("Decision: " + decision);
        return decision;
    }

    @Override
    public void acknowledgeResults(String s) {
        JSONObject response = new JSONObject(new JSONTokener(new StringReader(s)));
        logger.info("Drone coords: [" + this.drone.getPosition().x + ", " + this.drone.getPosition().y + "]");
        // logger.info("** Response received:\n"+response.toString(2));

        // Integer cost = response.getInt("cost");
        // logger.info("The cost of the action was {}", cost);

        // String status = response.getString("status");
        // logger.info("The status of the drone is {}", status);

        // JSONObject extraInfo = response.getJSONObject("extras");
        // logger.info("Additional information received: {}", extraInfo);
    }

    @Override
    public String deliverFinalReport() {
        return "no creek found";
    }

}
