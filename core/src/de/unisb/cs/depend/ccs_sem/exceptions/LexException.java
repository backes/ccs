package de.unisb.cs.depend.ccs_sem.exceptions;


public class LexException extends Exception {

    private static final long serialVersionUID = -4281534601860612563L;
    private String environment;

    public LexException() {
        super();
    }

    public LexException(String message) {
        super(message);
    }

    public LexException(Throwable cause) {
        super(cause);
    }

    public LexException(String message, Throwable cause) {
        super(message, cause);
    }

    public LexException(String message, String environment) {
        super(message);
        this.environment = environment;
    }

    public String getEnvironment() {
        return environment;
    }

}
