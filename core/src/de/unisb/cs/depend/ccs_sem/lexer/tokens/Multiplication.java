package de.unisb.cs.depend.ccs_sem.lexer.tokens;



public class Multiplication extends AbstractToken {

    public Multiplication(int position) {
        super(position, position);
    }

    @Override
    public String toString() {
        return "*";
    }

}
