package de.unisb.cs.depend.ccs_sem.lexer.tokens;



public class Less extends AbstractToken {

    public Less(int position) {
        super(position, position);
    }

    @Override
    public String toString() {
        return "<";
    }

}
