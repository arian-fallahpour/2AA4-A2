// package ca.mcmaster.se2aa4.island.teamXXX.SearchStates;

// import org.json.JSONObject;

// import ca.mcmaster.se2aa4.island.teamXXX.Action;
// import ca.mcmaster.se2aa4.island.teamXXX.Vector;
// import ca.mcmaster.se2aa4.island.teamXXX.Drone.GridSearch;
// import ca.mcmaster.se2aa4.island.teamXXX.Enums.ActionType;
// import ca.mcmaster.se2aa4.island.teamXXX.Enums.Heading;

// public class EdgeCheckingState implements SearchState {
//     @Override
//     public Action handle(
//         GridSearch gridSearch, 
//         Vector position, 
//         Heading currentHeading, 
//         JSONObject lastResponse
//     ) {
//         // If we got a response with echo information
//         if (lastResponse != null && lastResponse.has("extras") && lastResponse.getJSONObject("extras").has("found")) {
//             JSONObject extras = lastResponse.getJSONObject("extras");
//             if (extras.getString("found").equals("OUT_OF_RANGE")) {
//                 // We're at the edge, time to turn
//                 gridSearch.edgeRange = extras.getInt("range");
//                 gridSearch.currentState = new TurningState();
//                 return new Action(ActionType.HEADING);
//             } else {
//                 // There's still ground ahead, keep flying
//                 gridSearch.groundRange = extras.getInt("range");
//                 gridSearch.currentState = new FlyingState();
//                 return new Action(ActionType.FLY);
//             }
//         }
        
//         // Send an echo in the current direction
//         Action echoAction = new Action(ActionType.ECHO);
//         echoAction.setParameter("direction", currentDirection);
//         return echoAction;
//     }
// }
