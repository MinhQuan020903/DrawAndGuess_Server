package com.backend.rest.auth.exc;

public class DuplicateUsernameExc extends Exception {
    public DuplicateUsernameExc(String message) {
        super(message);
    }

    public DuplicateUsernameExc(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateUsernameExc(Throwable cause) {
        super(cause);
    }
}
