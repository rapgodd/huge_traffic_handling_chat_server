package com.giyeon.chat_server.exception.customException;

public class NotFoundException extends RuntimeException {

    public static final NotFoundException EXCEPTION = new NotFoundException();

    private NotFoundException() {
        super("Resource not found");
    }
}
