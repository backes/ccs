package de.unisb.cs.depend.ccs_sem.semantics;

import java.util.Map;
import java.util.Map.Entry;

import de.unisb.cs.depend.ccs_sem.lexer.tokens.Identifier;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;


public class Program {

    private Map<Identifier, Expression> declarations;
    private Expression mainExpression;

    public Program(Map<Identifier, Expression> declarations, Expression expr) {
        this.declarations = declarations;
        this.mainExpression = expr;
    }
    
    @Override
    public String toString() {
        String newLine = System.getProperty("line.separator");
        assert newLine != null;
        String space = " ";
        
        StringBuilder sb = new StringBuilder();
        for (Entry<Identifier, Expression> decl: declarations.entrySet()) {
            sb.append(decl.getKey()).append(space);
            sb.append('=').append(space);
            sb.append(decl.getValue()).append(newLine);
        }
        if (declarations.size() > 0)
            sb.append(newLine);
        
        sb.append(mainExpression);
        
        return sb.toString();
    }

}
