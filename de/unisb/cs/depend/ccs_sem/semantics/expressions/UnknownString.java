package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.unisb.cs.depend.ccs_sem.exceptions.InteralSystemException;
import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.types.Action;
import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.utils.Globals;


public class UnknownString extends AbstractExpression {
    
    private String name;
    private List<String> parameters;

    public UnknownString(String name, List<String> parameters) {
        super();
        this.name = name;
        this.parameters = parameters;
    }

    public UnknownString(String name) {
        super();
        this.name = name;
        this.parameters = Collections.emptyList();
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
            sb.append(this);
            if (proposals.size() > 0) {
                sb.append(". Did you mean");
                for (Declaration prop: proposals)
                    sb.append(Globals.getNewline()).append("  - ").append(prop);
            }

            throw new ParseException(sb.toString());
        }
        Action prefix = Action.newAction(name);
        return new PrefixExpr(prefix, new StopExpr());
    }

    public boolean matches(Declaration decl) {
        return decl.getName().equals(name) && decl.getParameters().size() == parameters.size();
    }
    
    @Override
    public String toString() {
        if (parameters.size() == 0)
            return name;
        
        StringBuilder sb = new StringBuilder(name);
        sb.append('[');
        for (int i = 0; i < parameters.size(); ++i)
            sb.append(i==0 ? "," : "").append(parameters.get(i));
        sb.append(']');

        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((name == null) ? 0 : name.hashCode());
        result = PRIME * result + ((parameters == null) ? 0 : parameters.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final UnknownString other = (UnknownString) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (parameters == null) {
            if (other.parameters != null)
                return false;
        } else if (!parameters.equals(other.parameters))
            return false;
        return true;
    }

}
