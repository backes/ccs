package de.unisb.cs.depend.ccs_sem.exceptions;


public class ParseException extends Exception {

    private static final long serialVersionUID = 279050231911730217L;
    private String environment = null;

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

    public ParseException(String message, String environment) {
        super(message);
        this.environment = environment;
    }

    public void setEnvironment(String environment) {
        this.environment  = environment;
    }

    public String getEnvironment() {
        return environment;
    }

}
