package ca.mcmaster.se2aa4.island.teamXXX;

import java.io.StringReader;
import java.util.List;

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
        ResourceTracker tracker = ResourceTracker.getInstance(this.drone);
        
        // Debug info
        logger.info("Generating final report");
        logger.info("Total creeks tracked: " + tracker.getCreekCount());
        logger.info("Total sites tracked: " + tracker.getSiteCount());
        
        StringBuilder report = new StringBuilder();
        report.append("=== FINAL EXPLORATION REPORT ===\n\n");
        
        // Report on creeks with positions
        List<String> creeks = tracker.getAllCreeks();
        if (creeks.isEmpty()) {
            report.append("No creeks found\n");
        } else {
            report.append("Found ").append(tracker.getCreekCount()).append(" creeks:\n");
            for (String creek : creeks) {
                Vector position = tracker.getCreekPosition(creek);
                report.append("  - ").append(creek)
                      .append(" at position (")
                      .append(position.x).append(", ").append(position.y)
                      .append(")\n");
            }
        }
        
        // Report on emergency sites with positions
        List<String> sites = tracker.getAllEmergencySites();
        if (sites.isEmpty()) {
            report.append("No emergency sites found\n");
        } else {
            report.append("Found ").append(tracker.getSiteCount()).append(" emergency sites:\n");
            for (String site : sites) {
                Vector position = tracker.getSitePosition(site);
                report.append("  - ").append(site)
                      .append(" at position (")
                      .append(position.x).append(", ").append(position.y)
                      .append(")\n");
            }
        }
        
        // Add nearest creek to site information if sites exist
        if (!sites.isEmpty()) {
            report.append("\n=== EMERGENCY SITE ANALYSIS ===\n");
            for (String site : sites) {
                String nearestCreek = tracker.findNearestCreekToSite(site);
                if (nearestCreek != null) {
                    Vector sitePos = tracker.getSitePosition(site);
                    Vector creekPos = tracker.getCreekPosition(nearestCreek);
                    double dx = creekPos.x - sitePos.x;
                    double dy = creekPos.y - sitePos.y;
                    double distance = Math.sqrt(dx*dx + dy*dy);
                    
                    report.append("Site ").append(site)
                          .append(" at position (").append(sitePos.x).append(", ").append(sitePos.y).append(")")
                          .append(" - Nearest creek: ").append(nearestCreek)
                          .append(" at position (").append(creekPos.x).append(", ").append(creekPos.y).append(")")
                          .append(" (distance: ").append(String.format("%.2f", distance)).append(")\n");
                } else {
                    Vector sitePos = tracker.getSitePosition(site);
                    report.append("Site ").append(site)
                          .append(" at position (").append(sitePos.x).append(", ").append(sitePos.y).append(")")
                          .append(" - No creeks found nearby\n");
                }
            }
        }
        
        String finalReport = report.toString();
        logger.info("Final Report:\n" + finalReport);
        return finalReport;
    }
}
