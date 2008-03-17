package de.unisb.cs.depend.ccs_sem.lexer.tokens;

import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.OperatorToken;


public class Minus extends OperatorToken {

    public Minus(int startPosition) {
        super(startPosition, startPosition);
    }

    @Override
    public String toString() {
        return "-";
    }

}
