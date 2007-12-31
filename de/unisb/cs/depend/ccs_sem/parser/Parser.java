package de.unisb.cs.depend.ccs_sem.parser;

import java.util.List;

import de.unisb.cs.depend.ccs_sem.lexer.tokens.Token;
import de.unisb.cs.depend.ccs_sem.semantics.Program;


public interface Parser {
    Program parse(List<Token> tokens);
}
