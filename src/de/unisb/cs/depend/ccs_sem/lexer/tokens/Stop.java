package de.unisb.cs.depend.ccs_sem.lexer.tokens;


public class Stop extends AbstractToken {
    
    public Stop(int startPosition) {
        super(startPosition, startPosition);
    }

    @Override
    public String toString() {
        return "0";
    }
    
}
