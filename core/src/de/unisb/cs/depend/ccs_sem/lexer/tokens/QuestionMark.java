package de.unisb.cs.depend.ccs_sem.lexer.tokens;


public class QuestionMark extends AbstractToken {

    public QuestionMark(int startPosition) {
        super(startPosition, startPosition+1);
    }

    @Override
    public String toString() {
        return "?";
    }

}
