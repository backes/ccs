package de.unisb.cs.depend.ccs_sem.exceptions;


public class SystemException extends RuntimeException {

    private static final long serialVersionUID = -1337123462997555765L;

    public SystemException() {
        super();
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public SystemException(String message) {
        super(message);
    }

    public SystemException(Throwable cause) {
        super(cause);
    }

}
