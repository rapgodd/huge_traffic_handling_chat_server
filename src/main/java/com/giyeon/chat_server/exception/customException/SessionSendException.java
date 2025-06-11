package com.giyeon.chat_server.exception.customException;

public class SessionSendException extends RuntimeException {
    public static final SessionSendException EXCEPTION = new SessionSendException();
}
