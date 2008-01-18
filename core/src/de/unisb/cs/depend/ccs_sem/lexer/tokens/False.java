package de.unisb.cs.depend.ccs_sem.lexer.tokens;



public class False extends AbstractToken {

    public False(int startPosition, int endPosition) {
        super(startPosition, endPosition);
    }

    @Override
    public String toString() {
        return "false";
    }

}
