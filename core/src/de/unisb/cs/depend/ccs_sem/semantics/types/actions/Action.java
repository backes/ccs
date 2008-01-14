package de.unisb.cs.depend.ccs_sem.semantics.types.actions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.value.ConstantValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.value.Value;


public abstract class Action {

    // caching
    private static Map<Action, Action> repository = new HashMap<Action, Action>();

    // TODO not needed anymore?
    public static Action newAction(String name) throws ParseException {
        // TODO distinguish between different types of values
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

    public abstract String getLabel();

    public abstract String getChannel();

    public abstract Value getMessage();

    /**
     * Overwritten by all Actions that can act as input action.
     * @return
     */
    public boolean isInputAction() {
        return false;
    }

    /**
     * Overwritten by all Actions that can act as output action.
     * @return
     */
    public boolean isOutputAction() {
        return false;
    }

    public abstract Action instantiate(List<Value> parameters);

    public abstract Action insertParameters(List<Parameter> parameters);

    /**
     * @param actionToCheck
     * @return true iff <b>this</b> Action restrict the Action actionToCheck.
     */
    public abstract boolean restricts(Action actionToCheck);

    @Override
    public String toString() {
        return getLabel();
    }

    /**
     * See {@link Transition#synchronizeWith(Action)}
     * @param otherAction the Action that we want to synchronize with
     * @param our target Expression before synchronizing
     * @return either the Expression target or a new one that's instantiated using otherAction
     */
    public abstract Expression synchronizeWith(Action otherAction, Expression target);

    public abstract Action instantiateInputValue(Value value);

}
