package de.unisb.cs.depend.ccs_sem.junit.integrationtests.invalid;


import org.junit.Test;

import de.unisb.cs.depend.ccs_sem.exceptions.LexException;
import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.parser.CCSParser;


public class WrongParameter4 {

    private static final String term = "X[ch] = ch!4711; X[1]";

    @Test(expected=ParseException.class)
    public void checkForError() throws ParseException, LexException {
        new CCSParser().parse(term);
    }

}
