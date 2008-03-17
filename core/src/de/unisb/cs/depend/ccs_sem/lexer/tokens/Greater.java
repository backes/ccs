package de.unisb.cs.depend.ccs_sem.lexer.tokens;

import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.OperatorToken;


public class Greater extends OperatorToken {

    public Greater(int position) {
        super(position, position);
    }

    @Override
    public String toString() {
        return ">";
    }

}
