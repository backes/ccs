package de.unisb.cs.depend.ccs_sem.lexer.tokens;

import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.Token;

public class EOFToken implements Token {

    private final int position;

    public EOFToken(int position) {
        this.position = position;
    }

    @Override
    public int getEndPosition() {
        return position;
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    public int getStartPosition() {
        return position;
    }

}
