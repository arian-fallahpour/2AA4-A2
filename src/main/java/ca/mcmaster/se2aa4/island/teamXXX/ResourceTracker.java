package ca.mcmaster.se2aa4.island.teamXXX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ca.mcmaster.se2aa4.island.teamXXX.Drone.Drone;
import ca.mcmaster.se2aa4.island.teamXXX.Response.ScanResponse;

// Singleton class that tracks all discovered creeks and emergency sites
public class ResourceTracker {
    private static final Logger logger = LogManager.getLogger(ResourceTracker.class);
    private static ResourceTracker instance;
    
    private Map<String, Vector> creeks;
    private Map<String, Vector> emergencySites;
    private Drone drone;
    
    private ResourceTracker(Drone drone) {
        creeks = new HashMap<>();
        emergencySites = new HashMap<>();
        logger.info("Resource Tracker initialized");
        this.drone = drone;
    }
    
    // Returns the singleton instance, creating it if necessary
    public static synchronized ResourceTracker getInstance(Drone drone) {
        if (instance == null) {
            instance = new ResourceTracker(drone);
        }
        return instance;
    }
    
    // Processes scan results and stores any new creeks and sites with their positions
    public void processScanResponse(ScanResponse response) {
        if (response == null) return;
        
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
        
        if (response.getSites() != null) {
            for (String site : response.getSites()) {
                if (!emergencySites.containsKey(site)) {
                    emergencySites.put(site, currentPosition);
                    logger.info("New emergency site discovered: " + site + " at " + currentPosition);
                }
            }
        }
    }
    
    public List<String> getAllCreeks() {
        return new ArrayList<>(creeks.keySet());
    }
    
    public List<String> getAllEmergencySites() {
        return new ArrayList<>(emergencySites.keySet());
    }
    
    public int getCreekCount() {
        return creeks.size();
    }
    
    public int getSiteCount() {
        return emergencySites.size();
    }
    
    public boolean hasCreek(String creek) {
        return creeks.containsKey(creek);
    }
    
    public boolean hasEmergencySite(String site) {
        return emergencySites.containsKey(site);
    }
    
    public Vector getCreekPosition(String creekId) {
        return creeks.get(creekId);
    }

    public Vector getSitePosition(String siteId) {
        return emergencySites.get(siteId);
    }
    
    public void reset() {
        creeks.clear();
        emergencySites.clear();
        logger.info("Resource Tracker reset");
    }
    
    // Finds the nearest creek to a given emergency site
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
