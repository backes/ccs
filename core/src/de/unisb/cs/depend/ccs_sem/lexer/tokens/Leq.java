package de.unisb.cs.depend.ccs_sem.lexer.tokens;



public class Leq extends AbstractToken {

    public Leq(int position) {
        super(position, position+1);
    }

    @Override
    public String toString() {
        return "<=";
    }

}
