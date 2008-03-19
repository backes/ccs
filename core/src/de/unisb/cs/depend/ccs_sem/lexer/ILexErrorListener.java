package de.unisb.cs.depend.ccs_sem.lexer;

import de.unisb.cs.depend.ccs_sem.exceptions.LexException;

public interface ILexErrorListener {

    void reportLexingError(LexException e);

}
