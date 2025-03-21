package ca.mcmaster.se2aa4.island.teamXXX;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Generates the final exploration report with resource statistics
public class DeliverFinalReport {
    private static final Logger logger = LogManager.getLogger(DeliverFinalReport.class);
    private final ResourceTracker resourceTracker;
    private String finalReport;
    
    
    public DeliverFinalReport(ResourceTracker resourceTracker) {
        this.resourceTracker = resourceTracker;
        generateFinalReport();
    }
    
    // Creates a comprehensive report of all discovered resources and their relationships
    private void generateFinalReport() {
        logger.debug("Generating final report");
        StringBuilder report = new StringBuilder();
        
        report.append("===== FINAL EXPLORATION REPORT =====\n\n");
    
        int creekCount = resourceTracker.getCreekCount();
        List<String> creeks = resourceTracker.getAllCreeks();
        report.append("CREEKS DISCOVERED: ").append(creekCount).append("\n");
        
        if (creekCount > 0) {
            report.append("CREEK DETAILS:\n");
            for (String creek : creeks) {
                Vector position = resourceTracker.getCreekPosition(creek);
                report.append(" - ").append(creek).append(" at position ").append(position).append("\n");
            }
        } else {
            report.append("No creeks were discovered during this exploration.\n");
        }
        
        report.append("\n");
        
        int siteCount = resourceTracker.getSiteCount();
        List<String> sites = resourceTracker.getAllEmergencySites();
        report.append("EMERGENCY SITES DISCOVERED: ").append(siteCount).append("\n");
        
        if (siteCount > 0) {
            report.append("EMERGENCY SITE DETAILS:\n");
            for (String site : sites) {
                Vector position = resourceTracker.getSitePosition(site);
                report.append(" - ").append(site).append(" at position ").append(position).append("\n");
            }
            
            // Add nearest creek analysis for each emergency site
            report.append("\nNEAREST CREEK ANALYSIS:\n");
            for (String site : sites) {
                String nearestCreek = resourceTracker.findNearestCreekToSite(site);
                if (nearestCreek != null) {
                    Vector sitePos = resourceTracker.getSitePosition(site);
                    Vector creekPos = resourceTracker.getCreekPosition(nearestCreek);
                    int dx = creekPos.x - sitePos.x;
                    int dy = creekPos.y - sitePos.y;
                    double distance = Math.sqrt(dx*dx + dy*dy);
                    
                    report.append(" - Emergency site ").append(site)
                          .append(" → closest creek is ").append(nearestCreek)
                          .append(" (distance: ").append(String.format("%.2f", distance)).append(")\n");
                } else {
                    report.append(" - Emergency site ").append(site)
                          .append(" → no creek discovered nearby\n");
                }
            }
        } else {
            report.append("No emergency sites were discovered during this exploration.\n");
        }
        
        report.append("\n===== END OF REPORT =====");
        
        finalReport = report.toString();
        logger.info("Final Report:\n" + finalReport);
    }
    
    
    public String getFinalReport() {
        return finalReport;
    }
} 