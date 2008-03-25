package de.unisb.cs.depend.ccs_sem.lexer;

import java.io.Reader;
import java.util.List;

import de.unisb.cs.depend.ccs_sem.exceptions.LexException;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.Token;


/**
 * The interface that a lexer has to implement.
 *
 * @author Clemens Hammacher
 */
public interface Lexer {

    /**
     * Lexes the input provided by the given Reader.
     *
     * @param input the Reader to read the input from
     * @return a list of tokens
     * @throws LexException if an error occured while lexing
     */
    List<Token> lex(Reader input) throws LexException;

    /**
     * Lexes the input given in a String.
     *
     * @param input String to lex
     * @return a list of tokens
     * @throws LexException if an error occured while lexing
     */
    List<Token> lex(String input) throws LexException;

}
