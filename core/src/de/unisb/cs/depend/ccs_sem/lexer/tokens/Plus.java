package de.unisb.cs.depend.ccs_sem.lexer.tokens;

import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.OperatorToken;


public class Plus extends OperatorToken {

    public Plus(int startPosition) {
        super(startPosition, startPosition);
    }

    @Override
    public String toString() {
        return "+";
    }

}
