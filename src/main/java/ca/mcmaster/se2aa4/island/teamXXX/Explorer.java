package ca.mcmaster.se2aa4.island.teamXXX;

import java.io.StringReader;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.ace_design.island.bot.IExplorerRaid;
import org.json.JSONObject;
import org.json.JSONTokener;

import ca.mcmaster.se2aa4.island.teamXXX.Drone.Drone;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.ActionType;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Biome;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Heading;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Orientation;
import ca.mcmaster.se2aa4.island.teamXXX.Response.EchoResponse;
import ca.mcmaster.se2aa4.island.teamXXX.Response.Response;
import ca.mcmaster.se2aa4.island.teamXXX.Response.ScanResponse;

public class Explorer implements IExplorerRaid {
    Boolean scanned;
    Integer turns = 0;
    Boolean shouldTurnLeft = false;
    Drone drone;
    Integer count = 0;
    ArrayList<Vector> creekCoords;

    ActionType previousActionType;

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

        // Moves in a zig-zag pattern
        // RUN Runner.main, then take a look at outputs/Explorer.svg
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
        this.previousActionType = action.getType();
        return action.toString();
    }

    @Override
    public void acknowledgeResults(String s) {
        JSONObject jsonResponse = new JSONObject(new JSONTokener(new StringReader(s)));

        try {
            logger.info("The drone's coordinates are: " + this.drone.getPosition().toString());
            if (this.previousActionType == ActionType.SCAN) {
                ScanResponse response = new ScanResponse(jsonResponse);
                ArrayList<String> creeks = response.getCreeks();
                if (creeks.size() > 0) {
                    logger.info("CREEKS: " + creeks.toString());
                }
                
                // if (creeks.size() > 0) {   
                //     logger.info("The cost of the action was {}", response.getCost());
                //     logger.info("The status of the drone is {}", response.getStatus());
                //     logger.info("The biomes scanned are {}", response.getBiomes().toString());
                // }
            } 
        } catch(Exception e) {
            System.err.println("EEE: " + e.getMessage());
        }
    }

    @Override
    public String deliverFinalReport() {
        logger.info(this.creekCoords.toString());
        return this.creekCoords.toString();
    }

}
