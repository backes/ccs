package de.unisb.cs.depend.ccs_sem.lexer.tokens;


public class Equals extends AbstractToken {

    private boolean comp;

    public Equals(int startPosition, int endPosition, boolean comp) {
        super(startPosition, endPosition);
        this.comp = comp;
    }

    public boolean isComp() {
        return comp;
    }
    
    @Override
    public String toString() {
        return comp ? "==" : "=";
    }

}
