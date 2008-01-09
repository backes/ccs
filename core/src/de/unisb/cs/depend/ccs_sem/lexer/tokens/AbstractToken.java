package de.unisb.cs.depend.ccs_sem.lexer.tokens;


public abstract class AbstractToken implements Token {
    
    int startPosition;
    int endPosition;
    
    protected AbstractToken(int startPosition, int endPosition) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public int getEndPosition() {
        return endPosition;
    }

}
