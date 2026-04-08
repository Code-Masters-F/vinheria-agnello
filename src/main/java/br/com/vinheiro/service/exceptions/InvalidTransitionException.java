package br.com.vinheiro.service.exceptions;

public class InvalidTransitionException extends Exception {
    public InvalidTransitionException(String message) {
        super(message);
    }

    public InvalidTransitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
