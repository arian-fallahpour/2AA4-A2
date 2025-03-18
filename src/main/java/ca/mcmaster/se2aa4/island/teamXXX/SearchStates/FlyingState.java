// package ca.mcmaster.se2aa4.island.teamXXX.SearchStates;

// import org.json.JSONObject;

// import ca.mcmaster.se2aa4.island.teamXXX.Action;
// import ca.mcmaster.se2aa4.island.teamXXX.Vector;
// import ca.mcmaster.se2aa4.island.teamXXX.Drone.GridSearch;
// import ca.mcmaster.se2aa4.island.teamXXX.Enums.ActionType;
// import ca.mcmaster.se2aa4.island.teamXXX.Enums.Heading;

// public class FlyingState implements SearchState {
//     @Override
//     public Action handle(
//         GridSearch gridSearch, 
//         Vector position, 
//         Heading currentHeading, 
//         JSONObject lastResponse
//     ) {
//         // If we've flown enough in this direction, scan again
//         if (++moveCounter >= MOVE_STEP) {
//             moveCounter = 0;
//             currentState = new ScanningState();
//             return new Action(ActionType.SCAN);
//         }
        
//         // Check if we need to change direction first
//         if (currentHeading != currentDirection) {
//             Action headingAction = new Action(ActionType.HEADING);
//             headingAction.setParameter("direction", currentDirection);
//             return headingAction;
//         }
        
//         // Continue flying
//         return new Action(ActionType.FLY);
//     }
// }
