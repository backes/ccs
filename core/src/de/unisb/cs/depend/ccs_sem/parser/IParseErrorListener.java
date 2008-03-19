package de.unisb.cs.depend.ccs_sem.parser;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;

public interface IParseErrorListener {

    void reportParsingError(ParseException e);

}
