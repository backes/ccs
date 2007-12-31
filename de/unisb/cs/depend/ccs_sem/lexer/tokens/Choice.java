package de.unisb.cs.depend.ccs_sem.lexer.tokens;


public class Choice extends AbstractToken {

    public Choice(int startPosition) {
        super(startPosition, startPosition);
    }

    @Override
    public String toString() {
        return "+";
    }
    
}
