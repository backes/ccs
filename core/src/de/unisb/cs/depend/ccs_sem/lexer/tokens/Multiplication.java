package de.unisb.cs.depend.ccs_sem.lexer.tokens;

import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.OperatorToken;


public class Multiplication extends OperatorToken {

    public Multiplication(int position) {
        super(position, position);
    }

    @Override
    public String toString() {
        return "*";
    }

}
