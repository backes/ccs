package de.unisb.cs.depend.ccs_sem.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.lexer.tokens.Identifier;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.Token;
import de.unisb.cs.depend.ccs_sem.semantics.types.ProcessVariable;


public class ParsingResult {

    public class ReadComment {

        public int startPosition;
        public int endPosition;

        public ReadComment(int startPosition, int endPosition) {
            this.startPosition = startPosition;
            this.endPosition = endPosition;
        }

    }

    public class ReadProcessVariable {
        public ProcessVariable processVariable;
        public int tokenIndexStart;
        public int tokenIndexEnd;

        public ReadProcessVariable(ProcessVariable processVariable,
                int tokenIndexStart, int tokenIndexEnd) {
            this.processVariable = processVariable;
            this.tokenIndexStart = tokenIndexStart;
            this.tokenIndexEnd = tokenIndexEnd;
        }

        @Override
        public String toString() {
            return processVariable.getFullName();
        }

        public int getPositionStart() {
            return tokens == null ? -1 : tokens.get(tokenIndexStart).getStartPosition();
        }

        public int getPositionEnd() {
            return tokens == null ? -1 : tokens.get(tokenIndexEnd).getEndPosition();
        }
    }

    public List<Token> tokens;
    public final List<ReadProcessVariable> processVariables = new ArrayList<ReadProcessVariable>();
    public int mainExpressionTokenIndexStart;
    public int mainExpressionTokenIndexEnd;
    public final List<ReadComment> comments = new ArrayList<ReadComment>();
    public final Map<Identifier, Object> identifiers = new HashMap<Identifier, Object>();

    public void addProcessVariable(ProcessVariable processVariable,
            int tokenIndexStart, int tokenIndexEnd) {
        processVariables.add(new ReadProcessVariable(processVariable, tokenIndexStart, tokenIndexEnd));
    }

    public void newComment(int startPosition, int endPosition) {
        comments.add(new ReadComment(startPosition, endPosition));
    }

    public void addIdentifierMapping(Identifier identifier, Object semantic) {
        identifiers.put(identifier, semantic);
    }

}
