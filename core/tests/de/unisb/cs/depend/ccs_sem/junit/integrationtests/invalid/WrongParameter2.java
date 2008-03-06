package de.unisb.cs.depend.ccs_sem.junit.integrationtests.invalid;


import org.junit.Test;

import de.unisb.cs.depend.ccs_sem.exceptions.LexException;
import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.parser.CCSParser;


public class WrongParameter2 {

    private static final String term = "X[a] = a; X[out!1]";

    @Test(expected=ParseException.class)
    public void checkForError() throws ParseException, LexException {
        new CCSParser().parse(term);
    }

}
