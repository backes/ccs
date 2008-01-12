package de.unisb.cs.depend.ccs_sem.semantics.types;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.exceptions.InternalSystemException;
import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;


public abstract class Action implements Cloneable {

    private static Map<Action, Action> repository = new HashMap<Action, Action>();

    public abstract String getLabel();

    public static Action newAction(String name) throws ParseException {
        Action action;
        int index;
        if ("i".equals(name)) {
            action = TauAction.get();
        } else if ((index = name.indexOf('?')) != -1) {
            final String firstPart = name.substring(0, index);
            final String secondPart = name.substring(index+1);
            if (firstPart.contains("?") || firstPart.contains("!")
                    || secondPart.contains("?") || secondPart.contains("!"))
                throw new ParseException("Illegal action: " + name);
            action = new InputAction(firstPart,
                secondPart.isEmpty() ? null : new ConstantValue(secondPart));
        } else if ((index = name.indexOf('!')) != -1) {
            final String firstPart = name.substring(0, index);
            final String secondPart = name.substring(index+1);
            if (firstPart.contains("?") || firstPart.contains("!")
                    || secondPart.contains("?") || secondPart.contains("!"))
                throw new ParseException("Illegal action: " + name);
            action = new OutputAction(firstPart,
                secondPart.isEmpty() ? null : new ConstantValue(secondPart));
        } else {
            action = new SimpleAction(new ConstantValue(name));
        }

        return getAction(action);
    }

    public static Action getAction(Action action) {
        final Action foundAction = repository.get(action);
        if (foundAction != null)
            return foundAction;

        repository.put(action, action);

        return action;
    }

    /**
     * @return the counter action, or <code>null</code> if there is no counteraction
     */
    public abstract Action getCounterAction();

    public abstract boolean isCounterAction(Action action);

    public abstract Action instantiate(List<Value> parameters);

    public abstract Action insertParameters(List<Value> parameters);

    @Override
    public String toString() {
        return getLabel();
    }

    @Override
    public Action clone() {
        Action cloned;
        try {
            cloned = (Action) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new InternalSystemException("Action cannot be cloned", e);
        }

        return cloned;
    }

}
