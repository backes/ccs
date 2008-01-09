package de.unisb.cs.depend.ccs_sem.exceptions;


public class FileReadException extends Exception {

    private static final long serialVersionUID = 2684386121310605507L;

    public FileReadException() {
        super();
    }

    public FileReadException(String message) {
        super(message);
    }

    public FileReadException(Throwable cause) {
        super(cause);
    }

    public FileReadException(String message, Throwable cause) {
        super(message, cause);
    }

}
