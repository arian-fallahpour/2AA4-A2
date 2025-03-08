package ca.mcmaster.se2aa4.island.teamXXX.Drone;

import ca.mcmaster.se2aa4.island.teamXXX.Action;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.ActionType;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Heading;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Orientation;

public class Drone {
    private Heading direction;
    private Battery battery;

    public Drone(Integer battery, Heading direction) {
        this.battery = new Battery(battery);
        this.direction = direction;
    }

    public Action fly() {
        return new Action(ActionType.FLY);
    }

    public Action turn(Orientation orientation) {
        Action action = new Action(ActionType.HEADING);

        this.direction = this.direction.turn(orientation);
        action.setParameter("direction", this.direction.toString());

        return action;
    }

    public Action echo(Orientation orientation) {
        Action action = new Action(ActionType.ECHO);

        this.direction = this.direction.turn(orientation);
        action.setParameter("direction", this.direction.toString());

        return action;
    }


    public Action scan() {
        return new Action(ActionType.SCAN);
    }
}
