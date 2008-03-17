package de.unisb.cs.depend.ccs_sem.lexer.tokens;

import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.OperatorToken;


public class Division extends OperatorToken {

    public Division(int position) {
        super(position, position);
    }

    @Override
    public String toString() {
        return "/";
    }

}
