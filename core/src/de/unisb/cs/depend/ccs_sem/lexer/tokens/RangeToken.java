package de.unisb.cs.depend.ccs_sem.lexer.tokens;


public class RangeToken extends AbstractToken {

    public RangeToken(int startPosition, int endPosition) {
        super(startPosition, endPosition);
    }

    @Override
    public String toString() {
        return "range";
    }

}
