package de.unisb.cs.depend.ccs_sem.lexer;

import java.io.StringReader;
import java.util.List;

import de.unisb.cs.depend.ccs_sem.exceptions.LexException;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Token;


public abstract class AbstractLexer implements Lexer {

    public List<Token> lex(String input) throws LexException {
        return lex(new StringReader(input));
    }

}
