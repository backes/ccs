package de.unisb.cs.depend.ccs_sem.lexer.tokens;

import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.AbstractToken;


public class Colon extends AbstractToken {

    public Colon(int position) {
        super(position, position);
    }

    @Override
    public String toString() {
        return ":";
    }

}
