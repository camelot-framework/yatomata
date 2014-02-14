package ru.yandex.qatools.fsm;

/**
 * Implement this interface to force Yatomata verify if the FSM completion is required
 *
 * @author: Ilya Sadykov
 */
public interface StopConditionAware<State, Event> {

    /**
     * Method is called when it is required to check if FSM must be stopped
     */
    boolean isStopRequired(State state, Event event);
}
