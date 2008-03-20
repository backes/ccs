package de.unisb.cs.depend.ccs_sem.exceptions;


public class LexException extends Exception {

    private static final long serialVersionUID = -4281534601860612563L;
    int position;

    public LexException(int position) {
        super();
        this.position = position;
    }

    public LexException(String message, int position) {
        super(message);
        this.position = position;
    }

    public LexException(Throwable cause, int position) {
        super(cause);
        this.position = position;
    }

    public LexException(String message, Throwable cause, int position) {
        super(message, cause);
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

}
