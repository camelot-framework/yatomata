package ru.yandex.qatools.fsm.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Marks class as container for FSM transition event handlers. Each event handler is a method marked
 * with {@link OnTransit} annotation.
 *
 * @author Ilya Sadykov
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({TYPE})
public @interface FSM {

    /**
     * Initial FSM state
     *
     * @return class of initial FSM state
     */
    Class start();

}
