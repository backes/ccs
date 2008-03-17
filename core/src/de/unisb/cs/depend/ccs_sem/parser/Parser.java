package de.unisb.cs.depend.ccs_sem.parser;

import java.util.List;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.Token;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;


public interface Parser {
    Program parse(List<Token> tokens) throws ParseException;
}
