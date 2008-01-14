package de.unisb.cs.depend.ccs_sem.lexer.tokens;


public class Exclamation extends AbstractToken {

    public Exclamation(int startPosition) {
        super(startPosition, startPosition+1);
    }

    @Override
    public String toString() {
        return "!";
    }

}
