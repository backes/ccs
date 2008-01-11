package de.unisb.cs.depend.ccs_sem.exceptions;


public class ExportException extends Exception {

    private static final long serialVersionUID = 8843738253591067538L;

    public ExportException() {
        super();
    }

    public ExportException(String message) {
        super(message);
    }

    public ExportException(Throwable cause) {
        super(cause);
    }

    public ExportException(String message, Throwable cause) {
        super(message, cause);
    }

}
