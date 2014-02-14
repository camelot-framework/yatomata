package ru.yandex.qatools.fsm;

/**
 * @author: Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class FSMException extends Exception {

    public FSMException(String message) {
        super(message);
    }

    public FSMException(String message, Throwable cause) {
        super(message, cause);
    }
}
