package de.unisb.cs.depend.ccs_sem.lexer.tokens;


public class Assignment extends AbstractToken {

    public Assignment(int startPosition) {
        super(startPosition, startPosition);
    }

    @Override
    public String toString() {
        return "=";
    }

}
