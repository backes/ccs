package de.unisb.cs.depend.ccs_sem.lexer.tokens;


public class Minus extends AbstractToken {

    public Minus(int startPosition) {
        super(startPosition, startPosition);
    }

    @Override
    public String toString() {
        return "-";
    }
    
}
