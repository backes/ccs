package de.unisb.cs.depend.ccs_sem.lexer.tokens;



public class Multiplikation extends AbstractToken {

    public Multiplikation(int position) {
        super(position, position);
    }

    @Override
    public String toString() {
        return "*";
    }

}
