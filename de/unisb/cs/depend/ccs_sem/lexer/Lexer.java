package de.unisb.cs.depend.ccs_sem.lexer;

import java.io.Reader;
import java.util.List;

import de.unisb.cs.depend.ccs_sem.exceptions.LexException;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Token;


public interface Lexer {
    List<Token> lex(Reader input) throws LexException;
    List<Token> lex(String input) throws LexException;
}
