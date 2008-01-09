package de.unisb.cs.depend.ccs_sem.lexer.tokens;


public class Parallel extends AbstractToken {

    public Parallel(int startPosition) {
        super(startPosition, startPosition);
    }

    @Override
    public String toString() {
        return "|";
    }

}
