package ru.yandex.qatools.fsm;

/**
 * @author: Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface Yatomata<T> {

    /**
     * Builder to build the fsm instance
     */
    public static interface Builder<T> {
        /**
         * Build the fsm with the defined initial state
         */
        Yatomata<T> build(Object state);

        /**
         * Build th fsm with the default initial state
         */
        Yatomata<T> build();
    }

    /**
     * Fires new event into the state machine
     */
    Object fire(Object event);

    /**
     * Returns the internal FSM instance
     */
    Object getFSM();

    /**
     * Returns the internal FSM class
     */
    Class<T> getFSMClass();

    /**
     * Returns the current state for the FSM
     */
    Object getCurrentState();

    /**
     * Checks if the FSM is already completed
     */
    boolean isCompleted();
}
