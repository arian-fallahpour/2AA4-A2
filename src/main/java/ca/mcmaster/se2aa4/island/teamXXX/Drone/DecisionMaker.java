package ca.mcmaster.se2aa4.island.teamXXX.Drone;
import ca.mcmaster.se2aa4.island.teamXXX.Action;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.ActionType;
import ca.mcmaster.se2aa4.island.teamXXX.Enums.Heading;


//class to help bundle all the data a drone has when making a decision
public class DecisionMaker {
    private Action action;
    private Heading direction = Heading.Z;


    //one constructor if given direction, one constructor if not
    public DecisionMaker(ActionType action, Heading direction) {
        this.action = new Action(action);
        this.direction = direction;
    }

    public DecisionMaker(ActionType action) {
        this.action = new Action(action);
    }

    public ActionType getAction() {
        return this.action.getType();
    }

    public Heading getDirection() {
        return this.direction;
    }
    
}
