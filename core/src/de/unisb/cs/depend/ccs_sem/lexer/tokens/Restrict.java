package de.unisb.cs.depend.ccs_sem.lexer.tokens;

import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.OperatorToken;


public class Restrict extends OperatorToken {

    public Restrict(int startPosition) {
        super(startPosition, startPosition);
    }

    @Override
    public String toString() {
        return "\\";
    }

}
