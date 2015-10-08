package com.kendallshaw.dtdnormalizer;

public class DtdException extends Exception {

    private static final long serialVersionUID = -5200361341517407625L;

    public DtdException() {
        super();
    }

    public DtdException(String message, Throwable cause) {
        super(message, cause);
    }

    public DtdException(String message) {
        super(message);
    }

    public DtdException(Throwable cause) {
        super(cause);
    }
}
