package de.unisb.cs.depend.ccs_sem.lexer.tokens;


/**
 * 
 * All tokens have to be immutable.
 * 
 * @author Clemens Hammacher
 *
 */
public interface Token {
    int getStartPosition();
    int getEndPosition();
}
