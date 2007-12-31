package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.unisb.cs.depend.ccs_sem.exceptions.InteralSystemException;
import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;


public class UnknownString extends AbstractExpression {
    
    private String name;
    private List<String> parameters;

    public UnknownString(String name, List<String> parameters) {
        super();
        assert name != null && parameters != null;
        
        this.name = name;
        this.parameters = parameters;
    }

    public Collection<Expression> getChildren() {
        StackTraceElement topmostStackTraceElement = Thread.currentThread().getStackTrace()[0];
        throw new InteralSystemException(topmostStackTraceElement.getClassName()
            + "." + topmostStackTraceElement.getMethodName()
            + " should never be called. Did you forget to call replaceRecursion?");
    }

    @Override
    protected List<Transition> evaluate0() {
        StackTraceElement topmostStackTraceElement = Thread.currentThread().getStackTrace()[0];
        throw new InteralSystemException(topmostStackTraceElement.getClassName()
            + "." + topmostStackTraceElement.getMethodName()
            + " should never be called. Did you forget to call replaceRecursion?");
    }

    public Expression replaceRecursion(List<Declaration> declarations) throws ParseException {
        for (Declaration decl: declarations) {
            if (matches(decl)) {
                RecursiveExpr newExpr = new RecursiveExpr(decl, parameters);
                return newExpr;
            }
        }
        
        // no match: take the string as prefix and add a "stop"
        // (error if parameters are given)
        if (parameters.size() > 0) {
            // search for possible matches
            List<Declaration> proposals = new ArrayList<Declaration>();
            for (Declaration decl: declarations)
                if (decl.getName().equalsIgnoreCase(name))
                    proposals.add(decl);
            StringBuilder sb = new StringBuilder("Unknown recursion identifier ");
            // TODO sb fuellen
            throw new ParseException(sb.toString());
        }
        // TODO Auto-generated method stub
        return null;
    }

    public boolean matches(Declaration decl) {
        return decl.getName().equals(name) && decl.getParameters().size() == parameters.size();
    }

}
