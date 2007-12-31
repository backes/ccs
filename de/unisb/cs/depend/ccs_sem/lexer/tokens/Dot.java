package de.unisb.cs.depend.ccs_sem.lexer.tokens;



public class Dot extends AbstractToken {

    public Dot(int startPosition) {
        super(startPosition, startPosition);
    }

    @Override
    public String toString() {
        return ".";
    }

}
