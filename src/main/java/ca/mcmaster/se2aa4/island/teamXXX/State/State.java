package ca.mcmaster.se2aa4.island.teamXXX.State;

import ca.mcmaster.se2aa4.island.teamXXX.Response.Response;

import ca.mcmaster.se2aa4.island.teamXXX.Action;

// Interface for all drone states in the state machine pattern
public interface State {
    Action request();
    State respond(Response response);
}
