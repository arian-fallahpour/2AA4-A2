package ca.mcmaster.se2aa4.island.teamXXX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ca.mcmaster.se2aa4.island.teamXXX.Drone.Drone;
import ca.mcmaster.se2aa4.island.teamXXX.Response.ScanResponse;

/**
 * Singleton class that tracks all discovered creeks and emergency sites
 * using the existing ScanResponse class
 */
public class ResourceTracker {
    private static final Logger logger = LogManager.getLogger(ResourceTracker.class);
    private static ResourceTracker instance;
    
    // Use Maps to link IDs to positions
    private Map<String, Vector> creeks;
    private Map<String, Vector> emergencySites;
    private Drone drone;
    
    private ResourceTracker(Drone drone) {
        creeks = new HashMap<>();
        emergencySites = new HashMap<>();
        logger.info("Resource Tracker initialized");
        this.drone = drone;
    }
    
    /**
     * Get the singleton instance
     */
    public static synchronized ResourceTracker getInstance(Drone drone) {
        if (instance == null) {
            instance = new ResourceTracker(drone);
        }
        return instance;
    }
    
    /**
     * Process a scan response and update tracked resources
     */
    public void processScanResponse(ScanResponse response) {
        if (response == null) return;
        
        // Get current drone position to use as resource position
        Vector currentPosition = drone.getPosition();
               
        // Add creeks with positions
        if (response.getCreeks() != null) {
            for (String creek : response.getCreeks()) {
                if (!creeks.containsKey(creek)) {
                    creeks.put(creek, currentPosition);
                    logger.info("New creek discovered: " + creek + " at " + currentPosition);
                }
            }
        }
        
        // Add any new emergency sites from the scan response
        if (response.getSites() != null) {
            for (String site : response.getSites()) {
                if (!emergencySites.containsKey(site)) {
                    emergencySites.put(site, currentPosition);
                    logger.info("New emergency site discovered: " + site + " at " + currentPosition);
                }
            }
        }
    }
    
    /**
     * Get all discovered creeks
     */
    public List<String> getAllCreeks() {
        return new ArrayList<>(creeks.keySet());
    }
    
    /**
     * Get all discovered emergency sites
     */
    public List<String> getAllEmergencySites() {
        return new ArrayList<>(emergencySites.keySet());
    }
    
    /**
     * Get count of discovered creeks
     */
    public int getCreekCount() {
        return creeks.size();
    }
    
    /**
     * Get count of discovered emergency sites
     */
    public int getSiteCount() {
        return emergencySites.size();
    }
    
    /**
     * Check if a specific creek has been discovered
     */
    public boolean hasCreek(String creek) {
        return creeks.containsKey(creek);
    }
    
    /**
     * Check if a specific emergency site has been discovered
     */
    public boolean hasEmergencySite(String site) {
        return emergencySites.containsKey(site);
    }
    /**
     * Get the position of a specific creek
     */
    public Vector getCreekPosition(String creekId) {
        return creeks.get(creekId);
    }

    /**
     * Get the position of a specific emergency site
     */
    public Vector getSitePosition(String siteId) {
        return emergencySites.get(siteId);
    }
    
    /**
     * Clear all tracked resources (useful for testing)
     */
    public void reset() {
        creeks.clear();
        emergencySites.clear();
        logger.info("Resource Tracker reset");
    }
    
    /**
     * Find the nearest creek to a given emergency site
     */
    public String findNearestCreekToSite(String siteId) {
        if (!emergencySites.containsKey(siteId)) {
            return null; // Site not found
        }
        
        Vector sitePosition = emergencySites.get(siteId);
        String nearestCreek = null;
        int minDistance = Integer.MAX_VALUE;
        
        for (Map.Entry<String, Vector> entry : creeks.entrySet()) {
            String creekId = entry.getKey();
            Vector creekPosition = entry.getValue();
            
            int dx = creekPosition.x - sitePosition.x;
            int dy = creekPosition.y - sitePosition.y;
            int distance = dx*dx + dy*dy; // Square of Euclidean distance
            
            if (distance < minDistance) {
                minDistance = distance;
                nearestCreek = creekId;
            }
        }
        
        return nearestCreek;
    }
}
