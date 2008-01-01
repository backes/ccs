package de.unisb.cs.depend.ccs_sem.exceptions;


public class InternalSystemException extends RuntimeException {

    private static final long serialVersionUID = 6966331073077514373L;

    public InternalSystemException() {
        super();
    }

    public InternalSystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public InternalSystemException(String message) {
        super(message);
    }

    public InternalSystemException(Throwable cause) {
        super(cause);
    }

}
