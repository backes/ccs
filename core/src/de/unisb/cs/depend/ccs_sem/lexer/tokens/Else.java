package de.unisb.cs.depend.ccs_sem.lexer.tokens;


public class Else extends AbstractToken {

    public Else(int startPosition, int endPosition) {
        super(startPosition, endPosition);
    }

    @Override
    public String toString() {
        return "else";
    }

}
