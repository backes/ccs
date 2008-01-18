package de.unisb.cs.depend.ccs_sem.lexer.tokens;



public class Geq extends AbstractToken {

    public Geq(int position) {
        super(position, position+1);
    }

    @Override
    public String toString() {
        return ">=";
    }

}
