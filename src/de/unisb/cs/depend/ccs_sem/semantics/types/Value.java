package de.unisb.cs.depend.ccs_sem.semantics.types;

import java.util.List;


public interface Value extends Cloneable {
    String getValue();

    Value replaceParameters(List<Value> parameters);

    Value insertParameters(List<Value> parameters);

    Value clone();

}
