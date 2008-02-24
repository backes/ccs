package de.unisb.cs.depend.ccs_sem.lexer.tokens;


public class Assign extends AbstractToken {

    public Assign(int startPosition, int endPosition) {
        super(startPosition, endPosition);
    }

    @Override
    public String toString() {
        return "=";
    }

}
