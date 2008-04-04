package de.unisb.cs.depend.ccs_sem.lexer.tokens;

import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.AbstractToken;



/**
 * An Identifier consists of 'a'..'z', 'A'..'Z', '0'..'9' and '_'.
 *
 * @author Clemens Hammacher
 */
public class Identifier extends AbstractToken {
    private final String name;
    // has this identifier doublequotes around it?
    private final boolean isQuoted;

    public Identifier(int startPosition, int endPosition, String name, boolean isQuoted) {
        super(startPosition, endPosition);
        this.name = name;
        assert name.length() > 0;
        this.isQuoted = isQuoted;
    }

    public boolean isQuoted() {
        return isQuoted;
    }

    @Override
    public String toString() {
        if (isQuoted)
            return '"' + name + '"';
        return name;
    }

    public String getName() {
        return name;
    }

    public boolean isUpperCase() {
        assert name.length() > 0;
        return Character.isUpperCase(name.charAt(0));
    }

    public boolean isLowerCase() {
        assert name.length() > 0;
        return Character.isLowerCase(name.charAt(0));
    }

}
