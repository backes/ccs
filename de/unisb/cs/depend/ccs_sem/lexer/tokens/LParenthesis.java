package de.unisb.cs.depend.ccs_sem.lexer.tokens;


public class LParenthesis extends AbstractToken {

    public LParenthesis(int startPosition) {
        super(startPosition, startPosition);
    }

    @Override
    public String toString() {
        return "(";
    }
    
}
