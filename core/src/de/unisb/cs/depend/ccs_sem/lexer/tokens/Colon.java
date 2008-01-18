package de.unisb.cs.depend.ccs_sem.lexer.tokens;



public class Colon extends AbstractToken {

    public Colon(int position) {
        super(position, position);
    }

    @Override
    public String toString() {
        return ":";
    }

}
