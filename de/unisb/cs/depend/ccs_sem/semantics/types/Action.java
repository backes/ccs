package de.unisb.cs.depend.ccs_sem.semantics.types;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;


public abstract class Action {

    public abstract String getValue();

    public static Action newAction(String name) throws ParseException {
        int index = name.indexOf('?');
        if (index != -1) {
            String firstPart = name.substring(0, index);
            String secondPart = name.substring(index+1);
            if (firstPart.contains("?") || firstPart.contains("!")
                    || secondPart.contains("?") || secondPart.contains("!"))
                throw new ParseException("Illegal action: " + name);
            return new InputAction(firstPart, secondPart);
        }
        
        index = name.indexOf('!');
        if (index != -1) {
            String firstPart = name.substring(0, index);
            String secondPart = name.substring(index+1);
            if (firstPart.contains("?") || firstPart.contains("!")
                    || secondPart.contains("?") || secondPart.contains("!"))
                throw new ParseException("Illegal action: " + name);
            return new OutputAction(firstPart, secondPart);
        }
        
        return new SimpleAction(name);
    }
}
