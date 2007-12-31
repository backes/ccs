package de.unisb.cs.depend.ccs_sem.exceptions;


public class InteralSystemException extends RuntimeException {

    private static final long serialVersionUID = 6966331073077514373L;

    public InteralSystemException() {
        super();
    }

    public InteralSystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public InteralSystemException(String message) {
        super(message);
    }

    public InteralSystemException(Throwable cause) {
        super(cause);
    }

}
