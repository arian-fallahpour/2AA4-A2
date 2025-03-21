package ca.mcmaster.se2aa4.island.teamXXX;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A class to generate the final exploration report
 */
public class DeliverFinalReport {
    private final Logger logger = LogManager.getLogger();
    private final ResourceTracker tracker;
    private String finalReport;
    
    /**
     * Constructor that initializes the class with the ResourceTracker
     * @param tracker The ResourceTracker instance containing discovery information
     */
    public DeliverFinalReport(ResourceTracker tracker) {
        this.tracker = tracker;
        generateFinalReport();
    }
    
    /**
     * Generates the final report based on the tracker data
     */
    private void generateFinalReport() {
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
        
        this.finalReport = report.toString();
        logger.info("Final Report:\n" + this.finalReport);
    }
    
    /**
     * Returns the generated final report
     * @return The final report as a String
     */
    public String getFinalReport() {
        return this.finalReport;
    }
} 