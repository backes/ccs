package de.unisb.cs.depend.ccs_sem.lexer.tokens;



public class IntervalDots extends AbstractToken {

    public IntervalDots(int startPosition, int endPosition) {
        super(startPosition, endPosition);
    }

    @Override
    public String toString() {
        return "..";
    }

}
