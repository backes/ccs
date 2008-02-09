package de.unisb.cs.depend.ccs_sem.lexer.tokens;


public class Error extends AbstractToken {

    public Error(int startPosition, int endPosition) {
        super(startPosition, endPosition);
    }

    @Override
    public String toString() {
        return "ERROR";
    }

}
