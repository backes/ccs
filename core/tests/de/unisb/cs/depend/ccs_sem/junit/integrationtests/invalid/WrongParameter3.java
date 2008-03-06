package de.unisb.cs.depend.ccs_sem.junit.integrationtests.invalid;


import org.junit.Test;

import de.unisb.cs.depend.ccs_sem.exceptions.LexException;
import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.parser.CCSParser;


public class WrongParameter3 {

    private static final String term = "X[n] = out!n.X[n+1]; X[a]";

    @Test(expected=ParseException.class)
    public void checkForError() throws ParseException, LexException {
        new CCSParser().parse(term);
    }

}
