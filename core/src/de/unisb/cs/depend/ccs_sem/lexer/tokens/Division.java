package de.unisb.cs.depend.ccs_sem.lexer.tokens;



public class Division extends AbstractToken {

    public Division(int position) {
        super(position, position);
    }

    @Override
    public String toString() {
        return "/";
    }

}
