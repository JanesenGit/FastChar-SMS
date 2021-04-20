package com.fastchar.sms.exception;

public class FastSMSException extends RuntimeException {
    private static final long serialVersionUID = -7295130877515204380L;

    public FastSMSException() {
        super();
    }

    public FastSMSException(String message) {
        super(message);
    }

    public FastSMSException(String message, Throwable cause) {
        super(message, cause);
    }

    public FastSMSException(Throwable cause) {
        super(cause);
    }

}
