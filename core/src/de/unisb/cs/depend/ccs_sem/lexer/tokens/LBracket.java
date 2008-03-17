package de.unisb.cs.depend.ccs_sem.lexer.tokens;

import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.AbstractToken;



public class LBracket extends AbstractToken {

    public LBracket(int startPosition) {
        super(startPosition, startPosition);
    }

    @Override
    public String toString() {
        return "[";
    }

}
