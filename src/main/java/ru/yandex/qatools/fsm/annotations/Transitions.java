package ru.yandex.qatools.fsm.annotations;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * @author Ilya Sadykov
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({TYPE})
public @interface Transitions {

    /**
     * A list of available transitions for this FSM
     * <pre>
     * <code>
     * {@literal @}Transitions({
     *     {@literal @}Transit(from = State1.class, on = Event1.class, to = State2.class),
     *     {@literal @}Transit(from = State2.class, on = Event2.class, to = State3.class)
     * })
     * </code>
     * </pre>
     */
    Transit[] value();

    final class ANY implements Serializable {
    }

    final class PREVIOUS implements Serializable {
    }
}
