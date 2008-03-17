package de.unisb.cs.depend.ccs_sem.lexer.tokens;

import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.AbstractToken;



public class RBrace extends AbstractToken {

    public RBrace(int startPosition) {
        super(startPosition, startPosition);
    }

    @Override
    public String toString() {
        return "}";
    }

}
