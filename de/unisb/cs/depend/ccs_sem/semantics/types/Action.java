package de.unisb.cs.depend.ccs_sem.semantics.types;

import java.util.List;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;


public abstract class Action {

    public abstract String getLabel();

    public static Action newAction(String name) throws ParseException {
        if ("i".equals(name))
            return TauAction.get();
        
        int index = name.indexOf('?');
        if (index != -1) {
            String firstPart = name.substring(0, index);
            String secondPart = name.substring(index+1);
            if (firstPart.contains("?") || firstPart.contains("!")
                    || secondPart.contains("?") || secondPart.contains("!"))
                throw new ParseException("Illegal action: " + name);
            return new InputAction(firstPart, new ConstantValue(secondPart));
        }
        
        index = name.indexOf('!');
        if (index != -1) {
            String firstPart = name.substring(0, index);
            String secondPart = name.substring(index+1);
            if (firstPart.contains("?") || firstPart.contains("!")
                    || secondPart.contains("?") || secondPart.contains("!"))
                throw new ParseException("Illegal action: " + name);
            return new OutputAction(firstPart, new ConstantValue(secondPart));
        }
        
        return new SimpleAction(new ConstantValue(name));
    }

    public abstract boolean isCounterTransition(Action action);

    public abstract Action replaceParameters(List<Value> parameters);

}
