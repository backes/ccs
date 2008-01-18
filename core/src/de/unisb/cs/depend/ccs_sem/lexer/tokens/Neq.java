package de.unisb.cs.depend.ccs_sem.lexer.tokens;



public class Neq extends AbstractToken {

    public Neq(int position) {
        super(position, position+1);
    }

    @Override
    public String toString() {
        return "!=";
    }

}
