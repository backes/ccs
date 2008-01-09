package de.unisb.cs.depend.ccs_sem.lexer.tokens;


public class Restrict extends AbstractToken {

    public Restrict(int startPosition) {
        super(startPosition, startPosition);
    }

    @Override
    public String toString() {
        return "\\";
    }

}
