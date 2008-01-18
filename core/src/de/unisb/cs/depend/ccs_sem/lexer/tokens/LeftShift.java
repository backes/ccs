package de.unisb.cs.depend.ccs_sem.lexer.tokens;



public class LeftShift extends AbstractToken {

    public LeftShift(int position) {
        super(position, position+1);
    }

    @Override
    public String toString() {
        return "<<";
    }

}
