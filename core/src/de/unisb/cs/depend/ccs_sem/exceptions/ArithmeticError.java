package de.unisb.cs.depend.ccs_sem.exceptions;


public class ArithmeticError extends Exception {

    private static final long serialVersionUID = -1800431188088448008L;

    public ArithmeticError() {
        super();
    }

    public ArithmeticError(String message) {
        super(message);
    }

    public ArithmeticError(Throwable cause) {
        super(cause);
    }

    public ArithmeticError(String message, Throwable cause) {
        super(message, cause);
    }

}
