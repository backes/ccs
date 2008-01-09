package de.unisb.cs.depend.ccs_sem.lexer.tokens;


public class Semicolon extends AbstractToken {

    public Semicolon(int startPosition) {
        super(startPosition, startPosition);
    }

    @Override
    public String toString() {
        return ";";
    }

}
