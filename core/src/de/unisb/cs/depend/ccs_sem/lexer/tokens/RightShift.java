package de.unisb.cs.depend.ccs_sem.lexer.tokens;



public class RightShift extends AbstractToken {

    public RightShift(int position) {
        super(position, position+1);
    }

    @Override
    public String toString() {
        return ">>";
    }

}
