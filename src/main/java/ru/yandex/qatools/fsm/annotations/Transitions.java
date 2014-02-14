package ru.yandex.qatools.fsm.annotations;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * @author: Ilya Sadykov
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({TYPE})
public @interface Transitions {
    Transit[] value();

    static final class ANY implements Serializable {
    }

    static final class PREVIOUS implements Serializable {
    }
}
