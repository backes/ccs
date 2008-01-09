package de.unisb.cs.depend.ccs_sem.lexer.tokens;



public class LBrace extends AbstractToken {

    public LBrace(int startPosition) {
        super(startPosition, startPosition);
    }

    @Override
    public String toString() {
        return "{";
    }

}
