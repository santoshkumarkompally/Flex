package com.flex.exception;

public class InitiationException extends RuntimeException {
    String message;

    public InitiationException(String message) {
        System.out.print(message);
        this.message = message;
    }
}
