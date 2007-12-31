package de.unisb.cs.depend.ccs_sem.lexer.tokens;



public class Identifier extends AbstractToken {
    private String name;
    
    public Identifier(int startPosition, int endPosition, String name) {
        super(startPosition, endPosition);
        this.name = name;
    }
    
    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

}
