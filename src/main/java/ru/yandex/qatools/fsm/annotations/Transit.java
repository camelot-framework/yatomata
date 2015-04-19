package ru.yandex.qatools.fsm.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Defines a single transition inside {@link Transitions} annotation
 *
 * @author Ilya Sadykov
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({TYPE})
public @interface Transit {

    /**
     * Start state for transition. If not specified - this transition with match any start state.
     *
     * @return class of the start state
     */
    Class[] from() default Transitions.ANY.class;

    /**
     * End state for transition. If not specified - state will remain the same.
     *
     * @return class of the end state
     */
    Class to() default Transitions.PREVIOUS.class;

    /**
     * Event that triggers this transition
     *
     * @return trigger event class
     */
    Class[] on() default Object.class;

    /**
     * Whether this transition is a final transition of the FSM
     *
     * @return true if this is a final transition
     */
    boolean stop() default false;
}
