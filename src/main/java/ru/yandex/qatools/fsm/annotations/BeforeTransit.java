package ru.yandex.qatools.fsm.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * Marks method as pre-transition handler that is executed right before transition. See {@link OnTransit} for method
 * signature examples.
 *
 * @author Ilya Sadykov
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD})
public @interface BeforeTransit {
}
