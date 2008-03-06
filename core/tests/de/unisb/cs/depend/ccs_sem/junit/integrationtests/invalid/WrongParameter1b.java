package de.unisb.cs.depend.ccs_sem.junit.integrationtests.invalid;


import org.junit.Test;

import de.unisb.cs.depend.ccs_sem.exceptions.LexException;
import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.parser.CCSParser;


public class WrongParameter1b {

    private static final String term =
        "X[channel,message] = channel!message.do.something.else.X[channel,channel]; "
        + "X[out,0]";

    @Test(expected=ParseException.class)
    public void checkForError() throws ParseException, LexException {
        new CCSParser().parse(term);
    }

}
