package de.unisb.cs.depend.ccs_sem.lexer.tokens;



public class And extends AbstractToken {

    public And(int startPosition, int endPosition) {
        super(startPosition, endPosition);
    }

    @Override
    public String toString() {
        return "&&";
    }

}
