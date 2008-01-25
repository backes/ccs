package de.unisb.cs.depend.ccs_sem.lexer.tokens;


public class ConstToken extends AbstractToken {

    public ConstToken(int startPosition, int endPosition) {
        super(startPosition, endPosition);
    }

    @Override
    public String toString() {
        return "const";
    }

}
