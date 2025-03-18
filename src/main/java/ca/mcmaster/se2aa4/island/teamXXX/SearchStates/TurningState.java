// package ca.mcmaster.se2aa4.island.teamXXX.SearchStates;

// import org.json.JSONObject;

// import ca.mcmaster.se2aa4.island.teamXXX.Action;
// import ca.mcmaster.se2aa4.island.teamXXX.Vector;
// import ca.mcmaster.se2aa4.island.teamXXX.Drone.GridSearch;
// import ca.mcmaster.se2aa4.island.teamXXX.Enums.ActionType;
// import ca.mcmaster.se2aa4.island.teamXXX.Enums.Heading;

// public class TurningState implements SearchState {
//     @Override
//     public Action handle(
//         GridSearch gridSearch, 
//         Vector position, 
//         Heading currentHeading, 
//         JSONObject lastResponse
//     ) {
//         // Turn sequence: first turn perpendicular
//         if (turnStep == 0) {
//             // First turn is to the right of current direction
//             Heading turnDirection = currentDirection.right();
//             // Update for next turn step
//             turnStep++;
//             Action turnAction = new Action(ActionType.HEADING);
//             turnAction.setParameter("direction", turnDirection);
//             return turnAction;
//         }
//         // Turn sequence: scan after first turn
//         else if (turnStep == 1) {
//             // Scan after turning to catch any creeks at the edge
//             turnStep++;
//             currentState = new EdgeScanState();
//             return new Action(ActionType.SCAN);
//         }
//         // After turn is complete, go back to flying
//         else {
//             currentState = new FlyingState();
//             return new Action(ActionType.FLY);
//         }
//     }
// }