package ru.yandex.qatools.fsm;

/**
 * @author: Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class StateMachineException extends RuntimeException {
    public StateMachineException(String message) {
        super(message);
    }

    public StateMachineException(String message, Throwable cause) {
        super(message, cause);
    }
}
