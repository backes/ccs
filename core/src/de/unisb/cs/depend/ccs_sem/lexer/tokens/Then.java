package de.unisb.cs.depend.ccs_sem.lexer.tokens;


public class Then extends AbstractToken {

    public Then(int startPosition, int endPosition) {
        super(startPosition, endPosition);
    }

    @Override
    public String toString() {
        return "then";
    }

}
