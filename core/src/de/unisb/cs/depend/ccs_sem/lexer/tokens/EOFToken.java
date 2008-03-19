package de.unisb.cs.depend.ccs_sem.lexer.tokens;

import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.Token;

public class EOFToken implements Token {

    private final int position;

    public EOFToken(int position) {
        this.position = position;
    }

    public int getEndPosition() {
        return position;
    }

    public int getLength() {
        return 1;
    }

    public int getStartPosition() {
        return position;
    }

}
