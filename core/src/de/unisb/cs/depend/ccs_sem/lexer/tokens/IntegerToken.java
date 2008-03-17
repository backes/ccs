package de.unisb.cs.depend.ccs_sem.lexer.tokens;

import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.AbstractToken;



/**
 * An IntegerToken consists of a series (at least one) of '0'..'9'.
 *
 * @author Clemens Hammacher
 */
public class IntegerToken extends AbstractToken {
    private final int value;

    public IntegerToken(int startPosition, int endPosition, int value) {
        super(startPosition, endPosition);
        this.value = value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    public int getValue() {
        return value;
    }

}
