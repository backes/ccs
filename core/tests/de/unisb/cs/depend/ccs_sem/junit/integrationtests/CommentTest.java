package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class CommentTest extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "// a single-line comment // with other // comment signs\n" +
        "// another single-line comment (* with embedded multi-line comment *) foo\n" +
        "// some special chars: §§{{[²³é´àüö#µ\n" +
        "(* the same for multi-line: §§{{[²³é´àüö#µ *)" +
        " (* multi-line comment \n\n\r\n with newlines \r *)\n" +
        "" +
        "" +
        "a.(*comment *)b//another comment\n.(*c:*)c!(*and a value:*)(3*(*two:*)2)";
    }

    @Override
    protected void addStates() {
        addState("a.b.c!6.0");
        addState("b.c!6.0");
        addState("c!6.0");
        addState("0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "a");
        addTransition(1, 2, "b");
        addTransition(2, 3, "c!6");
    }

}
