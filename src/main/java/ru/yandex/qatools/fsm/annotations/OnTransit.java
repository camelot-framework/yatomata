package ru.yandex.qatools.fsm.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * @author: Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD})
public @interface OnTransit {
}
