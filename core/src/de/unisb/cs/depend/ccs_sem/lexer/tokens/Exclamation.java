package de.unisb.cs.depend.ccs_sem.lexer.tokens;

import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.AbstractToken;


public class Exclamation extends AbstractToken {

    public Exclamation(int startPosition) {
        super(startPosition, startPosition+1);
    }

    @Override
    public String toString() {
        return "!";
    }

}
