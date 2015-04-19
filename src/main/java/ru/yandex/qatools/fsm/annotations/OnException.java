package ru.yandex.qatools.fsm.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * Marks method as exception handler like shown below:
 * <pre>
 * <code>
 * {@literal @}@OnException(preserve = true)
 * public void onArithmeticException(SomeException e, FromState fromState, EndState endState, Event event) {
 *     System.out.println("An exception occurred: " + e.getMessage());
 * }
 * </code>
 * </pre>
 * @author Ilya Sadykov
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD})
public @interface OnException {

    /**
     * Whether to continue transition after handling exception
     *
     * @return true to continue transition
     */
    boolean preserve() default false;
}
