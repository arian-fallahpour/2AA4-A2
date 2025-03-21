package ca.mcmaster.se2aa4.island.teamXXX.Drone;

// Manages the drone's energy capacity and consumption
public class Battery {
    private Integer charge;

    public Battery(Integer charge) {
        this.charge = charge;
    }

    public Integer getCharge() {
        return Integer.valueOf(this.charge.toString());
    }

    public void drain(Integer amount) {
        this.charge -= amount;
    }

    public void recharge(Integer amount) {
        this.charge += amount;
    }
}
