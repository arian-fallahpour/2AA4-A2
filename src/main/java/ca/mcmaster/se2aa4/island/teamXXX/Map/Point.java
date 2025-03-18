package ca.mcmaster.se2aa4.island.teamXXX.Map;

import ca.mcmaster.se2aa4.island.teamXXX.Vector;


//class for tracking the drone's position along with the current biome/area it's at
public class Point {
    private String type;
    private Vector position;
    
    public Point(String type, Vector position) {
        this.type = type;
        this.position = position;
    }

    public String getType() {
        return type;
    }

    public Vector getPosition() {
        return position;
    }

    public int getX(){
        return position.x;
    }

    public int getY(){
        return position.y;
    }
}
