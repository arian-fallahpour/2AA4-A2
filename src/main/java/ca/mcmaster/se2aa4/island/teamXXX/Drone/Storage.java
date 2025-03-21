package ca.mcmaster.se2aa4.island.teamXXX.Drone;

import java.util.ArrayList;

// Stores discovered creeks and the current emergency site in the drone's memory
public class Storage implements Cloneable {
    private ArrayList<POI> creeks;
    private POI site;

    public Storage() {
        this.creeks = new ArrayList<POI>();
    }

    public void saveCreek(POI poi) { this.creeks.add(poi); }
    public void saveSite(POI poi) { this.site = poi; }

    public ArrayList<POI> getCreeks() { return this.creeks; }
    public POI getSite() { return this.site; }
}
