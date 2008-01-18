package de.unisb.cs.depend.ccs_sem.lexer.tokens;


public class When extends AbstractToken {

    public When(int startPosition, int endPosition) {
        super(startPosition, endPosition);
    }

    @Override
    public String toString() {
        return "when";
    }

}
