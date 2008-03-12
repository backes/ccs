package de.unisb.cs.depend.ccs_sem.parser;

import java.util.ArrayList;
import java.util.List;

import de.unisb.cs.depend.ccs_sem.lexer.tokens.Token;
import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;


public class ParsingResult {

    public class ReadDeclaration {
        public Declaration declaration;
        public int tokenIndexStart;
        public int tokenIndexEnd;

        public ReadDeclaration(Declaration declaration, int tokenIndexStart,
                int tokenIndexEnd) {
            this.declaration = declaration;
            this.tokenIndexStart = tokenIndexStart;
            this.tokenIndexEnd = tokenIndexEnd;
        }

        @Override
        public String toString() {
            return declaration.getFullName();
        }

        public int getPositionStart() {
            return tokens == null ? -1 : tokens.get(tokenIndexStart).getStartPosition();
        }

        public int getPositionEnd() {
            return tokens == null ? -1 : tokens.get(tokenIndexEnd).getEndPosition();
        }
    }

    public List<Token> tokens;
    public List<ReadDeclaration> declarations = new ArrayList<ReadDeclaration>();
    public int mainExpressionTokenIndexStart;
    public int mainExpressionTokenIndexEnd;

    public void addDeclaration(Declaration readDeclaration,
            int tokenIndexStart, int tokenIndexEnd) {
        declarations.add(new ReadDeclaration(readDeclaration, tokenIndexStart, tokenIndexEnd));
    }

}
