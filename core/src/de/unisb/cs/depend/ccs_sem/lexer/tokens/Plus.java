package de.unisb.cs.depend.ccs_sem.lexer.tokens;


public class Plus extends AbstractToken {

    public Plus(int startPosition) {
        super(startPosition, startPosition);
    }

    @Override
    public String toString() {
        return "+";
    }
    
}
