package ru.yandex.qatools.fsm.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * Defines custom instantiation logic for state objects.
 * <pre>
 * <code>
 * {@literal @}NewState
 * public SomeState initState(Class<SomeState> stateClass, SomeState event) {
 *     return new SomeState();
 * }
 * </code>
 * </pre>
 * @author Ilya Sadykov
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD})
public @interface NewState {
}
