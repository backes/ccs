package de.unisb.cs.depend.ccs_sem.lexer.tokens;

import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.AbstractToken;



public class Dot extends AbstractToken {

    public Dot(int position) {
        super(position, position);
    }

    @Override
    public String toString() {
        return ".";
    }

}
