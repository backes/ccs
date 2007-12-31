package de.unisb.cs.depend.ccs_sem.lexer.tokens;


public class Restrict extends AbstractToken {

    public Restrict(int startPosition) {
        super(startPosition, startPosition);
        // TODO Auto-generated constructor stub
    }

    @Override
    public String toString() {
        return "\\";
    }

}
