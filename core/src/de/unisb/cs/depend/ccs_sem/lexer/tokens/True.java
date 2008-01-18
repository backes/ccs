package de.unisb.cs.depend.ccs_sem.lexer.tokens;



public class True extends AbstractToken {

    public True(int startPosition, int endPosition) {
        super(startPosition, endPosition);
    }

    @Override
    public String toString() {
        return "true";
    }

}
