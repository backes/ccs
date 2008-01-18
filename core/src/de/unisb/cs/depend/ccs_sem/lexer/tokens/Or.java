package de.unisb.cs.depend.ccs_sem.lexer.tokens;



public class Or extends AbstractToken {

    public Or(int startPosition, int endPosition) {
        super(startPosition, endPosition);
    }

    @Override
    public String toString() {
        return "||";
    }

}
