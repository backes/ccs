package de.unisb.cs.depend.ccs_sem.exceptions;


public class ParseException extends Exception {

    private static final long serialVersionUID = 279050231911730217L;

    public ParseException() {
        super();
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseException(String message) {
        super(message);
    }

    public ParseException(Throwable cause) {
        super(cause);
    }

}
