package ru.yandex.qatools.fsm;

/**
 * Implement this interface to force Yatomata use your own method to initialize the new state object
 *
 * @author: Ilya Sadykov
 */
public interface InitStateAware<State, Event> {

    /**
     * Method which allows to redefine the default init state logic
     */
    boolean initState(Class<? extends State> stateClass, Event event);
}
