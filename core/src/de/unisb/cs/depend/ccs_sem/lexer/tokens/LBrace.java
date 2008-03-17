package de.unisb.cs.depend.ccs_sem.lexer.tokens;

import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.AbstractToken;



public class LBrace extends AbstractToken {

    public LBrace(int startPosition) {
        super(startPosition, startPosition);
    }

    @Override
    public String toString() {
        return "{";
    }

}
