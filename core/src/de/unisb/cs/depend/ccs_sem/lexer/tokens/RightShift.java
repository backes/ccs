package de.unisb.cs.depend.ccs_sem.lexer.tokens;

import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.OperatorToken;



public class RightShift extends OperatorToken {

    public RightShift(int position) {
        super(position, position+1);
    }

    @Override
    public String toString() {
        return ">>";
    }

}
