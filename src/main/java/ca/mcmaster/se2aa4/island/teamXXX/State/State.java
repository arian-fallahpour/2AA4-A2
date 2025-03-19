package ca.mcmaster.se2aa4.island.teamXXX.State;

import ca.mcmaster.se2aa4.island.teamXXX.Action;
import ca.mcmaster.se2aa4.island.teamXXX.Response.Response;

/*
 * I added this for the future so that we can separate the searchstates into their own classes
 * 
 * All States must have a Stage, that includes a FINAL state
 */
public interface State {
    /*
     * Determines current state's action and next state
     */
    public Action request();

    /*
     * Responds to a response from the game engine and updates application's state
     */
    public State respond(Response response);
}
