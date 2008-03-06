package de.unisb.cs.depend.ccs_sem.junit.integrationtests.invalid;


import org.junit.Assert;
import org.junit.Test;

import de.unisb.cs.depend.ccs_sem.exceptions.LexException;
import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.parser.CCSParser;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;


public class Unguarded1 {

    private static final String term = "X[a] = out!a . X[a+1] | Y[a+1]; "
        + "Y[n] = uepsilon!n . fertisch | X[2*n]; "
        + "X[0]";

    @Test
    public void checkForError() throws ParseException, LexException {
        final Program prog = new CCSParser().parse(term);
        Assert.assertFalse("Expression is not guarded.", prog.isGuarded());
    }

}
