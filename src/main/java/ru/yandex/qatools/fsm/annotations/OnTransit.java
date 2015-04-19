package ru.yandex.qatools.fsm.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * Marks method as FSM transition event handler. Such handler will be executed during transition. Method should have
 * one of the following signatures:
 * <pre>
 * <code>
 * {@literal @}OnTransit
 * public void onEvent(FromState fromState, ToState toState, Event event){}
 * 
 * {@literal @}OnTransit
 * public void onEvent(FromState fromState, Event event){}
 * 
 * {@literal @}OnTransit
 * public void onRun(Event event){}
 * </code>
 * </pre>
 * @author Ilya Sadykov
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD})
public @interface OnTransit {
}
