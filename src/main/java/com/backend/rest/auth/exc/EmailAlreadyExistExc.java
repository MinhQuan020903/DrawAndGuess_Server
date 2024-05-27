package com.backend.rest.auth.exc;

public class EmailAlreadyExistExc extends Exception {
    public EmailAlreadyExistExc(String message) {
        super(message);
    }

    public EmailAlreadyExistExc(String message, Throwable cause) {
        super(message, cause);
    }

    public EmailAlreadyExistExc(Throwable cause) {
        super(cause);
    }
}
