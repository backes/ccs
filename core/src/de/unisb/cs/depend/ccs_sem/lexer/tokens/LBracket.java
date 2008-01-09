package de.unisb.cs.depend.ccs_sem.lexer.tokens;



public class LBracket extends AbstractToken {

    public LBracket(int startPosition) {
        super(startPosition, startPosition);
    }

    @Override
    public String toString() {
        return "[";
    }

}
