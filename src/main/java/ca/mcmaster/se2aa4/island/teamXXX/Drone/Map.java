package ca.mcmaster.se2aa4.island.teamXXX.Drone;

public class Map implements Cloneable {
    private Integer rows;
    private Integer cols;

    public Map(Integer rows, Integer cols) {
        this.rows = rows;
        this.cols = cols;
    }

    public Integer getRows() { return this.rows; }
    public Integer getCols() { return this.cols; }

    @Override
    public Map clone() {
        return new Map(this.rows, this.cols);
    }
}
