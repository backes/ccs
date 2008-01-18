package de.unisb.cs.depend.ccs_sem.lexer.tokens;



public class Greater extends AbstractToken {

    public Greater(int position) {
        super(position, position);
    }

    @Override
    public String toString() {
        return ">";
    }

}
