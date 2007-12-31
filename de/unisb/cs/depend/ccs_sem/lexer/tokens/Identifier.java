package de.unisb.cs.depend.ccs_sem.lexer.tokens;



/**
 * An Identifier consists of 'a'..'z', 'A'..'Z', '0'..'9', '_', '?' and '!'.
 * 
 * @author Clemens Hammacher
 */
public class Identifier extends AbstractToken {
    private String name;
    
    public Identifier(int startPosition, int endPosition, String name) {
        super(startPosition, endPosition);
        this.name = name;
        assert name.length() > 0;
    }
    
    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    /**
     * @return true, iff this identifier is valid as parameter for a recursion variable.
     */
    public boolean isValidParameter() {
        for (int i = 0; i < name.length(); ++i) {
            char c = name.charAt(i);
            if (c == '?' || c == '!')
                return false;
            
            // assert that only 'a'..'z', 'A'..'Z', '0'..'9' and '_' are contained
            assert (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                || c == '_' || (i != 0 && c >= '0' && c <= '9');
        }
        
        // no illegal character found...
        return true;
    }

}
